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
    assertEquals("22.5", DisplayPolicy.formatBmi(22.54, false));
    assertEquals("+1.2 kg", DisplayPolicy.formatWeightChange(1.2, false));
    assertEquals("-0.8 kg", DisplayPolicy.formatWeightChange(-0.8, false));
    assertEquals(
        "Check your daily targets and energy progress.", DisplayPolicy.getCheckInMessage(false));
  }

  @Test
  void shouldMaskSensitiveMetricsInSensitiveMode() {
    assertEquals("Balanced", DisplayPolicy.formatCalories(2100.4, true));
    assertEquals("-- kg (Masked)", DisplayPolicy.formatWeight(68.52, true));
    assertEquals("Nourishing", DisplayPolicy.formatMacroGrams(75.3, true));
    assertEquals("Sustained Energy", DisplayPolicy.formatEnergyBalance(250.0, true));
    assertEquals("Gentle Energy Balance", DisplayPolicy.formatEnergyBalance(-350.0, true));
    assertEquals("Healthy Balance (Masked)", DisplayPolicy.formatBmi(22.54, true));
    assertEquals("Gentle Body Rhythm", DisplayPolicy.formatWeightChange(1.2, true));
    assertEquals("Gentle Body Rhythm", DisplayPolicy.formatWeightChange(-0.8, true));
    assertEquals(
        "Your daily check-in is ready whenever you are.", DisplayPolicy.getCheckInMessage(true));
  }
}
