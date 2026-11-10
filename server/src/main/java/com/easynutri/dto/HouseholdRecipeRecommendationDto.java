package com.easynutri.dto;

import java.util.List;

public record HouseholdRecipeRecommendationDto(
    String recipeId,
    String title,
    String coverImageUrl,
    String mealSlot,
    int prepMinutes,
    int cookMinutes,
    boolean isSafe,
    List<String> violations,
    String explanation,
    double totalServings,
    List<MemberPortionDto> portions) {}
