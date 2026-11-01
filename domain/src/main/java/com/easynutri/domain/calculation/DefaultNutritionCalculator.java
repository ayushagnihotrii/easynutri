package com.easynutri.domain.calculation;

import com.easynutri.domain.model.FoodCandidate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Deterministic pure Java implementation of nutrient scaling based on canonical per-100g database
 * values.
 */
public final class DefaultNutritionCalculator implements NutritionCalculator {

  @Override
  public Map<String, Double> calculateNutrientsPerGrams(FoodCandidate food, double grams) {
    Objects.requireNonNull(food, "food cannot be null");
    if (grams < 0.0) {
      throw new IllegalArgumentException("Grams cannot be negative: " + grams);
    }

    double ratio = grams / 100.0;
    Map<String, Double> baseNutrients = food.nutrientsPer100g();
    Map<String, Double> scaled = new HashMap<>(baseNutrients.size());

    for (Map.Entry<String, Double> entry : baseNutrients.entrySet()) {
      if (entry.getValue() != null) {
        double scaledVal = entry.getValue() * ratio;
        double rounded =
            BigDecimal.valueOf(scaledVal).setScale(2, RoundingMode.HALF_UP).doubleValue();
        scaled.put(entry.getKey(), rounded);
      }
    }

    return Map.copyOf(scaled);
  }
}
