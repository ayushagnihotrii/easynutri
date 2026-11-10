package com.easynutri.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.easynutri.domain.recipe.DietaryTagClassifier;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DietaryTagClassifierTest {

  @Test
  @DisplayName("Classifies vegan, dairy-free, and high protein when ingredients and macros align")
  void testClassifyVeganHighProtein() {
    List<DietaryTagClassifier.IngredientDetail> ingredients =
        List.of(
            new DietaryTagClassifier.IngredientDetail(
                "tofu", "Firm Tofu", "Legumes", List.of("soy"), List.of("VEGAN")),
            new DietaryTagClassifier.IngredientDetail(
                "spinach", "Fresh Spinach", "Vegetables", List.of(), List.of("VEGAN")),
            new DietaryTagClassifier.IngredientDetail(
                "olive_oil", "Extra Virgin Olive Oil", "Fats", List.of(), List.of("VEGAN")));

    DietaryTagClassifier.RecipeNutritionSnapshot nutrition =
        new DietaryTagClassifier.RecipeNutritionSnapshot(
            280.0, 18.5, 8.0, 12.0, 6.5, 120.0, 3.8, 160.0);

    List<String> tags = DietaryTagClassifier.classifyTags(ingredients, nutrition);

    assertTrue(tags.contains("VEGETARIAN"));
    assertTrue(tags.contains("VEGAN"));
    assertTrue(tags.contains("DAIRY_FREE"));
    assertTrue(tags.contains("GLUTEN_FREE"));
    assertTrue(tags.contains("HIGH_PROTEIN"));
    assertTrue(tags.contains("HIGH_FIBER"));
    assertTrue(tags.contains("LOW_SODIUM"));
    assertTrue(tags.contains("IRON_SOURCE"));
    assertTrue(tags.contains("CALCIUM_SOURCE"));
  }

  @Test
  @DisplayName("Excludes vegan and dairy-free when paneer is an ingredient")
  void testExcludesVeganWhenDairyPresent() {
    List<DietaryTagClassifier.IngredientDetail> ingredients =
        List.of(
            new DietaryTagClassifier.IngredientDetail(
                "paneer", "Fresh Cottage Cheese", "Dairy", List.of("milk"), List.of("VEGETARIAN")));

    DietaryTagClassifier.RecipeNutritionSnapshot nutrition =
        new DietaryTagClassifier.RecipeNutritionSnapshot(
            320.0, 16.0, 4.0, 22.0, 0.5, 180.0, 0.4, 200.0);

    List<String> tags = DietaryTagClassifier.classifyTags(ingredients, nutrition);

    assertTrue(tags.contains("VEGETARIAN"));
    assertFalse(tags.contains("VEGAN"));
    assertFalse(tags.contains("DAIRY_FREE"));
  }
}
