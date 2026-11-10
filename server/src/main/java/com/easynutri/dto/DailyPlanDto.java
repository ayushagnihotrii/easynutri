package com.easynutri.dto;

import java.time.LocalDate;
import java.util.List;

public record DailyPlanDto(
    LocalDate date,
    String dayOfWeek,
    List<MealPlanItemDto> meals,
    double dayCalories,
    double dayProteinG,
    double dayCarbsG,
    double dayFatG) {}
