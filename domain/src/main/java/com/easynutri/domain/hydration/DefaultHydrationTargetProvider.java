package com.easynutri.domain.hydration;

import java.util.Locale;
import java.util.Set;

/** Clinical implementation of hydration target calculations. */
public final class DefaultHydrationTargetProvider implements HydrationTargetProvider {

  private static final int DEFAULT_TARGET_ML = 2500;
  private static final int CLINICAL_FLUID_RESTRICTION_CAP_ML = 1500;

  @Override
  public HydrationTargetResult calculateTarget(
      double weightKg,
      int workoutMinutes,
      Set<String> healthConditions,
      Integer clinicianTargetMl) {

    // 1. Explicit clinician override takes highest precedence
    if (clinicianTargetMl != null && clinicianTargetMl > 0) {
      return new HydrationTargetResult(
          clinicianTargetMl,
          true,
          "Target established by healthcare provider.",
          "Follow clinical guidance precisely.");
    }

    // 2. Check clinical conditions requiring fluid restriction (CHF, Renal failure)
    boolean hasRestrictionCondition = false;
    String restrictionReason = null;

    if (healthConditions != null) {
      for (String condition : healthConditions) {
        String normalized = condition.toLowerCase(Locale.ROOT);
        if (normalized.contains("heart_failure")
            || normalized.contains("chf")
            || normalized.contains("cardiac")) {
          hasRestrictionCondition = true;
          restrictionReason =
              "Cardiovascular / Heart condition detected. Fluid intake is clinically restricted to prevent fluid overload.";
          break;
        } else if (normalized.contains("kidney")
            || normalized.contains("ckd")
            || normalized.contains("renal")) {
          hasRestrictionCondition = true;
          restrictionReason =
              "Renal / Kidney condition detected. Strict fluid restriction applies to reduce filtration stress.";
          break;
        }
      }
    }

    if (hasRestrictionCondition) {
      return new HydrationTargetResult(
          CLINICAL_FLUID_RESTRICTION_CAP_ML,
          true,
          restrictionReason,
          "Clinical limit: 1,500 ml/day. Consult your nephrologist or cardiologist for an exact allowance.");
    }

    // 3. Standard metabolic calculation: ~35 ml per kg of body weight
    double validWeight = weightKg > 30 ? weightKg : 65.0;
    int baseTarget = (int) Math.round(validWeight * 35.0);

    // Activity hydration supplement: ~300 ml per 30 minutes of workout
    int activitySupplement = (Math.max(0, workoutMinutes) / 30) * 300;

    int totalTarget = Math.max(1800, Math.min(4500, baseTarget + activitySupplement));

    return new HydrationTargetResult(
        totalTarget,
        false,
        null,
        "Optimal daily hydration based on "
            + Math.round(validWeight)
            + " kg body mass and activity level.");
  }
}
