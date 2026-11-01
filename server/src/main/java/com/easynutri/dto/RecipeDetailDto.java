package com.easynutri.dto;

import java.util.List;

public record RecipeDetailDto(
    String id,
    String titleEn,
    String titleHi,
    String descriptionEn,
    int baseServings,
    int currentServings,
    int prepMinutes,
    int cookMinutes,
    String mealSlot,
    String mealRole,
    String instructionsText,
    List<String> tags,
    double totalCalories,
    double totalProteinG,
    double totalCarbG,
    double totalFatG,
    double totalFiberG,
    double perServingCalories,
    double perServingProteinG,
    double perServingCarbG,
    double perServingFatG,
    List<ScaledIngredientDto> ingredients,
    String source,
    boolean verified) {}
