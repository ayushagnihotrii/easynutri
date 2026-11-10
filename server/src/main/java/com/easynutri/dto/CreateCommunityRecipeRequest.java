package com.easynutri.dto;

import java.util.List;

public record CreateCommunityRecipeRequest(
    String title,
    String description,
    String coverImageUrl,
    int baseServings,
    int prepMinutes,
    int cookMinutes,
    String mealSlot,
    String instructionsText,
    List<RecipeIngredientInputDto> ingredients) {

  public CreateCommunityRecipeRequest {
    ingredients = List.copyOf(ingredients != null ? ingredients : List.of());
  }
}
