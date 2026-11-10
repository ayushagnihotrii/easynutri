package com.easynutri.domain.policy;

/**
 * Enforces privacy, eating-disorder safety, and user preference policies. When sensitive-user mode
 * is active, raw caloric values, exact body weights, and obsessive numerical metrics are suppressed
 * or converted to qualitative indicators.
 */
public final class DisplayPolicy {

  private DisplayPolicy() {}

  /**
   * Formats calories according to sensitive mode policy.
   *
   * @param calories raw caloric value
   * @param sensitiveMode true if sensitive mode is enabled
   * @return formatted string or masked placeholder
   */
  public static String formatCalories(double calories, boolean sensitiveMode) {
    if (sensitiveMode) {
      return "Balanced";
    }
    return Math.round(calories) + " kcal";
  }

  /**
   * Formats weight according to sensitive mode policy.
   *
   * @param weightKg weight in kilograms
   * @param sensitiveMode true if sensitive mode is enabled
   * @return formatted string or masked placeholder
   */
  public static String formatWeight(double weightKg, boolean sensitiveMode) {
    if (sensitiveMode) {
      return "-- kg (Masked)";
    }
    return String.format(java.util.Locale.US, "%.1f kg", weightKg);
  }

  /**
   * Formats a macronutrient amount in grams.
   *
   * @param grams macro amount in grams
   * @param sensitiveMode true if sensitive mode is enabled
   * @return formatted string or masked placeholder
   */
  public static String formatMacroGrams(double grams, boolean sensitiveMode) {
    if (sensitiveMode) {
      return "Nourishing";
    }
    return Math.round(grams) + "g";
  }

  /**
   * Evaluates energy balance qualitatively when sensitive mode is enabled.
   *
   * @param netKcal net energy balance (intake - expenditure)
   * @param sensitiveMode true if sensitive mode is active
   * @return descriptive message
   */
  public static String formatEnergyBalance(double netKcal, boolean sensitiveMode) {
    if (sensitiveMode) {
      if (Math.abs(netKcal) < 200) {
        return "Well Balanced";
      } else if (netKcal > 0) {
        return "Sustained Energy";
      } else {
        return "Active Deficit";
      }
    }
    if (Math.abs(netKcal) < 100) {
      return "Neutral Balance (0 kcal)";
    } else if (netKcal > 0) {
      return "+" + Math.round(netKcal) + " kcal Surplus";
    } else {
      return Math.round(netKcal) + " kcal Deficit";
    }
  }
}
