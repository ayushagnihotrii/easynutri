package com.easynutri.core.display;

import java.util.Locale;

/** Standard pass-through display policy formatting exact numerical metrics. */
public class DefaultDisplayPolicy implements DisplayPolicy {

  @Override
  public boolean shouldDisplayNutritionalNumbers() {
    return true;
  }

  @Override
  public String formatCalories(double calories) {
    return String.format(Locale.getDefault(), "%,.0f kcal", calories);
  }

  @Override
  public String formatWeight(double weightKg) {
    return String.format(Locale.getDefault(), "%.1f kg", weightKg);
  }

  @Override
  public String formatMacro(double grams, String unitLabel) {
    return String.format(Locale.getDefault(), "%.1f %s", grams, unitLabel);
  }
}
