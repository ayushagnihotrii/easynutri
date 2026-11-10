package com.easynutri.dto;

import java.time.LocalDate;
import java.util.List;

public record ActivitySummaryDto(
    LocalDate date,
    String formattedDate,
    int steps,
    int goalSteps,
    int stepProgressPercent,
    double stepCalories,
    List<WorkoutLogDto> workouts,
    double totalWorkoutCalories,
    double netStepBurnKcal,
    double bmrKcal,
    double foodCalories,
    double totalExpenditureKcal,
    double netEnergyBalanceKcal,
    String energyBalanceStatus,
    boolean sensitiveMode,
    String formattedFoodCalories,
    String formattedTotalExpenditure,
    String formattedNetBalance) {}
