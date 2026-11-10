package com.easynutri.dto;

import java.time.LocalDate;

public record MealPlanItemDto(
    Long id,
    String recipeId,
    String recipeTitle,
    LocalDate plannedDate,
    String mealSlot,
    int servings,
    boolean locked,
    boolean completed,
    String selectionReason,
    String notes,
    double calories,
    double proteinG,
    double carbsG,
    double fatG,
    int prepMinutes,
    int cookMinutes) {

  public MealPlanItemDto(
      Long id,
      String recipeId,
      String recipeTitle,
      LocalDate plannedDate,
      String mealSlot,
      int servings) {
    this(
        id,
        recipeId,
        recipeTitle,
        plannedDate,
        mealSlot,
        servings,
        false,
        false,
        "Planned meal",
        "",
        0.0,
        0.0,
        0.0,
        0.0,
        15,
        20);
  }
}
