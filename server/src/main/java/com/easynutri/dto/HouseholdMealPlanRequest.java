package com.easynutri.dto;

import java.time.LocalDate;
import java.util.List;

public record HouseholdMealPlanRequest(
    Long householdId,
    String recipeId,
    LocalDate planDate,
    String mealSlot,
    List<Long> participatingUserIds) {

  public HouseholdMealPlanRequest {
    participatingUserIds =
        participatingUserIds != null ? List.copyOf(participatingUserIds) : List.of();
    mealSlot = mealSlot != null ? mealSlot.toUpperCase() : "DINNER";
  }
}
