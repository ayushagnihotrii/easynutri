package com.easynutri.domain.diary;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Pure Java aggregation service for computing daily nutritional statistics. */
public final class DiaryAggregator {

  private DiaryAggregator() {}

  /**
   * Summarizes all items consumed across slots on a given day.
   *
   * @param slotItems map of meal slot to list of item snapshots
   * @return comprehensive daily nutritional summary
   */
  public static DiaryDaySummary aggregateDay(Map<MealSlot, List<DiaryItemSnapshot>> slotItems) {
    if (slotItems == null || slotItems.isEmpty()) {
      return DiaryDaySummary.empty();
    }

    double totalCalories = 0.0;
    double totalProtein = 0.0;
    double totalCarbs = 0.0;
    double totalFat = 0.0;
    double totalFiber = 0.0;
    double totalSodium = 0.0;
    int totalCount = 0;

    Map<MealSlot, Double> caloriesBySlot = new EnumMap<>(MealSlot.class);
    Map<MealSlot, Integer> countBySlot = new EnumMap<>(MealSlot.class);

    for (Map.Entry<MealSlot, List<DiaryItemSnapshot>> entry : slotItems.entrySet()) {
      MealSlot slot = entry.getKey();
      List<DiaryItemSnapshot> items = entry.getValue();
      if (items == null) {
        continue;
      }

      double slotCalories = 0.0;
      for (DiaryItemSnapshot item : items) {
        slotCalories += item.calories();
        totalCalories += item.calories();
        totalProtein += item.proteinG();
        totalCarbs += item.carbsG();
        totalFat += item.fatG();
        totalFiber += item.fiberG();
        totalSodium += item.sodiumMg();
        totalCount++;
      }

      caloriesBySlot.put(slot, Math.round(slotCalories * 10.0) / 10.0);
      countBySlot.put(slot, items.size());
    }

    return new DiaryDaySummary(
        Math.round(totalCalories * 10.0) / 10.0,
        Math.round(totalProtein * 10.0) / 10.0,
        Math.round(totalCarbs * 10.0) / 10.0,
        Math.round(totalFat * 10.0) / 10.0,
        Math.round(totalFiber * 10.0) / 10.0,
        Math.round(totalSodium * 10.0) / 10.0,
        totalCount,
        caloriesBySlot,
        countBySlot);
  }
}
