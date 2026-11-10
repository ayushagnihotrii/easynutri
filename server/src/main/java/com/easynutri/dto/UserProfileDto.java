package com.easynutri.dto;

import java.util.Set;

/** DTO representing complete user profile and clinical targets for mobile/web clients. */
public record UserProfileDto(
    Long id,
    String username,
    int age,
    String biologicalSex,
    double heightCm,
    double weightKg,
    String activityLevel,
    String healthGoal,
    String dietaryPreference,
    boolean isPregnant,
    boolean sensitiveMode,
    boolean onboardingCompleted,
    Integer targetCalories,
    Integer targetProteinG,
    Integer targetCarbG,
    Integer targetFatG,
    Double targetWaterL,
    Set<String> allergies,
    Set<String> healthConditions) {}
