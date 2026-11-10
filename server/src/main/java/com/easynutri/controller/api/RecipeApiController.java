package com.easynutri.controller.api;

import com.easynutri.dto.RecipeDetailDto;
import com.easynutri.service.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recipes")
@Tag(
    name = "Recipes",
    description = "Endpoints for discovering verified recipes and scaled nutritional ingredients")
public class RecipeApiController {

  private final RecipeService recipeService;

  public RecipeApiController(RecipeService recipeService) {
    this.recipeService = recipeService;
  }

  @GetMapping
  @Operation(summary = "Search recipes with optional text query or meal slot filter")
  public ResponseEntity<List<RecipeDetailDto>> listRecipes(
      @RequestParam(value = "q", required = false) String query,
      @RequestParam(value = "slot", required = false) String slot) {
    List<RecipeDetailDto> recipes;
    if (query != null && !query.isBlank()) {
      recipes = recipeService.searchRecipes(query);
    } else if (slot != null && !slot.isBlank() && !"ALL".equalsIgnoreCase(slot)) {
      recipes = recipeService.findByMealSlot(slot);
    } else {
      recipes = recipeService.findAll();
    }
    return ResponseEntity.ok(recipes);
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get recipe details with dynamically scaled ingredient quantities and nutrition")
  public ResponseEntity<RecipeDetailDto> getRecipe(
      @PathVariable("id") String id,
      @RequestParam(value = "servings", required = false, defaultValue = "2") int servings) {
    int safeServings = Math.max(1, Math.min(20, servings));
    return recipeService
        .findByIdAndServings(id, safeServings)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }
}
