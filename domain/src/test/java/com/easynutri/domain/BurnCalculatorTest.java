package com.easynutri.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.easynutri.domain.calculation.DefaultBurnCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BurnCalculatorTest {

  private DefaultBurnCalculator calculator;

  @BeforeEach
  void setUp() {
    calculator = new DefaultBurnCalculator();
  }

  @Test
  @DisplayName("Calculates male and female BMR accurately according to Mifflin-St Jeor")
  void testBmrCalculation() {
    // Male 70kg, 175cm, 30 years: 10(70) + 6.25(175) - 5(30) + 5 = 700 + 1093.75 - 150 + 5 =
    // 1648.75
    double maleBmr = calculator.calculateBmr(70.0, 175.0, 30, true);
    assertEquals(1648.75, maleBmr, 0.01);

    // Female 60kg, 165cm, 28 years: 10(60) + 6.25(165) - 5(28) - 161 = 600 + 1031.25 - 140 - 161 =
    // 1330.25
    double femaleBmr = calculator.calculateBmr(60.0, 165.0, 28, false);
    assertEquals(1330.25, femaleBmr, 0.01);
  }

  @Test
  @DisplayName("Estimates step calorie burn realistically without negative numbers")
  void testStepBurn() {
    double burn = calculator.estimateStepBurn(10000, 70.0);
    assertTrue(burn > 300.0 && burn < 500.0);

    assertEquals(0.0, calculator.estimateStepBurn(0, 70.0));
    assertEquals(0.0, calculator.estimateStepBurn(-500, 70.0));
  }

  @Test
  @DisplayName("Throws exception on invalid demographic inputs")
  void testInvalidInputs() {
    assertThrows(IllegalArgumentException.class, () -> calculator.calculateBmr(-70, 175, 25, true));
    assertThrows(IllegalArgumentException.class, () -> calculator.calculateBmr(70, -175, 25, true));
    assertThrows(IllegalArgumentException.class, () -> calculator.calculateBmr(70, 175, 0, true));
  }
}
