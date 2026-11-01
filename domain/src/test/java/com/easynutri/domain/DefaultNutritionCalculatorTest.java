package com.easynutri.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.easynutri.domain.calculation.DefaultNutritionCalculator;
import com.easynutri.domain.model.FoodCandidate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DefaultNutritionCalculatorTest {

  @Test
  @DisplayName("Verify linear scaling of nutrients from per-100g base values")
  void testNutrientScaling() {
    FoodCandidate food =
        new FoodCandidate(
            "food_moong_dal",
            "Moong Dal",
            "PULSES",
            Map.of(
                "energy_kcal", 348.0,
                "protein_g", 24.5,
                "carbohydrate_g", 59.9,
                "fat_g", 1.2,
                "sodium_mg", 15.0),
            List.of(),
            List.of("VEG"));

    DefaultNutritionCalculator calc = new DefaultNutritionCalculator();
    Map<String, Double> scaled = calc.calculateNutrientsPerGrams(food, 60.0);

    // 60g is 0.6x
    assertEquals(208.8, scaled.get("energy_kcal"), 0.1);
    assertEquals(14.7, scaled.get("protein_g"), 0.1);
    assertEquals(35.94, scaled.get("carbohydrate_g"), 0.1);
    assertEquals(0.72, scaled.get("fat_g"), 0.1);
    assertEquals(9.0, scaled.get("sodium_mg"), 0.1);
  }

  @Test
  @DisplayName("Verify negative gram input triggers validation exception")
  void testNegativeGrams() {
    DefaultNutritionCalculator calc = new DefaultNutritionCalculator();
    FoodCandidate food = new FoodCandidate("f1", "Sample", "VEG", Map.of(), List.of(), List.of());
    assertThrows(
        IllegalArgumentException.class, () -> calc.calculateNutrientsPerGrams(food, -10.0));
  }
}
