package com.easynutri.service;

import com.easynutri.domain.activity.*;
import com.easynutri.domain.calculation.DefaultBurnCalculator;
import com.easynutri.domain.policy.DisplayPolicy;
import com.easynutri.dto.*;
import com.easynutri.entity.*;
import com.easynutri.repository.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ActivityTrackingService {

  private final StepRecordRepository stepRecordRepository;
  private final WorkoutLogRepository workoutLogRepository;
  private final FoodDiaryEntryRepository diaryEntryRepository;
  private final UserRepository userRepository;
  private final UserProfileRepository userProfileRepository;
  private final DefaultBurnCalculator burnCalculator = new DefaultBurnCalculator();

  public ActivityTrackingService(
      StepRecordRepository stepRecordRepository,
      WorkoutLogRepository workoutLogRepository,
      FoodDiaryEntryRepository diaryEntryRepository,
      UserRepository userRepository,
      UserProfileRepository userProfileRepository) {
    this.stepRecordRepository = stepRecordRepository;
    this.workoutLogRepository = workoutLogRepository;
    this.diaryEntryRepository = diaryEntryRepository;
    this.userRepository = userRepository;
    this.userProfileRepository = userProfileRepository;
  }

  @Transactional(readOnly = true)
  public ActivitySummaryDto getActivitySummary(Long userId, LocalDate date) {
    LocalDate targetDate = date != null ? date : LocalDate.now();

    Optional<UserProfileEntity> profileOpt = userProfileRepository.findByUserId(userId);
    boolean sensitiveMode = profileOpt.map(UserProfileEntity::isSensitiveMode).orElse(false);
    double weightKg = profileOpt.map(UserProfileEntity::getWeightKg).orElse(65.0);
    double heightCm = profileOpt.map(UserProfileEntity::getHeightCm).orElse(170.0);
    int age = profileOpt.map(UserProfileEntity::getAge).orElse(28);
    boolean isMale =
        profileOpt.map(p -> "MALE".equalsIgnoreCase(p.getBiologicalSex())).orElse(true);

    double bmr = burnCalculator.calculateBmr(weightKg, heightCm, age, isMale);

    // Steps
    Optional<StepRecordEntity> stepOpt =
        stepRecordRepository.findByUserIdAndRecordDate(userId, targetDate);
    int steps = stepOpt.map(StepRecordEntity::getStepCount).orElse(0);
    int goalSteps = stepOpt.map(StepRecordEntity::getGoalSteps).orElse(10000);
    int stepProgress = (int) Math.min(100, ((double) steps / goalSteps) * 100);

    // Workouts
    List<WorkoutLogEntity> workouts =
        workoutLogRepository.findByUserIdAndRecordDate(userId, targetDate);
    List<WorkoutLogDto> workoutDtos = new ArrayList<>();
    double totalWorkoutBurn = 0.0;
    long totalWorkoutSteps = 0L;

    for (WorkoutLogEntity w : workouts) {
      ActivityType type = ActivityType.fromString(w.getActivityType());
      totalWorkoutBurn += w.getCaloriesBurned();
      totalWorkoutSteps += ActivityBurnEngine.estimateWorkoutSteps(type, w.getDurationMinutes());

      workoutDtos.add(
          new WorkoutLogDto(
              w.getId(),
              w.getRecordDate(),
              w.getActivityType(),
              type.getDisplayName(),
              type.getIcon(),
              w.getIntensity(),
              w.getDurationMinutes(),
              w.getDistanceKm(),
              w.getCaloriesBurned(),
              w.getMetValue(),
              w.getNotes(),
              DisplayPolicy.formatCalories(w.getCaloriesBurned(), sensitiveMode)));
    }

    // Step Burn with Deduplication
    double netStepBurn =
        ActivityBurnEngine.calculateNetStepBurn(steps, totalWorkoutSteps, weightKg);
    double stepBurnUnchecked = burnCalculator.estimateStepBurn(steps, weightKg);

    // Food Intake
    List<FoodDiaryEntryEntity> diaryEntries =
        diaryEntryRepository.findByUserIdAndDiaryDate(userId, targetDate);
    double foodCalories = 0.0;
    for (FoodDiaryEntryEntity entry : diaryEntries) {
      for (FoodDiaryItemEntity item : entry.getItems()) {
        foodCalories += item.getCalories();
      }
    }

    DailyEnergyBalance balance =
        DailyEnergyBalance.compute(foodCalories, bmr, totalWorkoutBurn, netStepBurn);

    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.US);

    return new ActivitySummaryDto(
        targetDate,
        targetDate.format(fmt),
        steps,
        goalSteps,
        stepProgress,
        stepBurnUnchecked,
        workoutDtos,
        Math.round(totalWorkoutBurn * 10.0) / 10.0,
        netStepBurn,
        Math.round(bmr * 10.0) / 10.0,
        Math.round(foodCalories * 10.0) / 10.0,
        balance.totalBurnKcal(),
        balance.netBalanceKcal(),
        DisplayPolicy.formatEnergyBalance(balance.netBalanceKcal(), sensitiveMode),
        sensitiveMode,
        DisplayPolicy.formatCalories(foodCalories, sensitiveMode),
        DisplayPolicy.formatCalories(balance.totalBurnKcal(), sensitiveMode),
        DisplayPolicy.formatCalories(balance.netBalanceKcal(), sensitiveMode));
  }

  public StepRecordEntity logSteps(Long userId, LogStepsRequest request) {
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    LocalDate date = request.date() != null ? request.date() : LocalDate.now();
    int goal =
        (request.goalSteps() != null && request.goalSteps() > 0) ? request.goalSteps() : 10000;
    String source =
        (request.sourceType() != null && !request.sourceType().isBlank())
            ? request.sourceType()
            : "MANUAL";

    StepRecordEntity record =
        stepRecordRepository
            .findByUserIdAndRecordDate(userId, date)
            .orElseGet(() -> new StepRecordEntity(user, date, 0, goal, source));

    record.setStepCount(request.steps());
    if (request.goalSteps() != null && request.goalSteps() > 0) {
      record.setGoalSteps(request.goalSteps());
    }
    record.setSourceType(source);
    return stepRecordRepository.save(record);
  }

  public WorkoutLogDto logWorkout(Long userId, LogWorkoutRequest request) {
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    Optional<UserProfileEntity> profileOpt = userProfileRepository.findByUserId(userId);
    double weightKg = profileOpt.map(UserProfileEntity::getWeightKg).orElse(65.0);
    boolean sensitiveMode = profileOpt.map(UserProfileEntity::isSensitiveMode).orElse(false);

    LocalDate date = request.date() != null ? request.date() : LocalDate.now();
    ActivityType type = ActivityType.fromString(request.activityType());
    ActivityIntensity intensity = ActivityIntensity.fromString(request.intensity());
    int duration = Math.max(1, request.durationMinutes());

    double metValue = type.getBaseMet() * intensity.getMetMultiplier();
    double burn;
    if (request.customCaloriesBurned() != null && request.customCaloriesBurned() > 0) {
      burn = request.customCaloriesBurned();
    } else {
      burn = ActivityBurnEngine.calculateWorkoutBurn(type, intensity, duration, weightKg);
    }

    WorkoutLogEntity entity = new WorkoutLogEntity();
    entity.setUser(user);
    entity.setRecordDate(date);
    entity.setActivityType(type.name());
    entity.setIntensity(intensity.name());
    entity.setDurationMinutes(duration);
    entity.setDistanceKm(request.distanceKm());
    entity.setCaloriesBurned(burn);
    entity.setMetValue(metValue);
    entity.setNotes(request.notes());
    entity.setSourceType("MANUAL");

    WorkoutLogEntity saved = workoutLogRepository.save(entity);

    return new WorkoutLogDto(
        saved.getId(),
        saved.getRecordDate(),
        saved.getActivityType(),
        type.getDisplayName(),
        type.getIcon(),
        saved.getIntensity(),
        saved.getDurationMinutes(),
        saved.getDistanceKm(),
        saved.getCaloriesBurned(),
        saved.getMetValue(),
        saved.getNotes(),
        DisplayPolicy.formatCalories(saved.getCaloriesBurned(), sensitiveMode));
  }

  public void deleteWorkout(Long userId, Long workoutId) {
    WorkoutLogEntity workout =
        workoutLogRepository
            .findById(workoutId)
            .orElseThrow(() -> new IllegalArgumentException("Workout not found: " + workoutId));

    if (!workout.getUser().getId().equals(userId)) {
      throw new SecurityException("Unauthorized to delete workout.");
    }

    workoutLogRepository.delete(workout);
  }
}
