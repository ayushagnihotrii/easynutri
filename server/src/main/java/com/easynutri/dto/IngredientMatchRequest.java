package com.easynutri.dto;

import java.util.List;

public record IngredientMatchRequest(
    List<String> ingredients,
    Boolean leftoverMode,
    List<String> leftoverIngredients,
    Integer hoursAgo,
    String storageMethod) {

  public IngredientMatchRequest {
    ingredients = List.copyOf(ingredients != null ? ingredients : List.of());
    leftoverMode = leftoverMode != null ? leftoverMode : false;
    leftoverIngredients =
        List.copyOf(leftoverIngredients != null ? leftoverIngredients : List.of());
    hoursAgo = hoursAgo != null ? hoursAgo : 0;
    storageMethod =
        storageMethod != null && !storageMethod.isBlank() ? storageMethod : "REFRIGERATED";
  }
}
