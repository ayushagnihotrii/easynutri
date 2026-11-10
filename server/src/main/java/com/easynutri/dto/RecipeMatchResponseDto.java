package com.easynutri.dto;

import java.util.List;

public record RecipeMatchResponseDto(
    String recipeId,
    String titleEn,
    String titleHi,
    String mealSlot,
    int cookMinutes,
    int prepMinutes,
    int servings,
    double matchScore,
    String matchLabel,
    String badgeClass,
    List<String> matchedIngredients,
    List<String> missingIngredients,
    List<String> suggestedSwaps,
    boolean usesLeftover,
    double caloriesPerServing,
    double proteinPerServing,
    double carbsPerServing,
    double fatPerServing,
    String safetyVerdict,
    List<String> safetyWarnings,
    boolean saved) {

  public RecipeMatchResponseDto {
    matchedIngredients = List.copyOf(matchedIngredients != null ? matchedIngredients : List.of());
    missingIngredients = List.copyOf(missingIngredients != null ? missingIngredients : List.of());
    suggestedSwaps = List.copyOf(suggestedSwaps != null ? suggestedSwaps : List.of());
    safetyWarnings = List.copyOf(safetyWarnings != null ? safetyWarnings : List.of());
  }
}
