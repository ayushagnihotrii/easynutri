package com.easynutri.domain.rules;

import com.easynutri.domain.model.FoodCandidate;
import com.easynutri.domain.model.ProfileContext;
import com.easynutri.domain.model.RecipeCandidate;
import com.easynutri.domain.model.SafetyCandidate;

/**
 * Core interface for evaluating candidates against health conditions, dietary types, and allergies.
 */
public interface RulesEngine {
  SafetyResult evaluateFood(ProfileContext profile, FoodCandidate candidate);

  SafetyResult evaluateRecipe(ProfileContext profile, RecipeCandidate candidate);

  SafetyResult evaluate(ProfileContext profile, SafetyCandidate candidate);
}
