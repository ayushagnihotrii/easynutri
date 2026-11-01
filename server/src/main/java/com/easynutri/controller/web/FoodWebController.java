package com.easynutri.controller.web;

import com.easynutri.dto.FoodDto;
import com.easynutri.dto.RecipeDetailDto;
import com.easynutri.service.FoodService;
import com.easynutri.service.RecipeService;
import java.security.Principal;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class FoodWebController {

  private final RecipeService recipeService;
  private final FoodService foodService;

  public FoodWebController(RecipeService recipeService, FoodService foodService) {
    this.recipeService = recipeService;
    this.foodService = foodService;
  }

  @GetMapping("/recipes")
  public String listRecipes(
      @RequestParam(value = "slot", required = false) String slot,
      @RequestParam(value = "q", required = false) String query,
      Model model,
      Principal principal) {
    String username = principal != null ? principal.getName() : "demo";
    model.addAttribute("username", username);
    model.addAttribute("pageTitle", "Discover Clinical Recipes");
    model.addAttribute("activeNav", "recipes");
    model.addAttribute("currentSlot", slot != null ? slot : "ALL");

    List<RecipeDetailDto> recipes;
    if (query != null && !query.isBlank()) {
      recipes = recipeService.searchRecipes(query);
    } else if (slot != null && !slot.isBlank() && !"ALL".equalsIgnoreCase(slot)) {
      recipes = recipeService.findByMealSlot(slot);
    } else {
      recipes = recipeService.findAll();
    }

    model.addAttribute("recipes", recipes);
    model.addAttribute("searchQuery", query != null ? query : "");
    return "recipes/index";
  }

  @GetMapping("/recipes/{id}")
  public String viewRecipeDetail(
      @PathVariable("id") String id,
      @RequestParam(value = "servings", required = false, defaultValue = "2") int servings,
      Model model,
      Principal principal) {
    String username = principal != null ? principal.getName() : "demo";
    model.addAttribute("username", username);
    model.addAttribute("activeNav", "recipes");

    int safeServings = Math.max(1, Math.min(20, servings));

    return recipeService
        .findByIdAndServings(id, safeServings)
        .map(
            recipe -> {
              model.addAttribute("recipe", recipe);
              model.addAttribute("pageTitle", recipe.titleEn());
              model.addAttribute("servings", safeServings);
              return "recipes/detail";
            })
        .orElse("redirect:/recipes");
  }

  @GetMapping("/foods")
  public String listFoods(
      @RequestParam(value = "category", required = false) String category,
      @RequestParam(value = "q", required = false) String query,
      Model model,
      Principal principal) {
    String username = principal != null ? principal.getName() : "demo";
    model.addAttribute("username", username);
    model.addAttribute("pageTitle", "Food & Nutrition Database");
    model.addAttribute("activeNav", "finder");
    model.addAttribute("currentCategory", category != null ? category : "ALL");

    List<FoodDto> foods;
    if (query != null && !query.isBlank()) {
      foods = foodService.searchFoods(query);
    } else if (category != null && !category.isBlank() && !"ALL".equalsIgnoreCase(category)) {
      foods = foodService.findByCategory(category);
    } else {
      foods = foodService.findAll();
    }

    model.addAttribute("foods", foods);
    model.addAttribute("searchQuery", query != null ? query : "");
    return "recipes/finder";
  }
}
