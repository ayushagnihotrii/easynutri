package com.easynutri.dto;

public record GroceryItemDto(
    Long id,
    String itemName,
    double quantity,
    String unit,
    boolean checked,
    String category,
    String foodId) {}
