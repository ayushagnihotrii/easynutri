package com.easynutri.domain.hydration;

import java.util.Locale;
import java.util.Set;

/** Clinical implementation of hydration target calculations. */
public final class DefaultHydrationTargetProvider implements HydrationTargetProvider {

  private static final int DEFAULT_TARGET_ML = 2500;
  private static final int PROVISIONAL_FLUID_ADVISORY_CAP_ML = 1500;

  @Override
  public HydrationTargetResult calculateTarget(
      double weightKg,
      int workoutMinutes,
      Set<String> healthConditions,
      Integer clinicianTargetMl) {

    // 1. Explicit clinician-reviewed target takes highest clinical precedence
    if (clinicianTargetMl != null && clinicianTargetMl > 0) {
      return new HydrationTargetResult(
          clinicianTargetMl,
          true,
          "Target established by certified healthcare provider.",
          "Strictly adhere to your clinical fluid prescription.");
    }

    // 2. Check clinical conditions requiring individualized fluid restriction (CHF, Renal failure)
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
              "Cardiovascular / Heart failure condition detected. Fluid restriction must be personalized by your cardiologist.";
          break;
        } else if (normalized.contains("kidney")
            || normalized.contains("ckd")
            || normalized.contains("renal")) {
          hasRestrictionCondition = true;
          restrictionReason =
              "Renal / Kidney disease detected. Fluid allowance varies by clinical stage and dialysis status.";
          break;
        }
      }
    }

    if (hasRestrictionCondition) {
      return new HydrationTargetResult(
          PROVISIONAL_FLUID_ADVISORY_CAP_ML,
          true,
          restrictionReason,
          "Universal fluid targets cannot be safely assumed for cardiovascular or renal conditions. Consult your nephrologist or cardiologist for a verified daily fluid target.");
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
