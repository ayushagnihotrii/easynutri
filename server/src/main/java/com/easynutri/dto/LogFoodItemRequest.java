package com.easynutri.dto;

import java.time.LocalDate;

public record LogFoodItemRequest(
    LocalDate date,
    String mealSlot,
    String foodId,
    String recipeId,
    String customName,
    Double amountG,
    Double servings) {}
