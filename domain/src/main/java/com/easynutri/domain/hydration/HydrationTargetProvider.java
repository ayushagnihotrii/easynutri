package com.easynutri.domain.hydration;

import java.util.Set;

/** Contract for determining safe personalized hydration targets. */
public interface HydrationTargetProvider {

  /**
   * Computes personalized fluid intake target based on body weight, activity, and medical
   * conditions.
   *
   * @param weightKg user weight in kg
   * @param workoutMinutes total workout duration today in minutes
   * @param healthConditions set of diagnosed health conditions
   * @param clinicianTargetMl explicit doctor override target in ml (optional, null if none)
   * @return evaluated hydration target result
   */
  HydrationTargetResult calculateTarget(
      double weightKg, int workoutMinutes, Set<String> healthConditions, Integer clinicianTargetMl);
}
