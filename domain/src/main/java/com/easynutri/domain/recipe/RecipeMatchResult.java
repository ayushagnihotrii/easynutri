package com.easynutri.domain.recipe;

import com.easynutri.domain.model.RecipeCandidate;
import java.util.List;
import java.util.Objects;

/** Detailed match evaluation result for an individual recipe against user-provided ingredients. */
public record RecipeMatchResult(
    RecipeCandidate candidate,
    double matchScore,
    RecipeMatchLabel matchLabel,
    List<String> matchedIngredients,
    List<String> missingIngredients,
    List<String> suggestedSwaps,
    boolean usesLeftover) {

  public RecipeMatchResult {
    Objects.requireNonNull(candidate, "candidate cannot be null");
    Objects.requireNonNull(matchLabel, "matchLabel cannot be null");
    matchedIngredients = List.copyOf(matchedIngredients != null ? matchedIngredients : List.of());
    missingIngredients = List.copyOf(missingIngredients != null ? missingIngredients : List.of());
    suggestedSwaps = List.copyOf(suggestedSwaps != null ? suggestedSwaps : List.of());
  }
}
