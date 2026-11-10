package com.easynutri.dto;

import java.util.List;

/** Unified dashboard payload for native mobile app home screen. */
public record DashboardDto(
    UserProfileDto profile,
    Integer targetCalories,
    Integer consumedCalories,
    Integer burnedCalories,
    Integer netCalories,
    Integer targetProteinG,
    Integer consumedProteinG,
    Integer targetCarbG,
    Integer consumedCarbG,
    Integer targetFatG,
    Integer consumedFatG,
    Double targetWaterL,
    Double consumedWaterL,
    Integer targetSteps,
    Integer currentSteps,
    int activeStreakDays,
    int expiringPantryCount,
    List<MealPlanItemDto> todayMeals,
    List<RecipeDetailDto> suggestedRecipes) {}
