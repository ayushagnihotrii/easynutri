package com.easynutri.domain.recipe;

import java.util.*;

/**
 * Pure Java automatic dietary tag generator. Evaluates verified ingredient records and nutritional
 * thresholds to assign evidence-backed tags.
 */
public final class DietaryTagClassifier {

  private DietaryTagClassifier() {}

  public record RecipeNutritionSnapshot(
      double caloriesPerServing,
      double proteinG,
      double carbsG,
      double fatG,
      double fiberG,
      double sodiumMg,
      double ironMg,
      double calciumMg) {}

  public record IngredientDetail(
      String foodId,
      String foodName,
      String category,
      List<String> allergenTags,
      List<String> dietaryFlags) {}

  public static List<String> classifyTags(
      List<IngredientDetail> ingredients, RecipeNutritionSnapshot nutrition) {
    if (ingredients == null || ingredients.isEmpty()) {
      return List.of();
    }

    Set<String> tags = new LinkedHashSet<>();

    boolean containsMeat = false;
    boolean containsDairy = false;
    boolean containsEggs = false;
    boolean containsGluten = false;
    boolean containsAlliumOrRoot = false;

    for (IngredientDetail ing : ingredients) {
      String id = ing.foodId() != null ? ing.foodId().toLowerCase(Locale.ROOT) : "";
      String name = ing.foodName() != null ? ing.foodName().toLowerCase(Locale.ROOT) : "";
      String cat = ing.category() != null ? ing.category().toLowerCase(Locale.ROOT) : "";

      List<String> allergens = ing.allergenTags() != null ? ing.allergenTags() : List.of();
      List<String> flags = ing.dietaryFlags() != null ? ing.dietaryFlags() : List.of();

      if (cat.contains("meat")
          || cat.contains("poultry")
          || cat.contains("seafood")
          || cat.contains("fish")
          || allergens.contains("fish")
          || allergens.contains("crustacean")) {
        containsMeat = true;
      }

      if (cat.contains("dairy")
          || allergens.contains("milk")
          || allergens.contains("dairy")
          || id.contains("paneer")
          || id.contains("milk")
          || id.contains("ghee")
          || id.contains("curd")
          || id.contains("butter")) {
        containsDairy = true;
      }

      if (cat.contains("egg") || allergens.contains("egg") || id.contains("egg")) {
        containsEggs = true;
      }

      if (allergens.contains("gluten")
          || allergens.contains("wheat")
          || id.contains("wheat")
          || id.contains("maida")
          || id.contains("atta")) {
        containsGluten = true;
      }

      if (id.contains("onion")
          || name.contains("onion")
          || id.contains("garlic")
          || name.contains("garlic")
          || id.contains("potato")
          || name.contains("potato")
          || id.contains("carrot")
          || name.contains("carrot")
          || id.contains("radish")
          || name.contains("radish")) {
        containsAlliumOrRoot = true;
      }
    }

    if (!containsMeat && !containsEggs) {
      tags.add("VEGETARIAN");
    }

    if (!containsMeat && !containsEggs && !containsDairy) {
      tags.add("VEGAN");
    }

    if (!containsMeat && !containsEggs && !containsDairy && !containsAlliumOrRoot) {
      tags.add("JAIN");
    }

    if (!containsDairy) {
      tags.add("DAIRY_FREE");
    }

    if (!containsGluten) {
      tags.add("GLUTEN_FREE");
    }

    // Nutrient density thresholds
    if (nutrition != null) {
      if (nutrition.proteinG() >= 15.0) {
        tags.add("HIGH_PROTEIN");
      }
      if (nutrition.fiberG() >= 6.0) {
        tags.add("HIGH_FIBER");
      }
      if (nutrition.sodiumMg() > 0 && nutrition.sodiumMg() <= 140.0) {
        tags.add("LOW_SODIUM");
      }
      if (nutrition.ironMg() >= 3.0) {
        tags.add("IRON_SOURCE");
      }
      if (nutrition.calciumMg() >= 150.0) {
        tags.add("CALCIUM_SOURCE");
      }
    }

    return new ArrayList<>(tags);
  }
}
