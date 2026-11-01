package com.easynutri.domain.nutrition;

import com.easynutri.domain.model.FoodCandidate;
import com.easynutri.domain.model.ProfileContext;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Contract for identifying micronutrient deficiencies and suggesting nutrient-dense foods. */
public interface DeficiencyService {

  /**
   * Identifies nutrients where logged intake falls below recommended daily allowances.
   *
   * @param dailyIntake logged nutrient totals for the evaluation period
   * @param profile user's demographic profile and health conditions
   * @return set of deficient nutrient keys (e.g., "iron", "calcium", "vitamin_b12")
   */
  Set<String> detectDeficiencies(Map<String, Double> dailyIntake, ProfileContext profile);

  /**
   * Recommends safe candidate foods specifically rich in the specified target nutrient.
   *
   * @param targetNutrient nutrient key to boost
   * @param candidateFoods available food catalog
   * @param profile user profile for allergy and health rule filtering
   * @param limit maximum recommendations to return
   * @return safe, ranked food candidates
   */
  List<FoodCandidate> findFoodsRichIn(
      String targetNutrient, List<FoodCandidate> candidateFoods, ProfileContext profile, int limit);
}
