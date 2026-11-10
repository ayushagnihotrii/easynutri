package com.easynutri.domain.safety;

/** Result of clinical emergency keyword screening on user messages. */
public record EmergencyDetectionResult(
    boolean isEmergency, String category, String directiveMessage) {

  public static EmergencyDetectionResult safe() {
    return new EmergencyDetectionResult(false, null, null);
  }

  public static EmergencyDetectionResult emergency(String category, String directiveMessage) {
    return new EmergencyDetectionResult(true, category, directiveMessage);
  }
}
