package com.easynutri.domain.diary;

/** Standardized meal categories for the daily food diary. */
public enum MealSlot {
  BREAKFAST("Breakfast", "🌅", 1),
  LUNCH("Lunch", "☀️", 2),
  DINNER("Dinner", "🌙", 3),
  SNACK("Snacks & Bites", "🍎", 4);

  private final String displayName;
  private final String icon;
  private final int displayOrder;

  MealSlot(String displayName, String icon, int displayOrder) {
    this.displayName = displayName;
    this.icon = icon;
    this.displayOrder = displayOrder;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getIcon() {
    return icon;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public static MealSlot fromString(String value) {
    if (value == null || value.isBlank()) {
      return BREAKFAST;
    }
    try {
      return MealSlot.valueOf(value.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      return BREAKFAST;
    }
  }
}
