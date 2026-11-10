package com.easynutri.domain.diary;

import java.util.Collections;
import java.util.Map;

/** Aggregated daily nutritional metrics across all meal slots. */
public record DiaryDaySummary(
    double totalCalories,
    double totalProteinG,
    double totalCarbsG,
    double totalFatG,
    double totalFiberG,
    double totalSodiumMg,
    int totalItemsCount,
    Map<MealSlot, Double> caloriesBySlot,
    Map<MealSlot, Integer> itemsCountBySlot) {

  public DiaryDaySummary {
    caloriesBySlot = caloriesBySlot == null ? Collections.emptyMap() : Map.copyOf(caloriesBySlot);
    itemsCountBySlot =
        itemsCountBySlot == null ? Collections.emptyMap() : Map.copyOf(itemsCountBySlot);
  }

  public static DiaryDaySummary empty() {
    return new DiaryDaySummary(
        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, Collections.emptyMap(), Collections.emptyMap());
  }
}
