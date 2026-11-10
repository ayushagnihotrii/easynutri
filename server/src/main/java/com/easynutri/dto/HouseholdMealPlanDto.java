package com.easynutri.dto;

import java.time.LocalDate;
import java.util.List;

public record HouseholdMealPlanDto(
    Long id,
    Long householdId,
    String recipeId,
    String recipeTitle,
    String recipeCoverUrl,
    LocalDate planDate,
    String mealSlot,
    List<HouseholdMemberDto> participatingMembers,
    double totalServings,
    List<MemberPortionDto> portions) {}
