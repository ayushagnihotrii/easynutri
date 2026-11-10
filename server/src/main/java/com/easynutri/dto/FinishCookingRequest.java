package com.easynutri.dto;

public record FinishCookingRequest(
    Integer servings, Boolean deductPantry, Boolean logDiary, String notes) {}
