package com.easynutri.core.display;

/**
 * Strategy interface governing the formatting and display of nutritional numbers. Acts as the
 * foundation for Sensitive-User Mode (Feature 33), allowing suppression of calories, weight, BMI,
 * and macro amounts for users in eating disorder recovery.
 */
public interface DisplayPolicy {
  boolean shouldDisplayNutritionalNumbers();

  String formatCalories(double calories);

  String formatWeight(double weightKg);

  String formatMacro(double grams, String unitLabel);
}
