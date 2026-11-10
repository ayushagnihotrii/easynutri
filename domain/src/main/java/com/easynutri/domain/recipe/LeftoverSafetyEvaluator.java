package com.easynutri.domain.recipe;

import com.easynutri.domain.rules.RuleVerdict;
import com.easynutri.domain.rules.SafetyResult;
import java.util.List;
import java.util.Locale;

/**
 * Pure Java food-safety evaluation engine for cooked leftovers based on ICMR-NIN and FSSAI
 * guidelines.
 */
public final class LeftoverSafetyEvaluator {

  private LeftoverSafetyEvaluator() {}

  /**
   * Evaluates the microbiological safety of cooked leftovers based on storage temperature and time.
   *
   * @param foodName name of the cooked ingredient
   * @param hoursAgo approximate hours since cooking
   * @param storageMethod storage condition ("REFRIGERATED", "ROOM_TEMP", "FROZEN")
   * @return SafetyResult with ALLOW, WARN, or BLOCK verdict and clinical rationale
   */
  public static SafetyResult evaluateLeftoverSafety(
      String foodName, int hoursAgo, String storageMethod) {

    String method =
        storageMethod != null ? storageMethod.trim().toUpperCase(Locale.ROOT) : "ROOM_TEMP";

    if (method.contains("ROOM") || method.contains("AMBIENT")) {
      if (hoursAgo > 4) {
        return new SafetyResult(
            RuleVerdict.BLOCK,
            List.of(
                String.format(
                    "Food Safety Hazard: Cooked '%s' left at room temperature for %d hours exceeds safe bacterial boundaries. Reheating does not neutralize heat-stable enterotoxins (e.g. Bacillus cereus). Please discard.",
                    foodName, hoursAgo)),
            List.of("Use freshly prepared or refrigerated ingredients."));
      } else if (hoursAgo > 2) {
        return new SafetyResult(
            RuleVerdict.WARN,
            List.of(
                String.format(
                    "Food Safety Notice: Cooked '%s' has been at ambient temperature for %d hours. Reheat thoroughly to a rolling boil or >= 75°C immediately before consuming.",
                    foodName, hoursAgo)),
            List.of());
      }
      return SafetyResult.allow();
    }

    if (method.contains("REFRIG")) {
      if (hoursAgo > 72) {
        return new SafetyResult(
            RuleVerdict.BLOCK,
            List.of(
                String.format(
                    "Storage Limit Exceeded: Cooked '%s' refrigerated for over %d hours (> 3 days) carries increased risk of spoilage and psychrotrophic bacterial proliferation. Consumption not advised.",
                    foodName, hoursAgo)),
            List.of("Prepare with fresh stock."));
      } else if (hoursAgo > 48) {
        return new SafetyResult(
            RuleVerdict.WARN,
            List.of(
                String.format(
                    "Extended Refrigeration: Cooked '%s' is 2-3 days old (%d hours). Verify freshness, aroma, and reheat thoroughly until steaming hot (>= 75°C).",
                    foodName, hoursAgo)),
            List.of());
      }
      return SafetyResult.allow();
    }

    if (method.contains("FROZEN") || method.contains("FREEZER")) {
      if (hoursAgo > 720) { // > 30 days
        return new SafetyResult(
            RuleVerdict.WARN,
            List.of("Freezer burn and textural degradation may affect quality after 30 days."),
            List.of());
      }
      return SafetyResult.allow();
    }

    return SafetyResult.allow();
  }
}
