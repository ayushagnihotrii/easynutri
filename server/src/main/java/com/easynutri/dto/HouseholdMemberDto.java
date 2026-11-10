package com.easynutri.dto;

import java.util.List;

public record HouseholdMemberDto(
    Long memberId,
    Long householdId,
    Long userId,
    String displayName,
    String relationship,
    String role,
    String avatarColor,
    String avatarInitials,
    int age,
    String biologicalSex,
    String dietaryPreference,
    List<String> allergies,
    List<String> healthConditions,
    boolean sensitiveMode,
    Integer targetCalories,
    boolean isDependent,
    boolean canManageHousehold,
    boolean isCurrentProfile) {}
