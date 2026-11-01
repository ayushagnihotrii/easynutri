package com.easynutri.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.easynutri.domain.calculation.BmiCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BmiCalculatorTest {

  @ParameterizedTest
  @DisplayName("Verify BMI category thresholds according to WHO standard")
  @CsvSource({
    "48.0, 165.0, UNDERWEIGHT, 17.6",
    "65.0, 170.0, NORMAL, 22.5",
    "78.0, 168.0, OVERWEIGHT, 27.6",
    "95.0, 165.0, OBESE, 34.9"
  })
  void testBmiCategories(
      double weight, double height, BmiCalculator.Category expectedCat, double expectedValue) {
    BmiCalculator.BmiResult result = BmiCalculator.calculate(weight, height);
    assertEquals(expectedValue, result.bmiValue(), 0.1);
    assertEquals(expectedCat, result.category());
    assertNotNull(result.guidance());
  }

  @Test
  @DisplayName("Verify physiological bounds validation")
  void testBoundsValidation() {
    assertThrows(IllegalArgumentException.class, () -> BmiCalculator.calculate(15.0, 170.0));
    assertThrows(IllegalArgumentException.class, () -> BmiCalculator.calculate(70.0, 30.0));
  }
}
