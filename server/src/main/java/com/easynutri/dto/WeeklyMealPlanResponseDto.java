package com.easynutri.dto;

import java.time.LocalDate;
import java.util.List;

public record WeeklyMealPlanResponseDto(
    LocalDate weekStart,
    LocalDate weekEnd,
    List<DailyPlanDto> days,
    double totalCalories,
    double totalProteinG,
    double targetCaloriesPerDay,
    double targetProteinPerDay) {}
