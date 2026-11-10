package com.easynutri.domain.activity;

/** Common physical activities with standard base Metabolic Equivalent of Task (MET) values. */
public enum ActivityType {
  WALKING("Walking", "🚶", 3.0),
  RUNNING("Running / Jogging", "🏃", 8.0),
  CYCLING("Cycling", "🚴", 6.8),
  SWIMMING("Swimming", "🏊", 6.5),
  YOGA("Yoga / Stretching", "🧘", 2.8),
  STRENGTH_TRAINING("Strength Training", "🏋️", 4.5),
  DANCING("Dancing / Aerobics", "💃", 5.5),
  SPORTS("Badminton / Sports", "🏸", 6.0),
  OTHER("General Workout", "⏱️", 4.0);

  private final String displayName;
  private final String icon;
  private final double baseMet;

  ActivityType(String displayName, String icon, double baseMet) {
    this.displayName = displayName;
    this.icon = icon;
    this.baseMet = baseMet;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getIcon() {
    return icon;
  }

  public double getBaseMet() {
    return baseMet;
  }

  public static ActivityType fromString(String value) {
    if (value == null || value.isBlank()) {
      return WALKING;
    }
    try {
      return ActivityType.valueOf(value.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      return OTHER;
    }
  }
}
