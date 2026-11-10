package com.easynutri;

import static org.junit.jupiter.api.Assertions.*;

import com.easynutri.ai.AiContextBuilder;
import com.easynutri.ai.AiResponseValidator;
import com.easynutri.domain.safety.EmergencyDetectionResult;
import com.easynutri.domain.safety.EmergencyDetector;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SafetyGatePipelineTest {

  private final AiResponseValidator responseValidator = new AiResponseValidator();

  @Test
  @DisplayName("EmergencyDetector intercepts acute clinical distress queries")
  void testAcuteDistressInterception() {
    EmergencyDetectionResult r1 =
        EmergencyDetector.evaluate("I have severe crushing chest pain and shortness of breath");
    assertTrue(r1.isEmergency());
    assertEquals("CARDIAC_EMERGENCY", r1.category());
    assertTrue(r1.directiveMessage().contains("emergency department"));

    EmergencyDetectionResult r2 =
        EmergencyDetector.evaluate(
            "My throat is swelling shut and I am covered in hives after eating nuts");
    assertTrue(r2.isEmergency());
    assertEquals("ANAPHYLAXIS", r2.category());
  }

  @Test
  @DisplayName("EmergencyDetector allows routine nutrition questions")
  void testRoutineNutritionAllowed() {
    EmergencyDetectionResult result =
        EmergencyDetector.evaluate("What are good whole food sources of plant-based protein?");
    assertFalse(result.isEmergency());
  }

  @Test
  @DisplayName(
      "ResponseValidator replaces calorie numbers and deficit terminology in sensitive mode")
  void testSensitiveModeSanitization() {
    AiContextBuilder.UserAiContext sensitiveContext =
        new AiContextBuilder.UserAiContext(
            true, "Alice", "VEGETARIAN", List.of(), List.of(), Map.of());

    String rawResponse =
        "You logged 1850 kcal today. To maintain an active deficit and lose weight, keep calories below 1700 cal.";

    AiResponseValidator.ValidationResult result =
        responseValidator.validate("How are my numbers?", rawResponse, sensitiveContext);

    assertFalse(result.isEmergency());
    assertFalse(result.sanitizedContent().contains("1850 kcal"));
    assertFalse(result.sanitizedContent().contains("active deficit"));
    assertTrue(result.sanitizedContent().contains("Gentle Energy Balance"));
  }
}
