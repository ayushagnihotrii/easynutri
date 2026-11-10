package com.easynutri.domain.nutrition;

import com.easynutri.domain.model.FoodCandidate;
import com.easynutri.domain.model.ProfileContext;
import com.easynutri.domain.model.RecipeCandidate;
import com.easynutri.domain.model.SafetyCandidate;
import com.easynutri.domain.rules.DefaultRulesEngine;
import com.easynutri.domain.rules.RulesEngine;
import com.easynutri.domain.rules.SafetyResult;
import java.util.Objects;

/**
 * Defensive safety gate intercepting all candidates before UI or AI presentation. Enforces zero
 * bypass of allergen blocks and medical condition constraints.
 */
public final class DefaultSafetyGate implements SafetyGate {

  private final RulesEngine rulesEngine;

  public DefaultSafetyGate(RulesEngine rulesEngine) {
    this.rulesEngine = rulesEngine != null ? rulesEngine : new DefaultRulesEngine();
  }

  public DefaultSafetyGate() {
    this(new DefaultRulesEngine());
  }

  @Override
  public SafetyResult verifyFood(ProfileContext profile, FoodCandidate candidate) {
    Objects.requireNonNull(profile, "profile cannot be null");
    Objects.requireNonNull(candidate, "candidate cannot be null");
    return rulesEngine.evaluateFood(profile, candidate);
  }

  @Override
  public SafetyResult verifyRecipe(ProfileContext profile, RecipeCandidate candidate) {
    Objects.requireNonNull(profile, "profile cannot be null");
    Objects.requireNonNull(candidate, "candidate cannot be null");
    return rulesEngine.evaluateRecipe(profile, candidate);
  }

  @Override
  public SafetyResult verify(ProfileContext profile, SafetyCandidate candidate) {
    Objects.requireNonNull(profile, "profile cannot be null");
    Objects.requireNonNull(candidate, "candidate cannot be null");
    return rulesEngine.evaluate(profile, candidate);
  }
}
