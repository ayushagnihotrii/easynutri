package com.easynutri.domain.calculation;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Deterministic clinical BMI (Body Mass Index) calculator adhering to WHO adult criteria. */
public final class BmiCalculator {

  public enum Category {
    UNDERWEIGHT("Underweight", "Nutrient-dense calorie replenishment recommended."),
    NORMAL("Normal Weight", "Healthy weight range. Maintain balanced nutrition."),
    OVERWEIGHT("Overweight", "Gentle caloric moderation and increased activity recommended."),
    OBESE("Obese", "Clinical lifestyle and dietary support advised.");

    private final String label;
    private final String clinicalGuidance;

    Category(String label, String clinicalGuidance) {
      this.label = label;
      this.clinicalGuidance = clinicalGuidance;
    }

    public String getLabel() {
      return label;
    }

    public String getClinicalGuidance() {
      return clinicalGuidance;
    }
  }

  public record BmiResult(double bmiValue, Category category, String label, String guidance) {}

  private BmiCalculator() {}

  /**
   * Calculates Body Mass Index (BMI = weight(kg) / height(m)^2).
   *
   * @param weightKg body weight in kilograms
   * @param heightCm body height in centimeters
   * @return immutable BmiResult with value rounded to 1 decimal place and WHO category
   */
  public static BmiResult calculate(double weightKg, double heightCm) {
    if (weightKg <= 20.0 || weightKg >= 400.0) {
      throw new IllegalArgumentException(
          "Weight must be within a realistic physiological range (20kg to 400kg). Provided: "
              + weightKg);
    }
    if (heightCm <= 50.0 || heightCm >= 260.0) {
      throw new IllegalArgumentException(
          "Height must be within a realistic physiological range (50cm to 260cm). Provided: "
              + heightCm);
    }

    double heightM = heightCm / 100.0;
    double rawBmi = weightKg / (heightM * heightM);

    BigDecimal rounded = BigDecimal.valueOf(rawBmi).setScale(1, RoundingMode.HALF_UP);
    double bmi = rounded.doubleValue();

    Category cat;
    if (bmi < 18.5) {
      cat = Category.UNDERWEIGHT;
    } else if (bmi < 25.0) {
      cat = Category.NORMAL;
    } else if (bmi < 30.0) {
      cat = Category.OVERWEIGHT;
    } else {
      cat = Category.OBESE;
    }

    return new BmiResult(bmi, cat, cat.getLabel(), cat.getClinicalGuidance());
  }
}
