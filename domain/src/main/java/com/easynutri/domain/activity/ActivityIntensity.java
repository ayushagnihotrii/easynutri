package com.easynutri.domain.activity;

/** Intensity multipliers for physical activity MET values. */
public enum ActivityIntensity {
  LIGHT("Light Effort", 0.8),
  MODERATE("Moderate Effort", 1.0),
  VIGOROUS("Vigorous / Intense", 1.35);

  private final String displayName;
  private final double metMultiplier;

  ActivityIntensity(String displayName, double metMultiplier) {
    this.displayName = displayName;
    this.metMultiplier = metMultiplier;
  }

  public String getDisplayName() {
    return displayName;
  }

  public double getMetMultiplier() {
    return metMultiplier;
  }

  public static ActivityIntensity fromString(String value) {
    if (value == null || value.isBlank()) {
      return MODERATE;
    }
    try {
      return ActivityIntensity.valueOf(value.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      return MODERATE;
    }
  }
}
