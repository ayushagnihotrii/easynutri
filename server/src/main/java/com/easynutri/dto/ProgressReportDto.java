package com.easynutri.dto;

import java.time.LocalDate;
import java.util.List;

public record ProgressReportDto(
    LocalDate startDate,
    LocalDate endDate,
    int daysInRange,
    int loggedDaysCount,
    int currentStreakDays,
    double adherenceScore,
    // Daily series for Chart.js
    List<String> dateLabels,
    List<Double> caloriesSeries,
    List<Double> proteinSeries,
    List<Double> carbsSeries,
    List<Double> fatSeries,
    List<Integer> stepsSeries,
    List<Integer> waterSeries,
    List<WeightMeasurementDto> weightHistory,
    // Period Averages
    double avgDailyCalories,
    double avgDailyProteinG,
    double avgDailyCarbsG,
    double avgDailyFatG,
    int avgDailySteps,
    int avgDailyWaterMl,
    // Profile Context
    String username,
    int age,
    String biologicalSex,
    double heightCm,
    double currentWeightKg,
    Double currentBmi,
    String bmiCategory,
    String healthGoal,
    String dietaryPreference,
    boolean sensitiveMode,
    List<String> insights) {}
