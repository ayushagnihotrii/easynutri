package com.easynutri.dto;

import com.easynutri.domain.model.IngredientCandidate;

public record IngredientCandidateDto(
    String rawInput,
    String canonicalFoodId,
    String displayNameEn,
    String displayNameHi,
    String category,
    double quantity,
    String unit,
    double quantityGrams,
    boolean isLeftover,
    int cookedHoursAgo,
    String storageMethod) {

  public static IngredientCandidateDto fromDomain(IngredientCandidate c) {
    return new IngredientCandidateDto(
        c.rawInput(),
        c.canonicalFoodId(),
        c.displayNameEn(),
        c.displayNameHi(),
        c.category(),
        c.quantity(),
        c.unit(),
        c.quantityGrams(),
        c.isLeftover(),
        c.cookedHoursAgo(),
        c.storageMethod());
  }
}
