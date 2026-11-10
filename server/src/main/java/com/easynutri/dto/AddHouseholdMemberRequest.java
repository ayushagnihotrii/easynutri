package com.easynutri.dto;

import java.util.List;

public record AddHouseholdMemberRequest(
    String displayName,
    String relationship, // SELF, SPOUSE, FATHER, MOTHER, CHILD, SIBLING, OTHER
    String role, // MEMBER, DEPENDENT, HOUSEHOLD_ADMIN
    int age,
    String biologicalSex,
    Double heightCm,
    Double weightKg,
    String activityLevel,
    String healthGoal,
    String dietaryPreference,
    List<String> allergies,
    List<String> healthConditions,
    boolean sensitiveMode,
    String avatarColor) {

  public AddHouseholdMemberRequest {
    allergies = allergies != null ? List.copyOf(allergies) : List.of();
    healthConditions = healthConditions != null ? List.copyOf(healthConditions) : List.of();
    role = role != null && !role.isBlank() ? role.toUpperCase() : "MEMBER";
    relationship =
        relationship != null && !relationship.isBlank() ? relationship.toUpperCase() : "OTHER";
    dietaryPreference =
        dietaryPreference != null && !dietaryPreference.isBlank()
            ? dietaryPreference.toUpperCase()
            : "VEGETARIAN";
    activityLevel = activityLevel != null ? activityLevel.toUpperCase() : "MODERATE";
    healthGoal = healthGoal != null ? healthGoal.toUpperCase() : "MAINTAIN";
    avatarColor = avatarColor != null && !avatarColor.isBlank() ? avatarColor : "#237A57";
  }
}
