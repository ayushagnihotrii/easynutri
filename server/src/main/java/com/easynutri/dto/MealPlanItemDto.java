package com.easynutri.dto;

import java.time.LocalDate;

public record MealPlanItemDto(
    Long id,
    String recipeId,
    String recipeTitle,
    LocalDate plannedDate,
    String mealSlot,
    int servings) {}
