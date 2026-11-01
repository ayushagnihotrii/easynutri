package com.easynutri.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record OnboardingRequest(
    @Min(1) @Max(120) int age,
    @NotBlank String biologicalSex,
    @Min(50) @Max(260) double heightCm,
    @Min(20) @Max(350) double weightKg,
    @NotBlank String activityLevel,
    @NotBlank String healthGoal,
    @NotBlank String dietaryPreference,
    Boolean isPregnant,
    Boolean sensitiveMode,
    List<String> allergies,
    List<String> healthConditions) {
  public OnboardingRequest {
    isPregnant = isPregnant != null ? isPregnant : false;
    sensitiveMode = sensitiveMode != null ? sensitiveMode : false;
    allergies = allergies != null ? allergies : List.of();
    healthConditions = healthConditions != null ? healthConditions : List.of();
  }
}
