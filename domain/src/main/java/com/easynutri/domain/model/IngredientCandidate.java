package com.easynutri.domain.model;

import java.util.Objects;

/**
 * Common immutable domain representation of an ingredient candidate extracted from any input method
 * (typed, voice, photo, barcode, OCR, or leftover).
 */
public record IngredientCandidate(
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

  public IngredientCandidate {
    Objects.requireNonNull(rawInput, "rawInput cannot be null");
    displayNameEn = displayNameEn != null ? displayNameEn : rawInput;
    displayNameHi = displayNameHi != null ? displayNameHi : "";
    category = category != null ? category : "GENERAL";
    unit = unit != null && !unit.isBlank() ? unit : "g";
    storageMethod = storageMethod != null ? storageMethod : "REFRIGERATED";
    if (quantityGrams <= 0.0 && quantity > 0.0) {
      quantityGrams = quantity;
    }
  }

  public static IngredientCandidate of(
      String rawInput, String foodId, String nameEn, String category) {
    return new IngredientCandidate(
        rawInput, foodId, nameEn, "", category, 100.0, "g", 100.0, false, 0, "FRESH");
  }

  public static IngredientCandidate leftover(
      String rawInput, String foodId, String nameEn, double grams, int hoursAgo, String storage) {
    return new IngredientCandidate(
        rawInput, foodId, nameEn, "", "LEFTOVER", grams, "g", grams, true, hoursAgo, storage);
  }
}
