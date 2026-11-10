package com.easynutri.dto;

import java.util.List;

public record RecipeAdaptationDto(
    Long adaptationId,
    String baseRecipeId,
    String baseRecipeTitle,
    String adaptationTitle,
    String adaptationType,
    int baseServings,
    double originalCalories,
    double originalProtein,
    double originalCarbs,
    double originalFat,
    double originalFiber,
    double originalSodium,
    double adaptedCalories,
    double adaptedProtein,
    double adaptedCarbs,
    double adaptedFat,
    double adaptedFiber,
    double adaptedSodium,
    String safetyVerdict,
    List<String> warnings,
    List<SubstitutionDiffDto> ingredientDiffs,
    List<String> instructions,
    boolean isSaved) {

  public record SubstitutionDiffDto(
      String originalFoodId,
      String originalFoodName,
      double originalQuantity,
      String originalUnit,
      String adaptedFoodId,
      String adaptedFoodName,
      double adaptedQuantity,
      String adaptedUnit,
      String substitutionReason) {}
}
