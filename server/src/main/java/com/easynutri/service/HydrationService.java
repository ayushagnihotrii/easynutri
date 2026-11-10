package com.easynutri.service;

import com.easynutri.domain.hydration.DefaultHydrationTargetProvider;
import com.easynutri.domain.hydration.HydrationTargetProvider;
import com.easynutri.domain.hydration.HydrationTargetResult;
import com.easynutri.dto.*;
import com.easynutri.entity.*;
import com.easynutri.repository.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class HydrationService {

  private final WaterLogRepository waterLogRepository;
  private final HydrationPreferenceRepository preferenceRepository;
  private final WorkoutLogRepository workoutLogRepository;
  private final UserRepository userRepository;
  private final UserProfileRepository userProfileRepository;
  private final HydrationTargetProvider targetProvider = new DefaultHydrationTargetProvider();

  public HydrationService(
      WaterLogRepository waterLogRepository,
      HydrationPreferenceRepository preferenceRepository,
      WorkoutLogRepository workoutLogRepository,
      UserRepository userRepository,
      UserProfileRepository userProfileRepository) {
    this.waterLogRepository = waterLogRepository;
    this.preferenceRepository = preferenceRepository;
    this.workoutLogRepository = workoutLogRepository;
    this.userRepository = userRepository;
    this.userProfileRepository = userProfileRepository;
  }

  @Transactional(readOnly = true)
  public HydrationSummaryDto getHydrationSummary(Long userId, LocalDate date) {
    LocalDate targetDate = date != null ? date : LocalDate.now();

    Optional<UserProfileEntity> profileOpt = userProfileRepository.findByUserId(userId);
    double weightKg = profileOpt.map(UserProfileEntity::getWeightKg).orElse(65.0);
    var healthConditions =
        profileOpt.map(UserProfileEntity::getHealthConditions).orElse(java.util.Set.of());

    List<WorkoutLogEntity> workouts =
        workoutLogRepository.findByUserIdAndRecordDate(userId, targetDate);
    int workoutMinutes = workouts.stream().mapToInt(WorkoutLogEntity::getDurationMinutes).sum();

    HydrationPreferenceEntity pref =
        preferenceRepository
            .findByUserId(userId)
            .orElseGet(
                () -> {
                  UserEntity user =
                      userRepository
                          .findById(userId)
                          .orElseThrow(
                              () -> new IllegalArgumentException("User not found: " + userId));
                  HydrationPreferenceEntity newPref = new HydrationPreferenceEntity(user, 2500);
                  return preferenceRepository.save(newPref);
                });

    HydrationTargetResult calc =
        targetProvider.calculateTarget(
            weightKg,
            workoutMinutes,
            healthConditions,
            pref.isClinicalRestriction() ? pref.getDailyTargetMl() : null);

    int targetMl = calc.dailyTargetMl();

    List<WaterLogEntity> logs =
        waterLogRepository.findByUserIdAndRecordDateOrderByLoggedAtAsc(userId, targetDate);
    List<WaterLogDto> dtos = new ArrayList<>();
    int currentIntake = 0;
    DateTimeFormatter timeFmt =
        DateTimeFormatter.ofPattern("h:mm a", Locale.US).withZone(ZoneId.systemDefault());

    for (WaterLogEntity log : logs) {
      currentIntake += log.getAmountMl();
      dtos.add(
          new WaterLogDto(
              log.getId(),
              log.getRecordDate(),
              log.getAmountMl(),
              log.getNotes(),
              log.getLoggedAt(),
              timeFmt.format(log.getLoggedAt())));
    }

    int progress = (int) Math.min(100, ((double) currentIntake / targetMl) * 100);
    DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.US);

    return new HydrationSummaryDto(
        targetDate,
        targetDate.format(dateFmt),
        currentIntake,
        targetMl,
        progress,
        calc.isClinicallyRestricted(),
        calc.clinicalNotice(),
        calc.recommendation(),
        dtos,
        pref.isReminderEnabled(),
        pref.getReminderIntervalHours(),
        pref.getStartTime(),
        pref.getEndTime());
  }

  public WaterLogDto logWater(Long userId, LogWaterRequest request) {
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    LocalDate date = request.date() != null ? request.date() : LocalDate.now();
    int amount = Math.max(10, request.amountMl());
    String notes =
        (request.notes() != null && !request.notes().isBlank())
            ? request.notes()
            : (amount + " ml logged");

    WaterLogEntity entity = new WaterLogEntity(user, date, amount, notes);
    WaterLogEntity saved = waterLogRepository.save(entity);

    DateTimeFormatter timeFmt =
        DateTimeFormatter.ofPattern("h:mm a", Locale.US).withZone(ZoneId.systemDefault());

    return new WaterLogDto(
        saved.getId(),
        saved.getRecordDate(),
        saved.getAmountMl(),
        saved.getNotes(),
        saved.getLoggedAt(),
        timeFmt.format(saved.getLoggedAt()));
  }

  public void deleteWaterLog(Long userId, Long logId) {
    WaterLogEntity log =
        waterLogRepository
            .findById(logId)
            .orElseThrow(() -> new IllegalArgumentException("Water log not found: " + logId));

    if (!log.getUser().getId().equals(userId)) {
      throw new SecurityException("Unauthorized to delete water log.");
    }

    waterLogRepository.delete(log);
  }

  public void savePreferences(Long userId, SaveHydrationPreferencesRequest request) {
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    HydrationPreferenceEntity pref =
        preferenceRepository
            .findByUserId(userId)
            .orElseGet(() -> new HydrationPreferenceEntity(user, 2500));

    if (request.dailyTargetMl() != null && request.dailyTargetMl() > 500) {
      pref.setDailyTargetMl(request.dailyTargetMl());
    }
    if (request.reminderEnabled() != null) {
      pref.setReminderEnabled(request.reminderEnabled());
    }
    if (request.reminderIntervalHours() != null && request.reminderIntervalHours() > 0) {
      pref.setReminderIntervalHours(request.reminderIntervalHours());
    }
    if (request.startTime() != null && !request.startTime().isBlank()) {
      pref.setStartTime(request.startTime());
    }
    if (request.endTime() != null && !request.endTime().isBlank()) {
      pref.setEndTime(request.endTime());
    }

    preferenceRepository.save(pref);
  }
}
