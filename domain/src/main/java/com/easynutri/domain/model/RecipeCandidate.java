package com.easynutri.domain.model;

import java.util.List;
import java.util.Objects;

/** Immutable recipe candidate representation for domain-level safety and rule evaluation. */
public record RecipeCandidate(
    String candidateId,
    String candidateName,
    List<String> ingredientFoodIds,
    String mealSlot,
    String mealRole,
    List<String> derivedTags)
    implements SafetyCandidate {

  public RecipeCandidate {
    Objects.requireNonNull(candidateId, "candidateId cannot be null");
    Objects.requireNonNull(candidateName, "candidateName cannot be null");
    ingredientFoodIds = List.copyOf(ingredientFoodIds != null ? ingredientFoodIds : List.of());
    derivedTags = List.copyOf(derivedTags != null ? derivedTags : List.of());
  }
}
