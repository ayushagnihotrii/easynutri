package com.easynutri.domain.calculation;

/** Contract for calculating Basal Metabolic Rate (BMR), TDEE, and activity energy burn. */
public interface BurnCalculator {

  /**
   * Calculates Basal Metabolic Rate using the Mifflin-St Jeor equation.
   *
   * @param weightKg weight in kilograms
   * @param heightCm height in centimeters
   * @param ageYears age in years
   * @param isMale true if biological male, false if female
   * @return basal energy expenditure in kcal/day
   */
  double calculateBmr(double weightKg, double heightCm, int ageYears, boolean isMale);

  /**
   * Calculates Total Daily Energy Expenditure (TDEE) based on activity multiplier.
   *
   * @param bmr basal metabolic rate
   * @param activityFactor multiplier (1.2 sedentary to 1.9 heavy activity)
   * @return daily expenditure in kcal
   */
  double calculateTdee(double bmr, double activityFactor);

  /**
   * Estimates energy burned for physical activity by Metabolic Equivalent of Task (MET).
   *
   * @param met Metabolic Equivalent of Task
   * @param durationMinutes activity duration in minutes
   * @param weightKg body weight in kilograms
   * @return estimated calories burned in kcal
   */
  double calculateActivityBurn(double met, double durationMinutes, double weightKg);

  /**
   * Estimates energy burned from step count.
   *
   * @param steps recorded step count
   * @param weightKg body weight in kilograms
   * @return estimated calories burned in kcal
   */
  double estimateStepBurn(long steps, double weightKg);
}
