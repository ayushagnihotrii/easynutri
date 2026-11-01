package com.easynutri.domain.model;

/**
 * Sealed marker interface for any entity verifiable by the EasyNutri safety core. Permitted
 * subtypes must be strongly typed and immutable.
 */
public sealed interface SafetyCandidate permits FoodCandidate, RecipeCandidate {
  String candidateId();

  String candidateName();
}
