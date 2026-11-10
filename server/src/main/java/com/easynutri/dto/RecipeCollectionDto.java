package com.easynutri.dto;

import java.util.List;

public record RecipeCollectionDto(
    Long id, String name, int displayOrder, int recipeCount, List<String> recipeIds) {}
