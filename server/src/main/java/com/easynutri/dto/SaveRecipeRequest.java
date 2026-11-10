package com.easynutri.dto;

import jakarta.validation.constraints.NotBlank;

public record SaveRecipeRequest(
    @NotBlank(message = "Recipe ID is required") String recipeId, String notes) {}
