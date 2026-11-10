package com.easynutri.dto;

public record DiaryItemDto(
    Long id,
    Long entryId,
    String foodId,
    String recipeId,
    String itemName,
    double amountG,
    double servings,
    double calories,
    double proteinG,
    double carbsG,
    double fatG,
    double fiberG,
    double sodiumMg) {}
