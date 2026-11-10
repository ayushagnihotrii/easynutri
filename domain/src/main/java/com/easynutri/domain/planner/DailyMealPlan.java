package com.easynutri.domain.planner;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/** Consolidated daily meal plan aggregating all scheduled meals and daily nutritional totals. */
public record DailyMealPlan(
    LocalDate date,
    List<MealSlotProposal> meals,
    double totalCalories,
    double totalProteinG,
    double totalCarbG,
    double totalFatG,
    boolean satisfiesCalorieTarget,
    List<String> dailyNotes) {

  public DailyMealPlan {
    Objects.requireNonNull(date, "date cannot be null");
    meals = List.copyOf(meals != null ? meals : List.of());
    dailyNotes = List.copyOf(dailyNotes != null ? dailyNotes : List.of());
  }
}
