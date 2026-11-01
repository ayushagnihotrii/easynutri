package com.easynutri.domain.nutrition;

import com.easynutri.domain.model.FoodCandidate;
import com.easynutri.domain.model.ProfileContext;
import com.easynutri.domain.model.RecipeCandidate;
import com.easynutri.domain.model.SafetyCandidate;
import com.easynutri.domain.rules.SafetyResult;

/**
 * Defensive safety facade through which every candidate must pass before reaching any UI or client
 * response.
 */
public interface SafetyGate {
  SafetyResult verifyFood(ProfileContext profile, FoodCandidate candidate);

  SafetyResult verifyRecipe(ProfileContext profile, RecipeCandidate candidate);

  SafetyResult verify(ProfileContext profile, SafetyCandidate candidate);
}
