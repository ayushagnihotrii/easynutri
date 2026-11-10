package com.easynutri.domain.safety;

import java.util.Locale;

/**
 * Pure Java clinical emergency screener. Detects signs of acute life-threatening medical events
 * (anaphylaxis, cardiac distress, stroke, severe diabetic crisis, poisoning) and mandates immediate
 * referral to emergency services rather than offering nutritional or lifestyle remedies.
 */
public final class EmergencyDetector {

  private EmergencyDetector() {}

  /**
   * Evaluates user prompt for acute medical emergency indicators.
   *
   * @param query user prompt or message
   * @return EmergencyDetectionResult detailing emergency classification and guidance
   */
  public static EmergencyDetectionResult evaluate(String query) {
    if (query == null || query.isBlank()) {
      return EmergencyDetectionResult.safe();
    }

    String text = query.toLowerCase(Locale.ROOT);

    // 1. Acute Anaphylaxis / Severe Allergic Reaction
    if (containsAny(
        text,
        "anaphylaxis",
        "throat is closing",
        "throat swelling",
        "throat is swelling",
        "swelling shut",
        "can't breathe allergy",
        "cannot breathe allergy",
        "tongue is swollen",
        "epipen",
        "covered in hives",
        "allergic shock")) {
      return EmergencyDetectionResult.emergency(
          "ANAPHYLAXIS",
          "🚨 MEDICAL EMERGENCY: Severe allergic reaction (Anaphylaxis) suspected. "
              + "Use an epinephrine auto-injector (EpiPen) immediately if prescribed and call local emergency services (112 / 911 / 999) immediately. "
              + "Do not consume food, water, or oral home remedies.");
    }

    // 2. Cardiac Emergency / Myocardial Infarction
    if (containsAny(
        text,
        "chest pain",
        "chest pressure",
        "crushing pain in chest",
        "crushing chest pain",
        "pain radiating to left arm",
        "heart attack",
        "cardiac arrest")) {
      return EmergencyDetectionResult.emergency(
          "CARDIAC_EMERGENCY",
          "🚨 MEDICAL EMERGENCY: Acute cardiac distress symptoms detected. "
              + "Seek immediate emergency medical attention or proceed to the nearest emergency department right now. "
              + "Do not attempt to treat chest pain with food, dietary changes, or supplements.");
    }

    // 3. Acute Stroke (FAST symptoms)
    if (containsAny(
        text,
        "face drooping",
        "facial droop",
        "arm weakness",
        "slurred speech",
        "sudden numbness one side",
        "stroke symptoms")) {
      return EmergencyDetectionResult.emergency(
          "STROKE_EMERGENCY",
          "🚨 MEDICAL EMERGENCY: Acute neurological deficit (Stroke / FAST) suspected. "
              + "Call emergency services immediately. Note time of symptom onset. "
              + "Do not give food, water, or aspirin.");
    }

    // 4. Severe Diabetic Ketoacidosis or Hypoglycemic Shock
    if (containsAny(
        text,
        "ketoacidosis",
        "diabetic coma",
        "hypoglycemic seizure",
        "unresponsive diabetic",
        "severe hypoglycemia passing out")) {
      return EmergencyDetectionResult.emergency(
          "METABOLIC_CRISIS",
          "🚨 MEDICAL EMERGENCY: Acute diabetic or metabolic crisis suspected. "
              + "If conscious and able to swallow, follow emergency hypoglycemia protocols. "
              + "If confused or unresponsive, call emergency services immediately. Do not force oral liquids.");
    }

    // 5. Acute Poisoning or Chemical Ingestion
    if (containsAny(
        text,
        "swallowed bleach",
        "drank poison",
        "ingested cleaning chemical",
        "swallowed battery",
        "overdose on pills")) {
      return EmergencyDetectionResult.emergency(
          "POISONING_EMERGENCY",
          "🚨 MEDICAL EMERGENCY: Acute poisoning or chemical ingestion reported. "
              + "Contact your local Poison Control Center or Emergency Services immediately. "
              + "Do not induce vomiting unless explicitly directed by medical authorities.");
    }

    return EmergencyDetectionResult.safe();
  }

  private static boolean containsAny(String text, String... keywords) {
    for (String kw : keywords) {
      if (text.contains(kw)) {
        return true;
      }
    }
    return false;
  }
}
