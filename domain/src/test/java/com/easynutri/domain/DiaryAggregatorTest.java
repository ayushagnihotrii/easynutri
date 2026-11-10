package com.easynutri.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.easynutri.domain.diary.DiaryAggregator;
import com.easynutri.domain.diary.DiaryDaySummary;
import com.easynutri.domain.diary.DiaryItemSnapshot;
import com.easynutri.domain.diary.MealSlot;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DiaryAggregatorTest {

  @Test
  void shouldAggregateEmptyDayGracefully() {
    DiaryDaySummary summary = DiaryAggregator.aggregateDay(null);
    assertEquals(0.0, summary.totalCalories());
    assertEquals(0, summary.totalItemsCount());

    DiaryDaySummary emptySummary = DiaryAggregator.aggregateDay(Map.of());
    assertEquals(0.0, emptySummary.totalCalories());
    assertEquals(0, emptySummary.totalItemsCount());
  }

  @Test
  void shouldAggregateMultipleMealSlotsCorrectly() {
    Map<MealSlot, List<DiaryItemSnapshot>> slots = new EnumMap<>(MealSlot.class);

    DiaryItemSnapshot oats =
        new DiaryItemSnapshot(
            "food-1", null, "Oats Porridge", 150.0, 1.0, 250.0, 9.5, 42.0, 4.5, 6.0, 15.0);
    DiaryItemSnapshot tea =
        new DiaryItemSnapshot(
            "food-2", null, "Masala Chai", 120.0, 1.0, 65.0, 2.0, 10.0, 1.8, 0.0, 25.0);
    slots.put(MealSlot.BREAKFAST, List.of(oats, tea));

    DiaryItemSnapshot dalRoti =
        new DiaryItemSnapshot(
            "food-3",
            "recipe-1",
            "Dal Tadka with 2 Phulkas",
            300.0,
            1.0,
            420.0,
            18.0,
            65.0,
            9.5,
            8.5,
            380.0);
    slots.put(MealSlot.LUNCH, List.of(dalRoti));

    DiaryDaySummary summary = DiaryAggregator.aggregateDay(slots);

    assertEquals(735.0, summary.totalCalories());
    assertEquals(29.5, summary.totalProteinG());
    assertEquals(117.0, summary.totalCarbsG());
    assertEquals(15.8, summary.totalFatG());
    assertEquals(14.5, summary.totalFiberG());
    assertEquals(420.0, summary.totalSodiumMg());
    assertEquals(3, summary.totalItemsCount());
    assertEquals(315.0, summary.caloriesBySlot().get(MealSlot.BREAKFAST));
    assertEquals(420.0, summary.caloriesBySlot().get(MealSlot.LUNCH));
  }

  @Test
  void shouldScaleSnapshotCorrectly() {
    DiaryItemSnapshot item =
        new DiaryItemSnapshot(
            "f1", null, "Paneer Bhurji", 100.0, 1.0, 200.0, 14.0, 5.0, 14.0, 1.0, 220.0);
    DiaryItemSnapshot scaled = item.scale(1.5);

    assertEquals(150.0, scaled.amountG());
    assertEquals(1.5, scaled.servings());
    assertEquals(300.0, scaled.calories());
    assertEquals(21.0, scaled.proteinG());
  }
}
