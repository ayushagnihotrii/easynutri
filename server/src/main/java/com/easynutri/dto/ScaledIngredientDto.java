package com.easynutri.dto;

public record ScaledIngredientDto(
    String foodId,
    String nameEn,
    String nameHi,
    double baseAmountG,
    double scaledAmountG,
    boolean optional) {}
