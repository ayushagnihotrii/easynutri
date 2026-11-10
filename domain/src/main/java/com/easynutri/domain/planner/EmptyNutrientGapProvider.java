package com.easynutri.domain.planner;

import com.easynutri.domain.model.ProfileContext;
import java.util.Set;

/**
 * Default empty nutrient gap provider that never fabricates artificial nutritional deficiencies.
 */
public final class EmptyNutrientGapProvider implements NutrientGapProvider {

  @Override
  public Set<String> getNutrientGaps(ProfileContext profile) {
    return Set.of();
  }
}
