package com.easynutri.domain.model;

import java.util.List;
import java.util.Objects;

/** Immutable profile context used by domain rules, safety gates, and nutrition engines. */
public record ProfileContext(
    String profileId,
    int age,
    String gender,
    double heightCm,
    double weightKg,
    String dietType,
    List<String> allergies,
    List<String> healthConditions) {
  public ProfileContext {
    Objects.requireNonNull(profileId, "profileId cannot be null");
    allergies = List.copyOf(allergies != null ? allergies : List.of());
    healthConditions = List.copyOf(healthConditions != null ? healthConditions : List.of());
  }
}
