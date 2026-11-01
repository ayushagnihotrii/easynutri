package com.easynutri.service;

import com.easynutri.dto.RecipeDetailDto;
import com.easynutri.dto.ScaledIngredientDto;
import com.easynutri.entity.FoodEntity;
import com.easynutri.entity.RecipeEntity;
import com.easynutri.entity.RecipeIngredientEntity;
import com.easynutri.repository.RecipeRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecipeService {

  private final RecipeRepository recipeRepository;

  public RecipeService(RecipeRepository recipeRepository) {
    this.recipeRepository = recipeRepository;
  }

  @Transactional(readOnly = true)
  public List<RecipeDetailDto> findAll() {
    return recipeRepository.findAll().stream()
        .map(r -> computeScaledNutrition(r, r.getBaseServings()))
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<RecipeDetailDto> findByMealSlot(String slot) {
    if (slot == null || slot.isBlank() || "ALL".equalsIgnoreCase(slot)) {
      return findAll();
    }
    return recipeRepository.findByMealSlotIgnoreCase(slot).stream()
        .map(r -> computeScaledNutrition(r, r.getBaseServings()))
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public Optional<RecipeDetailDto> findByIdAndServings(String id, int servings) {
    return recipeRepository.findById(id).map(r -> computeScaledNutrition(r, servings));
  }

  @Transactional(readOnly = true)
  public List<RecipeDetailDto> searchRecipes(String query) {
    if (query == null || query.isBlank()) {
      return findAll();
    }
    return recipeRepository.searchRecipes(query.trim()).stream()
        .map(r -> computeScaledNutrition(r, r.getBaseServings()))
        .collect(Collectors.toList());
  }

  public RecipeDetailDto computeScaledNutrition(RecipeEntity recipe, int targetServings) {
    int safeServings = Math.max(1, targetServings);
    double scaleFactor = (double) safeServings / (double) Math.max(1, recipe.getBaseServings());

    double totalCalories = 0.0;
    double totalProtein = 0.0;
    double totalCarbs = 0.0;
    double totalFat = 0.0;
    double totalFiber = 0.0;

    List<ScaledIngredientDto> scaledIngredients = new ArrayList<>();

    for (RecipeIngredientEntity ing : recipe.getIngredients()) {
      FoodEntity food = ing.getFood();
      double scaledAmountG = round(ing.getAmountG() * scaleFactor, 1);

      scaledIngredients.add(
          new ScaledIngredientDto(
              food.getId(),
              food.getNameEn(),
              food.getNameHi(),
              ing.getAmountG(),
              scaledAmountG,
              ing.isOptional()));

      // Calculate nutrient contribution from canonical food per-100g database values
      double ratio = scaledAmountG / 100.0;
      totalCalories += food.getEnergyKcal() * ratio;
      totalProtein += food.getProteinG() * ratio;
      totalCarbs += food.getCarbohydrateG() * ratio;
      totalFat += food.getFatG() * ratio;
      totalFiber += food.getFibreG() * ratio;
    }

    double perServingCal = totalCalories / safeServings;
    double perServingPro = totalProtein / safeServings;
    double perServingCarb = totalCarbs / safeServings;
    double perServingFat = totalFat / safeServings;

    List<String> tags =
        recipe.getTags() != null && !recipe.getTags().isBlank()
            ? Arrays.stream(recipe.getTags().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList())
            : List.of();

    return new RecipeDetailDto(
        recipe.getId(),
        recipe.getTitleEn(),
        recipe.getTitleHi(),
        recipe.getDescriptionEn(),
        recipe.getBaseServings(),
        safeServings,
        recipe.getPrepMinutes(),
        recipe.getCookMinutes(),
        recipe.getMealSlot(),
        recipe.getMealRole(),
        recipe.getInstructionsText(),
        tags,
        round(totalCalories, 1),
        round(totalProtein, 1),
        round(totalCarbs, 1),
        round(totalFat, 1),
        round(totalFiber, 1),
        round(perServingCal, 1),
        round(perServingPro, 1),
        round(perServingCarb, 1),
        round(perServingFat, 1),
        scaledIngredients,
        recipe.getSource(),
        recipe.isVerified());
  }

  private double round(double value, int places) {
    return BigDecimal.valueOf(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
  }
}
