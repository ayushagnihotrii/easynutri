package com.easynutri.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.easynutri.domain.activity.ActivityBurnEngine;
import com.easynutri.domain.activity.ActivityIntensity;
import com.easynutri.domain.activity.ActivityType;
import com.easynutri.domain.activity.DailyEnergyBalance;
import org.junit.jupiter.api.Test;

class ActivityBurnEngineTest {

  @Test
  void shouldCalculateWorkoutBurnByMet() {
    // 70 kg person running moderate intensity for 30 minutes
    // MET = 8.0 * 1.0 = 8.0
    // burn = (8.0 * 3.5 * 70 / 200) * 30 = 9.8 * 30 = 294 kcal
    double burned =
        ActivityBurnEngine.calculateWorkoutBurn(
            ActivityType.RUNNING, ActivityIntensity.MODERATE, 30, 70.0);
    assertEquals(294.0, burned);
  }

  @Test
  void shouldEstimateStepsAndDeduplicateExpenditure() {
    // 30 min brisk walking => ~3150 steps estimated
    long estimatedSteps = ActivityBurnEngine.estimateWorkoutSteps(ActivityType.WALKING, 30);
    assertEquals(3150, estimatedSteps);

    // Total steps 10,000, workout accounted for 3,150 => 6,850 net steps
    // 6,850 * 0.04 * (70/70) = 274.0 kcal
    double netStepBurn = ActivityBurnEngine.calculateNetStepBurn(10000, estimatedSteps, 70.0);
    assertEquals(274.0, netStepBurn);
  }

  @Test
  void shouldComputeDailyEnergyBalanceCorrectly() {
    // Intake = 2000 kcal, BMR = 1500 kcal, Workouts = 300 kcal, Steps = 200 kcal
    // Total burn = 2000 kcal => net 0
    DailyEnergyBalance balance = DailyEnergyBalance.compute(2000.0, 1500.0, 300.0, 200.0);

    assertEquals(2000.0, balance.totalBurnKcal());
    assertEquals(0.0, balance.netBalanceKcal());
    assertEquals("Balanced Maintenance", balance.statusDescription());
  }
}
