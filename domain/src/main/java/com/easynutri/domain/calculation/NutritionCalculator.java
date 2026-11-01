package com.easynutri.domain.calculation;

import com.easynutri.domain.model.FoodCandidate;
import java.util.Map;

/**
 * Deterministic nutrition calculation interface. Calculates exact nutrient amounts from verified
 * database rows.
 */
public interface NutritionCalculator {
  Map<String, Double> calculateNutrientsPerGrams(FoodCandidate food, double grams);
}
