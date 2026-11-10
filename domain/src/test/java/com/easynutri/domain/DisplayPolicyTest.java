package com.easynutri.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.easynutri.domain.policy.DisplayPolicy;
import org.junit.jupiter.api.Test;

class DisplayPolicyTest {

  @Test
  void shouldFormatCorrectlyInStandardMode() {
    assertEquals("2100 kcal", DisplayPolicy.formatCalories(2100.4, false));
    assertEquals("68.5 kg", DisplayPolicy.formatWeight(68.52, false));
    assertEquals("75g", DisplayPolicy.formatMacroGrams(75.3, false));
    assertTrue(DisplayPolicy.formatEnergyBalance(250.0, false).contains("+250 kcal Surplus"));
  }

  @Test
  void shouldMaskSensitiveMetricsInSensitiveMode() {
    assertEquals("Balanced", DisplayPolicy.formatCalories(2100.4, true));
    assertEquals("-- kg (Masked)", DisplayPolicy.formatWeight(68.52, true));
    assertEquals("Nourishing", DisplayPolicy.formatMacroGrams(75.3, true));
    assertEquals("Sustained Energy", DisplayPolicy.formatEnergyBalance(250.0, true));
  }
}
