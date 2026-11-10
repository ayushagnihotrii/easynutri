package com.easynutri.dto;

import java.util.List;

public record CookBeforeExpiresDto(
    String recipeId,
    String recipeTitle,
    String mealSlot,
    int cookMinutes,
    List<String> expiringIngredientsUsed,
    int missingCount,
    String matchLabel) {}
