package com.easynutri.controller.api;

import com.easynutri.dto.FoodDto;
import com.easynutri.service.FoodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/foods")
@Tag(
    name = "Food Catalog",
    description = "Endpoints for canonical IFCT & USDA nutrition food catalog")
public class FoodApiController {

  private final FoodService foodService;

  public FoodApiController(FoodService foodService) {
    this.foodService = foodService;
  }

  @GetMapping
  @Operation(summary = "Search foods with optional text query or category filter")
  public ResponseEntity<List<FoodDto>> listFoods(
      @RequestParam(value = "q", required = false) String query,
      @RequestParam(value = "category", required = false) String category) {
    List<FoodDto> foods;
    if (query != null && !query.isBlank()) {
      foods = foodService.searchFoods(query);
    } else if (category != null && !category.isBlank() && !"ALL".equalsIgnoreCase(category)) {
      foods = foodService.findByCategory(category);
    } else {
      foods = foodService.findAll();
    }
    return ResponseEntity.ok(foods);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get canonical food item with complete nutrient breakdown per 100g")
  public ResponseEntity<FoodDto> getFood(@PathVariable("id") String id) {
    return foodService
        .findById(id)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping("/categories")
  @Operation(summary = "Get all distinct food catalog categories")
  public ResponseEntity<List<String>> getCategories() {
    return ResponseEntity.ok(
        List.of(
            "Grains & Cereals",
            "Pulses & Legumes",
            "Vegetables",
            "Fruits",
            "Dairy & Milk",
            "Nuts & Oilseeds",
            "Spices & Condiments",
            "General & Packaged"));
  }
}
