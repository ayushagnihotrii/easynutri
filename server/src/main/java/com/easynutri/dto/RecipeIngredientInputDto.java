package com.easynutri.dto;

public record RecipeIngredientInputDto(
    String foodId, String ingredientName, double amountG, String unit) {}
