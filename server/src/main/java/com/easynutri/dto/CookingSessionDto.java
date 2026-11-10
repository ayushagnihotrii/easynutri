package com.easynutri.dto;

import java.util.List;

public record CookingSessionDto(
    Long id,
    String recipeId,
    String recipeTitle,
    int servings,
    int currentStep,
    int totalSteps,
    boolean completed,
    boolean pantryDeducted,
    boolean diaryLogged,
    String notes,
    List<CookingStepDto> steps,
    List<ScaledIngredientDto> ingredients) {}
