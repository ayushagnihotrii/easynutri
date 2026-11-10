package com.easynutri.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AddMealPlanRequest(
    @NotBlank(message = "Recipe ID is required") String recipeId,
    @NotNull(message = "Planned date is required") LocalDate plannedDate,
    String mealSlot,
    Integer servings) {

  public AddMealPlanRequest {
    mealSlot = mealSlot != null && !mealSlot.isBlank() ? mealSlot : "LUNCH";
    servings = servings != null && servings > 0 ? servings : 2;
  }
}
