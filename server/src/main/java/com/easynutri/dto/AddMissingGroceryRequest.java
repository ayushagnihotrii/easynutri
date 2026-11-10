package com.easynutri.dto;

import java.util.List;

public record AddMissingGroceryRequest(List<String> missingIngredients, String recipeId) {
  public AddMissingGroceryRequest {
    missingIngredients = List.copyOf(missingIngredients != null ? missingIngredients : List.of());
  }
}
