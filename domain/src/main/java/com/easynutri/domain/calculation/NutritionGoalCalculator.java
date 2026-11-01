package com.easynutri.domain.calculation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Pure Java clinical calculator for daily calorie goals and macronutrient targets. Adheres to
 * ICMR-NIN and clinical dietetics safety guidelines.
 */
public final class NutritionGoalCalculator {

  public enum ActivityLevel {
    SEDENTARY(1.2, "Sedentary (Desk job, little to no exercise)"),
    LIGHT(1.375, "Lightly Active (Light exercise 1-3 days/week)"),
    MODERATE(1.55, "Moderately Active (Moderate exercise 3-5 days/week)"),
    VERY_ACTIVE(1.725, "Very Active (Hard exercise 6-7 days/week)"),
    EXTRA_ACTIVE(1.9, "Extremely Active (Athletic training/physical labor)");

    private final double factor;
    private final String description;

    ActivityLevel(double factor, String description) {
      this.factor = factor;
      this.description = description;
    }

    public double getFactor() {
      return factor;
    }

    public String getDescription() {
      return description;
    }
  }

  public enum Goal {
    LOSE_WEIGHT("Healthy Weight Loss", -500),
    MAINTAIN("Weight Maintenance", 0),
    GAIN_MUSCLE("Lean Muscle Gain", +300);

    private final String label;
    private final int calorieAdjustment;

    Goal(String label, int calorieAdjustment) {
      this.label = label;
      this.calorieAdjustment = calorieAdjustment;
    }

    public String getLabel() {
      return label;
    }

    public int getCalorieAdjustment() {
      return calorieAdjustment;
    }
  }

  public record NutritionTargets(
      double bmrKcal,
      double tdeeKcal,
      int targetCaloriesKcal,
      int proteinGrams,
      int carbGrams,
      int fatGrams,
      double waterLiters,
      String clinicalNotes) {
    public NutritionTargets {
      Objects.requireNonNull(clinicalNotes, "clinicalNotes cannot be null");
    }
  }

  private static final BurnCalculator BURN_CALCULATOR = new DefaultBurnCalculator();

  private NutritionGoalCalculator() {}

  /**
   * Calculates comprehensive daily caloric and macronutrient targets.
   *
   * @param age age in years
   * @param isMale biological sex for Mifflin-St Jeor equation
   * @param heightCm height in centimeters
   * @param weightKg weight in kilograms
   * @param activityLevel daily physical activity tier
   * @param goal target health direction
   * @param isPregnant pregnancy indicator (prohibits deficit)
   * @return immutable NutritionTargets containing daily targets and safety notes
   */
  public static NutritionTargets calculateTargets(
      int age,
      boolean isMale,
      double heightCm,
      double weightKg,
      ActivityLevel activityLevel,
      Goal goal,
      boolean isPregnant) {

    if (age <= 0 || age > 120) {
      throw new IllegalArgumentException("Age must be between 1 and 120 years.");
    }
    Objects.requireNonNull(activityLevel, "activityLevel cannot be null");
    Objects.requireNonNull(goal, "goal cannot be null");

    double bmr = BURN_CALCULATOR.calculateBmr(weightKg, heightCm, age, isMale);
    double tdee = BURN_CALCULATOR.calculateTdee(bmr, activityLevel.getFactor());

    StringBuilder notes = new StringBuilder();
    Goal effectiveGoal = goal;

    // Safety Rule 1: Minors (<18) must never be assigned caloric deficit targets
    if (age < 18 && goal == Goal.LOSE_WEIGHT) {
      effectiveGoal = Goal.MAINTAIN;
      notes.append(
          "Adolescent safety rule: Caloric deficits are restricted for under-18 users. Target set to maintenance for healthy growth. ");
    }

    // Safety Rule 2: Pregnancy requires nutritional adequacy; caloric restriction prohibited
    if (isPregnant && goal == Goal.LOSE_WEIGHT) {
      effectiveGoal = Goal.MAINTAIN;
      notes.append(
          "Maternal safety rule: Caloric restriction is prohibited during pregnancy. Target set to maintenance with nutrient density focus. ");
    }

    int rawTarget = (int) Math.round(tdee + effectiveGoal.getCalorieAdjustment());

    // Safety Rule 3: Clinical minimum safety floors (1200 kcal female, 1500 kcal male)
    int safetyFloor = isMale ? 1500 : 1200;
    int finalCalories = rawTarget;
    if (finalCalories < safetyFloor) {
      finalCalories = safetyFloor;
      notes.append(
          String.format(
              "Clinical calorie floor applied: Calorie target adjusted to safe physiological minimum of %d kcal/day. ",
              safetyFloor));
    }

    if (notes.isEmpty()) {
      notes.append(
          String.format(
              "Calculated based on Mifflin-St Jeor TDEE with %s adjustment.",
              effectiveGoal.getLabel()));
    }

    // Macro Target Derivation
    // Protein: LOSE_WEIGHT -> 1.4 g/kg, GAIN_MUSCLE -> 1.8 g/kg, MAINTAIN -> 1.1 g/kg
    double proteinMultiplier =
        switch (effectiveGoal) {
          case LOSE_WEIGHT -> 1.4;
          case GAIN_MUSCLE -> 1.8;
          case MAINTAIN -> 1.1;
        };
    int targetProteinGrams = (int) Math.round(weightKg * proteinMultiplier);
    int proteinCalories = targetProteinGrams * 4;

    // Fat: 25% of total calories (9 kcal/g)
    int fatCalories = (int) Math.round(finalCalories * 0.25);
    int targetFatGrams = Math.max(30, (int) Math.round(fatCalories / 9.0));

    // Carbs: Remaining calories (4 kcal/g)
    int remainingCalories = finalCalories - proteinCalories - (targetFatGrams * 9);
    int targetCarbGrams = Math.max(50, (int) Math.round(remainingCalories / 4.0));

    // Hydration Target: ~35ml per kg body weight, clamped between 2.0L and 4.0L
    double rawWater = (weightKg * 35.0) / 1000.0;
    double clampedWater = Math.max(2.0, Math.min(4.0, rawWater));
    double waterLiters =
        BigDecimal.valueOf(clampedWater).setScale(1, RoundingMode.HALF_UP).doubleValue();

    return new NutritionTargets(
        BigDecimal.valueOf(bmr).setScale(1, RoundingMode.HALF_UP).doubleValue(),
        BigDecimal.valueOf(tdee).setScale(1, RoundingMode.HALF_UP).doubleValue(),
        finalCalories,
        targetProteinGrams,
        targetCarbGrams,
        targetFatGrams,
        waterLiters,
        notes.toString().trim());
  }
}
