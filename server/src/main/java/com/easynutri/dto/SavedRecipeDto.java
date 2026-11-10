package com.easynutri.dto;

public record SavedRecipeDto(
    Long id,
    String recipeId,
    String recipeTitle,
    String recipeTitleHi,
    String mealSlot,
    int cookMinutes,
    int baseServings,
    String notes) {}
