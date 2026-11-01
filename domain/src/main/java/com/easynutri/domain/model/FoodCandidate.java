package com.easynutri.domain.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Immutable food candidate representation for domain-level safety and rule evaluation. */
public record FoodCandidate(
    String candidateId,
    String candidateName,
    String category,
    Map<String, Double> nutrientsPer100g,
    List<String> allergenTags,
    List<String> dietaryFlags)
    implements SafetyCandidate {

  public FoodCandidate {
    Objects.requireNonNull(candidateId, "candidateId cannot be null");
    Objects.requireNonNull(candidateName, "candidateName cannot be null");
    nutrientsPer100g = Map.copyOf(nutrientsPer100g != null ? nutrientsPer100g : Map.of());
    allergenTags = List.copyOf(allergenTags != null ? allergenTags : List.of());
    dietaryFlags = List.copyOf(dietaryFlags != null ? dietaryFlags : List.of());
  }
}
