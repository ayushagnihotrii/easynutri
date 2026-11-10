package com.easynutri.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.easynutri.domain.safety.EmergencyDetectionResult;
import com.easynutri.domain.safety.EmergencyDetector;
import org.junit.jupiter.api.Test;

class EmergencyDetectorTest {

  @Test
  void shouldPassNormalNutritionQuerySafely() {
    EmergencyDetectionResult res =
        EmergencyDetector.evaluate("What high protein vegetarian meal can I cook with paneer?");
    assertFalse(res.isEmergency());
    assertNull(res.category());
  }

  @Test
  void shouldDetectAnaphylaxisEmergency() {
    EmergencyDetectionResult res =
        EmergencyDetector.evaluate("Help! My throat is closing after eating peanuts!");
    assertTrue(res.isEmergency());
    assertEquals("ANAPHYLAXIS", res.category());
    assertTrue(res.directiveMessage().contains("112 / 911"));
    assertTrue(res.directiveMessage().contains("EpiPen"));
  }

  @Test
  void shouldDetectChestPainCardiacEmergency() {
    EmergencyDetectionResult res =
        EmergencyDetector.evaluate("I have severe crushing chest pain and arm numbness.");
    assertTrue(res.isEmergency());
    assertEquals("CARDIAC_EMERGENCY", res.category());
    assertTrue(res.directiveMessage().contains("emergency department"));
  }

  @Test
  void shouldDetectDiabeticKetoacidosisCrisis() {
    EmergencyDetectionResult res =
        EmergencyDetector.evaluate("Patient is unresponsive with suspected ketoacidosis.");
    assertTrue(res.isEmergency());
    assertEquals("METABOLIC_CRISIS", res.category());
  }
}
