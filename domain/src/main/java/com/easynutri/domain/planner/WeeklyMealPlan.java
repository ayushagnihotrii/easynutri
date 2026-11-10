package com.easynutri.domain.planner;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/** Complete 7-day personalized weekly meal plan with daily plans and pantry utilization summary. */
public record WeeklyMealPlan(
    LocalDate startDate,
    LocalDate endDate,
    List<DailyMealPlan> days,
    double averageDailyCalories,
    int totalPantryItemsUsed,
    int totalRecipesScheduled) {

  public WeeklyMealPlan {
    Objects.requireNonNull(startDate, "startDate cannot be null");
    Objects.requireNonNull(endDate, "endDate cannot be null");
    days = List.copyOf(days != null ? days : List.of());
  }
}
