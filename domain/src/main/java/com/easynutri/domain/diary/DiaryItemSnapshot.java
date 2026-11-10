package com.easynutri.domain.diary;

/**
 * Immutable historical snapshot of a logged food item or recipe portion. Captures verified
 * nutritional values at the moment of logging so future edits to the food catalog do not alter
 * historical records.
 */
public record DiaryItemSnapshot(
    String foodId,
    String recipeId,
    String itemName,
    double amountG,
    double servings,
    double calories,
    double proteinG,
    double carbsG,
    double fatG,
    double fiberG,
    double sodiumMg) {

  public DiaryItemSnapshot {
    if (itemName == null || itemName.isBlank()) {
      throw new IllegalArgumentException("Item name cannot be null or empty.");
    }
    amountG = Math.max(0.0, amountG);
    servings = Math.max(0.0, servings);
    calories = Math.max(0.0, calories);
    proteinG = Math.max(0.0, proteinG);
    carbsG = Math.max(0.0, carbsG);
    fatG = Math.max(0.0, fatG);
    fiberG = Math.max(0.0, fiberG);
    sodiumMg = Math.max(0.0, sodiumMg);
  }

  /** Scales this snapshot by a factor (e.g. serving adjustment). */
  public DiaryItemSnapshot scale(double factor) {
    if (factor <= 0) {
      return this;
    }
    return new DiaryItemSnapshot(
        this.foodId,
        this.recipeId,
        this.itemName,
        this.amountG * factor,
        this.servings * factor,
        this.calories * factor,
        this.proteinG * factor,
        this.carbsG * factor,
        this.fatG * factor,
        this.fiberG * factor,
        this.sodiumMg * factor);
  }
}
