package com.easynutri.domain.nutrition;

import java.util.List;
import java.util.Objects;

/**
 * Immutable domain representation of clinical micronutrient references and ICMR-NIN 2024
 * guidelines.
 */
public record NutrientInfo(
    String key,
    String nameEn,
    String nameHi,
    String unit,
    double rdaMale,
    double rdaFemale,
    double rdaPregnant,
    String roleDescription,
    List<String> absorptionTips,
    List<String> deficiencyRisks,
    String clinicalDisclaimer) {

  public NutrientInfo {
    Objects.requireNonNull(key, "key cannot be null");
    Objects.requireNonNull(nameEn, "nameEn cannot be null");
    Objects.requireNonNull(unit, "unit cannot be null");
    absorptionTips = List.copyOf(absorptionTips != null ? absorptionTips : List.of());
    deficiencyRisks = List.copyOf(deficiencyRisks != null ? deficiencyRisks : List.of());
    clinicalDisclaimer =
        clinicalDisclaimer != null
            ? clinicalDisclaimer
            : "Consult a qualified physician or registered dietitian for suspected clinical deficiencies. EasyNutri provides dietary guidance, not diagnostic evaluation.";
  }
}
