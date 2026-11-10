package com.easynutri.domain.activity;

/**
 * Encapsulates the daily energy balance: Intake (Food Diary) vs Total Expenditure (BMR baseline +
 * Active Workouts + Net Steps).
 */
public record DailyEnergyBalance(
    double foodIntakeKcal,
    double bmrKcal,
    double workoutBurnKcal,
    double netStepBurnKcal,
    double totalBurnKcal,
    double netBalanceKcal,
    String statusDescription) {

  public static DailyEnergyBalance compute(
      double foodIntakeKcal, double bmrKcal, double workoutBurnKcal, double netStepBurnKcal) {
    double totalBurn = bmrKcal + workoutBurnKcal + netStepBurnKcal;
    double net = foodIntakeKcal - totalBurn;

    String status;
    if (Math.abs(net) < 150) {
      status = "Balanced Maintenance";
    } else if (net > 0) {
      status = String.format(java.util.Locale.US, "+%.0f kcal Surplus", net);
    } else {
      status = String.format(java.util.Locale.US, "%.0f kcal Deficit", net);
    }

    return new DailyEnergyBalance(
        Math.round(foodIntakeKcal * 10.0) / 10.0,
        Math.round(bmrKcal * 10.0) / 10.0,
        Math.round(workoutBurnKcal * 10.0) / 10.0,
        Math.round(netStepBurnKcal * 10.0) / 10.0,
        Math.round(totalBurn * 10.0) / 10.0,
        Math.round(net * 10.0) / 10.0,
        status);
  }
}
