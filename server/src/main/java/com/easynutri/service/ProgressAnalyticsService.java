package com.easynutri.service;

import com.easynutri.domain.calculation.BmiCalculator;
import com.easynutri.domain.insights.DefaultInsightEngine;
import com.easynutri.domain.insights.InsightEngine;
import com.easynutri.domain.model.ProfileContext;
import com.easynutri.domain.policy.DisplayPolicy;
import com.easynutri.dto.*;
import com.easynutri.entity.*;
import com.easynutri.repository.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProgressAnalyticsService {

  private final FoodDiaryEntryRepository diaryEntryRepository;
  private final StepRecordRepository stepRecordRepository;
  private final WaterLogRepository waterLogRepository;
  private final WeightMeasurementRepository weightRepository;
  private final UserRepository userRepository;
  private final UserProfileRepository userProfileRepository;
  private final InsightEngine insightEngine = new DefaultInsightEngine();

  public ProgressAnalyticsService(
      FoodDiaryEntryRepository diaryEntryRepository,
      StepRecordRepository stepRecordRepository,
      WaterLogRepository waterLogRepository,
      WeightMeasurementRepository weightRepository,
      UserRepository userRepository,
      UserProfileRepository userProfileRepository) {
    this.diaryEntryRepository = diaryEntryRepository;
    this.stepRecordRepository = stepRecordRepository;
    this.waterLogRepository = waterLogRepository;
    this.weightRepository = weightRepository;
    this.userRepository = userRepository;
    this.userProfileRepository = userProfileRepository;
  }

  @Transactional(readOnly = true)
  public ProgressReportDto getProgressReport(Long userId, int days) {
    int safeDays = Math.max(7, Math.min(90, days));
    LocalDate endDate = LocalDate.now();
    LocalDate startDate = endDate.minusDays(safeDays - 1);

    Optional<UserEntity> userOpt = userRepository.findById(userId);
    String username = userOpt.map(UserEntity::getUsername).orElse("User");

    Optional<UserProfileEntity> profileOpt = userProfileRepository.findByUserId(userId);
    boolean sensitiveMode = profileOpt.map(UserProfileEntity::isSensitiveMode).orElse(false);
    int age = profileOpt.map(UserProfileEntity::getAge).orElse(28);
    String biologicalSex = profileOpt.map(UserProfileEntity::getBiologicalSex).orElse("MALE");
    double heightCm = profileOpt.map(UserProfileEntity::getHeightCm).orElse(170.0);
    double weightKg = profileOpt.map(UserProfileEntity::getWeightKg).orElse(65.0);
    String healthGoal = profileOpt.map(UserProfileEntity::getHealthGoal).orElse("MAINTAIN");
    String diet = profileOpt.map(UserProfileEntity::getDietaryPreference).orElse("VEGETARIAN");

    BmiCalculator.BmiResult bmiCalc = BmiCalculator.calculate(weightKg, heightCm);
    double currentBmi = bmiCalc.bmiValue();
    String bmiCat = bmiCalc.label();

    // Diary entries in range
    List<FoodDiaryEntryEntity> diaryEntries =
        diaryEntryRepository.findByUserIdAndDiaryDateBetweenOrderByDiaryDateAsc(
            userId, startDate, endDate);

    Map<LocalDate, Map<String, Double>> dailyNutrition = new LinkedHashMap<>();
    Map<LocalDate, Integer> diaryItemsPerDay = new HashMap<>();

    for (FoodDiaryEntryEntity entry : diaryEntries) {
      LocalDate d = entry.getDiaryDate();
      dailyNutrition.putIfAbsent(
          d,
          new HashMap<>(
              Map.of(
                  "calories", 0.0,
                  "protein", 0.0,
                  "carbs", 0.0,
                  "fat", 0.0,
                  "fiber", 0.0,
                  "sodium", 0.0)));

      Map<String, Double> map = dailyNutrition.get(d);
      for (FoodDiaryItemEntity item : entry.getItems()) {
        map.put("calories", map.get("calories") + item.getCalories());
        map.put("protein", map.get("protein") + item.getProteinG());
        map.put("carbs", map.get("carbs") + item.getCarbsG());
        map.put("fat", map.get("fat") + item.getFatG());
        map.put("fiber", map.get("fiber") + item.getFiberG());
        map.put("sodium", map.get("sodium") + item.getSodiumMg());

        diaryItemsPerDay.put(d, diaryItemsPerDay.getOrDefault(d, 0) + 1);
      }
    }

    // Steps in range
    List<StepRecordEntity> stepRecords =
        stepRecordRepository.findByUserIdAndRecordDateBetweenOrderByRecordDateAsc(
            userId, startDate, endDate);
    Map<LocalDate, Integer> dailySteps = new HashMap<>();
    for (StepRecordEntity s : stepRecords) {
      dailySteps.put(s.getRecordDate(), s.getStepCount());
    }

    // Water in range
    List<WaterLogEntity> waterLogs =
        waterLogRepository.findByUserIdAndRecordDateBetweenOrderByRecordDateAscLoggedAtAsc(
            userId, startDate, endDate);
    Map<LocalDate, Integer> dailyWater = new HashMap<>();
    for (WaterLogEntity w : waterLogs) {
      dailyWater.put(
          w.getRecordDate(), dailyWater.getOrDefault(w.getRecordDate(), 0) + w.getAmountMl());
    }

    // Weight history in range
    List<WeightMeasurementEntity> weights =
        weightRepository.findByUserIdAndMeasurementDateBetweenOrderByMeasurementDateAsc(
            userId, startDate, endDate);
    List<WeightMeasurementDto> weightHistory = new ArrayList<>();
    for (WeightMeasurementEntity w : weights) {
      double bmiVal;
      String cat;
      if (w.getBmi() != null) {
        bmiVal = w.getBmi();
        cat = BmiCalculator.calculate(w.getWeightKg(), heightCm).label();
      } else {
        BmiCalculator.BmiResult r = BmiCalculator.calculate(w.getWeightKg(), heightCm);
        bmiVal = r.bmiValue();
        cat = r.label();
      }
      weightHistory.add(
          new WeightMeasurementDto(
              w.getId(),
              w.getMeasurementDate(),
              w.getWeightKg(),
              bmiVal,
              w.getNotes(),
              DisplayPolicy.formatWeight(w.getWeightKg(), sensitiveMode),
              cat));
    }

    // Daily Series lists for chart rendering
    DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("MMM d", Locale.US);
    List<String> dateLabels = new ArrayList<>();
    List<Double> calSeries = new ArrayList<>();
    List<Double> proteinSeries = new ArrayList<>();
    List<Double> carbsSeries = new ArrayList<>();
    List<Double> fatSeries = new ArrayList<>();
    List<Integer> stepsSeries = new ArrayList<>();
    List<Integer> waterSeries = new ArrayList<>();

    double sumCal = 0, sumProt = 0, sumCarb = 0, sumFat = 0;
    int sumSteps = 0, sumWater = 0;
    int activeNutritionDays = 0;

    for (int i = 0; i < safeDays; i++) {
      LocalDate current = startDate.plusDays(i);
      dateLabels.add(current.format(labelFmt));

      Map<String, Double> dayNutr = dailyNutrition.get(current);
      if (dayNutr != null) {
        double c = Math.round(dayNutr.get("calories") * 10.0) / 10.0;
        double p = Math.round(dayNutr.get("protein") * 10.0) / 10.0;
        double cb = Math.round(dayNutr.get("carbs") * 10.0) / 10.0;
        double f = Math.round(dayNutr.get("fat") * 10.0) / 10.0;

        calSeries.add(sensitiveMode ? 0.0 : c);
        proteinSeries.add(sensitiveMode ? 0.0 : p);
        carbsSeries.add(sensitiveMode ? 0.0 : cb);
        fatSeries.add(sensitiveMode ? 0.0 : f);

        sumCal += c;
        sumProt += p;
        sumCarb += cb;
        sumFat += f;
        activeNutritionDays++;
      } else {
        calSeries.add(0.0);
        proteinSeries.add(0.0);
        carbsSeries.add(0.0);
        fatSeries.add(0.0);
      }

      int steps = dailySteps.getOrDefault(current, 0);
      stepsSeries.add(steps);
      sumSteps += steps;

      int water = dailyWater.getOrDefault(current, 0);
      waterSeries.add(water);
      sumWater += water;
    }

    double avgCal = activeNutritionDays > 0 ? (sumCal / activeNutritionDays) : 0.0;
    double avgProt = activeNutritionDays > 0 ? (sumProt / activeNutritionDays) : 0.0;
    double avgCarb = activeNutritionDays > 0 ? (sumCarb / activeNutritionDays) : 0.0;
    double avgFat = activeNutritionDays > 0 ? (sumFat / activeNutritionDays) : 0.0;
    int avgSteps = sumSteps / safeDays;
    int avgWater = sumWater / safeDays;

    // Calculate streak
    List<LocalDate> distinctDates = diaryEntryRepository.findDistinctLoggedDates(userId);
    int streak = calculateStreak(distinctDates);

    double adherence =
        insightEngine.calculateAdherenceScore(streak, activeNutritionDays / (double) safeDays);

    // Rule-based insights
    Map<String, Map<String, Double>> insightMap = new HashMap<>();
    for (Map.Entry<LocalDate, Map<String, Double>> entry : dailyNutrition.entrySet()) {
      insightMap.put(entry.getKey().toString(), entry.getValue());
    }

    List<String> allergiesList =
        profileOpt.map(p -> new ArrayList<>(p.getAllergies())).orElseGet(ArrayList::new);
    List<String> conditionsList =
        profileOpt.map(p -> new ArrayList<>(p.getHealthConditions())).orElseGet(ArrayList::new);

    ProfileContext profileCtx =
        new ProfileContext(
            String.valueOf(userId),
            age,
            biologicalSex,
            heightCm,
            weightKg,
            diet,
            allergiesList,
            conditionsList);

    List<String> insights = insightEngine.generateWeeklyInsights(insightMap, profileCtx);

    return new ProgressReportDto(
        startDate,
        endDate,
        safeDays,
        activeNutritionDays,
        streak,
        adherence,
        dateLabels,
        calSeries,
        proteinSeries,
        carbsSeries,
        fatSeries,
        stepsSeries,
        waterSeries,
        weightHistory,
        Math.round(avgCal * 10.0) / 10.0,
        Math.round(avgProt * 10.0) / 10.0,
        Math.round(avgCarb * 10.0) / 10.0,
        Math.round(avgFat * 10.0) / 10.0,
        avgSteps,
        avgWater,
        username,
        age,
        biologicalSex,
        heightCm,
        weightKg,
        currentBmi,
        bmiCat,
        healthGoal,
        diet,
        sensitiveMode,
        insights);
  }

  public WeightMeasurementDto logWeight(Long userId, LogWeightRequest request) {
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    UserProfileEntity profile =
        userProfileRepository
            .findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("User profile not found: " + userId));

    LocalDate date = request.date() != null ? request.date() : LocalDate.now();
    double weight = Math.max(20.0, Math.min(300.0, request.weightKg()));
    BmiCalculator.BmiResult bmiResult = BmiCalculator.calculate(weight, profile.getHeightCm());

    WeightMeasurementEntity entity =
        new WeightMeasurementEntity(user, date, weight, bmiResult.bmiValue(), request.notes());
    WeightMeasurementEntity saved = weightRepository.save(entity);

    // Update current profile weight as well
    profile.setWeightKg(weight);
    userProfileRepository.save(profile);

    boolean sensitive = profile.isSensitiveMode();
    return new WeightMeasurementDto(
        saved.getId(),
        saved.getMeasurementDate(),
        saved.getWeightKg(),
        saved.getBmi(),
        saved.getNotes(),
        DisplayPolicy.formatWeight(saved.getWeightKg(), sensitive),
        bmiResult.label());
  }

  private int calculateStreak(List<LocalDate> sortedDates) {
    if (sortedDates == null || sortedDates.isEmpty()) {
      return 0;
    }
    LocalDate today = LocalDate.now();
    LocalDate yesterday = today.minusDays(1);

    LocalDate first = sortedDates.get(0);
    if (!first.equals(today) && !first.equals(yesterday)) {
      return 0;
    }

    int streak = 1;
    LocalDate expected = first.minusDays(1);

    for (int i = 1; i < sortedDates.size(); i++) {
      LocalDate current = sortedDates.get(i);
      if (current.equals(expected)) {
        streak++;
        expected = expected.minusDays(1);
      } else if (current.equals(expected.plusDays(1))) {
        // duplicate date in set, continue
      } else {
        break;
      }
    }
    return streak;
  }
}
