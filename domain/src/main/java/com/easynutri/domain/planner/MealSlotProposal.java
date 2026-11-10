package com.easynutri.domain.planner;

import com.easynutri.domain.model.RecipeCandidate;
import java.util.Objects;

/**
 * Proposed meal candidate scheduled for a specific day and slot, with clinical selection rationale.
 */
public record MealSlotProposal(
    RecipeCandidate recipe,
    String mealSlot,
    int servings,
    String selectionReason,
    boolean locked,
    boolean completed) {

  public MealSlotProposal(
      RecipeCandidate recipe,
      String mealSlot,
      int servings,
      String selectionReason,
      boolean locked) {
    this(recipe, mealSlot, servings, selectionReason, locked, false);
  }

  public MealSlotProposal {
    Objects.requireNonNull(recipe, "recipe cannot be null");
    Objects.requireNonNull(mealSlot, "mealSlot cannot be null");
    selectionReason =
        selectionReason != null && !selectionReason.isBlank()
            ? selectionReason
            : "Balanced recipe matching your preferences";
    servings = Math.max(1, servings);
  }

  public String slotName() {
    return mealSlot();
  }

  public static MealSlotProposal of(RecipeCandidate recipe, String mealSlot, String reason) {
    return new MealSlotProposal(recipe, mealSlot, 2, reason, false, false);
  }

  public static MealSlotProposal locked(RecipeCandidate recipe, String mealSlot, int servings) {
    return new MealSlotProposal(
        recipe, mealSlot, servings, "User locked meal selection", true, false);
  }
}
