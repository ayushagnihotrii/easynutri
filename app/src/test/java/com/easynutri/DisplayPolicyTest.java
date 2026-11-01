package com.easynutri;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.easynutri.core.display.DefaultDisplayPolicy;
import com.easynutri.core.display.DisplayPolicy;
import org.junit.Before;
import org.junit.Test;

public class DisplayPolicyTest {

  private DisplayPolicy displayPolicy;

  @Before
  public void setUp() {
    displayPolicy = new DefaultDisplayPolicy();
  }

  @Test
  public void testDefaultPolicyDisplaysNutritionalNumbers() {
    assertTrue(displayPolicy.shouldDisplayNutritionalNumbers());
  }

  @Test
  public void testFormatCalories() {
    String formatted = displayPolicy.formatCalories(2150.0);
    assertTrue(formatted.contains("2,150") || formatted.contains("2150"));
    assertTrue(formatted.contains("kcal"));
  }

  @Test
  public void testFormatWeight() {
    String formatted = displayPolicy.formatWeight(68.5);
    assertEquals("68.5 kg", formatted);
  }

  @Test
  public void testFormatMacro() {
    String formatted = displayPolicy.formatMacro(24.5, "g");
    assertEquals("24.5 g", formatted);
  }
}
