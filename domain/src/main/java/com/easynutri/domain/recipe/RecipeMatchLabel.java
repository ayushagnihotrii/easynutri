package com.easynutri.domain.recipe;

/** Result classification labels for ingredient-based recipe matching. */
public enum RecipeMatchLabel {
  READY_NOW("Ready Now", "badge-mint"),
  READY_WITH_SWAP("Ready With a Swap", "badge-primary"),
  MISSING_ONE("Missing 1 Ingredient", "badge-orange"),
  MISSING_TWO("Missing 2 Ingredients", "badge-orange"),
  MISSING_MANY("Missing 3+ Ingredients", "badge-outline");

  private final String displayName;
  private final String badgeClass;

  RecipeMatchLabel(String displayName, String badgeClass) {
    this.displayName = displayName;
    this.badgeClass = badgeClass;
  }

  public String displayName() {
    return displayName;
  }

  public String badgeClass() {
    return badgeClass;
  }
}
