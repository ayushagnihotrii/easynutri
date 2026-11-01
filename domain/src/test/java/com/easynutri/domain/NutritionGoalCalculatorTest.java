package com.easynutri.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.easynutri.domain.calculation.NutritionGoalCalculator;
import com.easynutri.domain.calculation.NutritionGoalCalculator.ActivityLevel;
import com.easynutri.domain.calculation.NutritionGoalCalculator.Goal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NutritionGoalCalculatorTest {

  @Test
  @DisplayName("Verify healthy adult weight loss targets with -500 kcal adjustment")
  void testAdultWeightLoss() {
    // 30yo male, 175cm, 80kg, moderately active (1.55)
    // BMR = 10*80 + 6.25*175 - 5*30 + 5 = 800 + 1093.75 - 150 + 5 = 1748.75
    // TDEE = 1748.75 * 1.55 = 2710.6 kcal
    // Target = 2710.6 - 500 = 2211 kcal
    var targets =
        NutritionGoalCalculator.calculateTargets(
            30, true, 175.0, 80.0, ActivityLevel.MODERATE, Goal.LOSE_WEIGHT, false);

    assertEquals(1748.8, targets.bmrKcal(), 0.5);
    assertEquals(2710.6, targets.tdeeKcal(), 1.0);
    assertEquals(2211, targets.targetCaloriesKcal(), 2);
    // Protein: 80 * 1.4 = 112g
    assertEquals(112, targets.proteinGrams());
    assertTrue(targets.carbGrams() > 100);
    assertTrue(targets.fatGrams() > 40);
  }

  @Test
  @DisplayName("Verify adolescent safety rule: under-18 user weight loss is blocked to maintenance")
  void testMinorSafetyRule() {
    var targets =
        NutritionGoalCalculator.calculateTargets(
            16, false, 160.0, 55.0, ActivityLevel.LIGHT, Goal.LOSE_WEIGHT, false);

    // Goal adjusted to maintain
    assertTrue(targets.clinicalNotes().contains("Adolescent safety rule"));
    assertEquals(Math.round(targets.tdeeKcal()), targets.targetCaloriesKcal());
  }

  @Test
  @DisplayName("Verify maternal safety rule: pregnancy prohibits caloric deficit")
  void testPregnancySafetyRule() {
    var targets =
        NutritionGoalCalculator.calculateTargets(
            28, false, 165.0, 65.0, ActivityLevel.LIGHT, Goal.LOSE_WEIGHT, true);

    assertTrue(targets.clinicalNotes().contains("Maternal safety rule"));
    assertEquals(Math.round(targets.tdeeKcal()), targets.targetCaloriesKcal());
  }

  @Test
  @DisplayName("Verify clinical floor: calories cannot dip below 1200 kcal for female")
  void testFemaleCalorieFloor() {
    // Low weight/height sedentary female
    var targets =
        NutritionGoalCalculator.calculateTargets(
            50, false, 150.0, 42.0, ActivityLevel.SEDENTARY, Goal.LOSE_WEIGHT, false);

    assertTrue(targets.targetCaloriesKcal() >= 1200);
    assertTrue(targets.clinicalNotes().contains("Clinical calorie floor applied"));
  }
}
