package com.easynutri.domain.activity;

/**
 * Pure Java engine calculating physical activity burn and deduplicating step expenditure from
 * logged ambulatory workouts (e.g. running, walking) to avoid double counting active energy.
 */
public final class ActivityBurnEngine {

  private ActivityBurnEngine() {}

  /**
   * Calculates caloric expenditure for a structured workout using clinical MET formulas.
   *
   * @param activityType activity category
   * @param intensity intensity level
   * @param durationMinutes duration in minutes
   * @param weightKg user weight in kg
   * @return estimated calories burned in kcal
   */
  public static double calculateWorkoutBurn(
      ActivityType activityType,
      ActivityIntensity intensity,
      int durationMinutes,
      double weightKg) {
    if (activityType == null || intensity == null || durationMinutes <= 0 || weightKg <= 0) {
      return 0.0;
    }
    double effectiveMet = activityType.getBaseMet() * intensity.getMetMultiplier();
    // Formula: MET * 3.5 * weight(kg) / 200 * duration(minutes)
    double burned = (effectiveMet * 3.5 * weightKg / 200.0) * durationMinutes;
    return Math.round(burned * 10.0) / 10.0;
  }

  /**
   * Estimates how many steps were likely embedded within an ambulatory workout.
   *
   * @param activityType activity category
   * @param durationMinutes duration in minutes
   * @return estimated ambulatory steps
   */
  public static long estimateWorkoutSteps(ActivityType activityType, int durationMinutes) {
    if (activityType == null || durationMinutes <= 0) {
      return 0;
    }
    return switch (activityType) {
      case WALKING -> (long) (durationMinutes * 105);
      case RUNNING -> (long) (durationMinutes * 150);
      case DANCING -> (long) (durationMinutes * 110);
      case SPORTS -> (long) (durationMinutes * 100);
      default -> 0L; // Cycling, Swimming, Yoga, Strength Training do not represent standard steps
    };
  }

  /**
   * Deduplicates total recorded steps by subtracting estimated steps from logged workouts,
   * returning the net non-workout steps and resulting non-workout step burn.
   *
   * @param totalSteps total pedometer / manual steps recorded
   * @param workoutStepEstimate total estimated steps accounted for in logged workouts
   * @param weightKg user weight in kg
   * @return net step burn in kcal
   */
  public static double calculateNetStepBurn(
      long totalSteps, long workoutStepEstimate, double weightKg) {
    if (totalSteps <= 0 || weightKg <= 0) {
      return 0.0;
    }
    long netSteps = Math.max(0, totalSteps - Math.max(0, workoutStepEstimate));
    // Average calorie burn per step ~ 0.04 kcal for a 70kg person, scaled linearly
    double burnPerStep = 0.04 * (weightKg / 70.0);
    return Math.round(netSteps * burnPerStep * 10.0) / 10.0;
  }
}
