package com.easynutri.domain.calculation;

/** Default pure Java implementation of clinical BMR and expenditure calculations. */
public final class DefaultBurnCalculator implements BurnCalculator {

  @Override
  public double calculateBmr(double weightKg, double heightCm, int ageYears, boolean isMale) {
    if (weightKg <= 0 || heightCm <= 0 || ageYears <= 0) {
      throw new IllegalArgumentException(
          "Weight, height, and age must be strictly positive values.");
    }
    // Mifflin-St Jeor Equation:
    // Men: 10 * weight(kg) + 6.25 * height(cm) - 5 * age(y) + 5
    // Women: 10 * weight(kg) + 6.25 * height(cm) - 5 * age(y) - 161
    double base = (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * ageYears);
    return isMale ? base + 5.0 : base - 161.0;
  }

  @Override
  public double calculateTdee(double bmr, double activityFactor) {
    if (bmr <= 0) {
      throw new IllegalArgumentException("BMR must be strictly positive.");
    }
    double factor = Math.max(1.1, Math.min(2.4, activityFactor));
    return Math.round(bmr * factor * 10.0) / 10.0;
  }

  @Override
  public double calculateActivityBurn(double met, double durationMinutes, double weightKg) {
    if (met <= 0 || durationMinutes <= 0 || weightKg <= 0) {
      return 0.0;
    }
    // Calories burned = MET * 3.5 * weight(kg) / 200 * duration(minutes)
    double burned = (met * 3.5 * weightKg / 200.0) * durationMinutes;
    return Math.round(burned * 10.0) / 10.0;
  }

  @Override
  public double estimateStepBurn(long steps, double weightKg) {
    if (steps <= 0 || weightKg <= 0) {
      return 0.0;
    }
    // Average calorie burn per step ~ 0.04 kcal for a 70kg person, scaled linearly
    double burnPerStep = 0.04 * (weightKg / 70.0);
    return Math.round(steps * burnPerStep * 10.0) / 10.0;
  }
}
