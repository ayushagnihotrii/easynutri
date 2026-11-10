package com.easynutri.ai;

import com.easynutri.domain.safety.EmergencyDetectionResult;
import com.easynutri.domain.safety.EmergencyDetector;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class AiResponseValidator {

  private static final Pattern CALORIE_PATTERN =
      Pattern.compile("(?i)\\b(\\d+(\\.\\d+)?)\\s*(kcal|calories|cal)\\b");
  private static final Pattern DEFICIT_PATTERN =
      Pattern.compile("(?i)\\b(active deficit|calorie deficit|burn fat|weight loss deficit)\\b");

  public record ValidationResult(
      boolean isEmergency,
      String sanitizedContent,
      String safetyStatus,
      String emergencyGuidance) {}

  public ValidationResult validate(
      String userPrompt, String rawAiResponse, AiContextBuilder.UserAiContext context) {
    // 1. Check emergency conditions in either user prompt or AI response
    EmergencyDetectionResult promptEmergency = EmergencyDetector.evaluate(userPrompt);
    EmergencyDetectionResult responseEmergency = EmergencyDetector.evaluate(rawAiResponse);

    if (promptEmergency.isEmergency() || responseEmergency.isEmergency()) {
      EmergencyDetectionResult activeEmergency =
          promptEmergency.isEmergency() ? promptEmergency : responseEmergency;
      return new ValidationResult(
          true,
          activeEmergency.directiveMessage(),
          "EMERGENCY_OVERRIDE",
          activeEmergency.category());
    }

    String content = rawAiResponse;

    // 2. Sensitive mode redactions
    if (context.sensitiveMode()) {
      content = CALORIE_PATTERN.matcher(content).replaceAll("nourishing energy");
      content = DEFICIT_PATTERN.matcher(content).replaceAll("Gentle Energy Balance");
    }

    return new ValidationResult(false, content, "ALLOW", null);
  }
}
