package com.easynutri.dto;

import jakarta.validation.constraints.NotBlank;

public record AddGroceryItemRequest(
    @NotBlank(message = "Item name is required") String itemName,
    Double quantity,
    String unit,
    String category,
    String foodId) {

  public AddGroceryItemRequest {
    quantity = quantity != null && quantity > 0 ? quantity : 1.0;
    unit = unit != null && !unit.isBlank() ? unit : "serving";
    category = category != null && !category.isBlank() ? category : "GENERAL";
  }
}
