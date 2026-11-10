package com.easynutri.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.easynutri.domain.model.RecipeCandidate;
import com.easynutri.domain.recipe.DefaultRecipeMatcher;
import com.easynutri.domain.recipe.RecipeMatchLabel;
import com.easynutri.domain.recipe.RecipeMatchResult;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EnhancedRecipeMatcherTest {

  private final DefaultRecipeMatcher matcher = new DefaultRecipeMatcher();

  @Test
  @DisplayName("Classify READY_NOW when all ingredients are present")
  void testReadyNowClassification() {
    RecipeCandidate khichdi =
        new RecipeCandidate(
            "r_khichdi",
            "Moong Khichdi",
            List.of("food_cooked_rice", "food_moong_dal"),
            "DINNER",
            "MAIN",
            List.of());

    Set<String> pantry = Set.of("food_cooked_rice", "food_moong_dal");
    List<RecipeMatchResult> results =
        matcher.matchRecipesDetailed(pantry, List.of(khichdi), Set.of());

    assertEquals(1, results.size());
    RecipeMatchResult result = results.get(0);
    assertEquals(RecipeMatchLabel.READY_NOW, result.matchLabel());
    assertEquals(1.0, result.matchScore(), 0.01);
    assertTrue(result.missingIngredients().isEmpty());
  }

  @Test
  @DisplayName("Classify READY_WITH_SWAP when substitute ingredient is available")
  void testReadyWithSwapClassification() {
    RecipeCandidate paneerBhurji =
        new RecipeCandidate(
            "r_bhurji",
            "Paneer Bhurji",
            List.of("food_paneer", "food_onion", "food_tomato"),
            "DINNER",
            "MAIN",
            List.of());

    // User has tofu instead of paneer
    Set<String> pantry = Set.of("tofu", "onion", "tomato");
    List<RecipeMatchResult> results =
        matcher.matchRecipesDetailed(pantry, List.of(paneerBhurji), Set.of());

    assertEquals(1, results.size());
    RecipeMatchResult result = results.get(0);
    assertEquals(RecipeMatchLabel.READY_WITH_SWAP, result.matchLabel());
    assertFalse(result.suggestedSwaps().isEmpty());
    assertTrue(result.suggestedSwaps().get(0).contains("Swap tofu for paneer"));
  }

  @Test
  @DisplayName("Classify MISSING_ONE and MISSING_TWO accurately")
  void testMissingCountClassification() {
    RecipeCandidate dalMakhani =
        new RecipeCandidate(
            "r_makhani",
            "Dal Makhani",
            List.of("food_rajma", "food_toor_dal", "food_tomato", "food_onion"),
            "DINNER",
            "MAIN",
            List.of());

    // User has 3 of 4 ingredients
    Set<String> pantry1 = Set.of("food_rajma", "food_toor_dal", "food_tomato");
    List<RecipeMatchResult> results1 =
        matcher.matchRecipesDetailed(pantry1, List.of(dalMakhani), Set.of());
    assertEquals(RecipeMatchLabel.MISSING_ONE, results1.get(0).matchLabel());
    assertEquals(1, results1.get(0).missingIngredients().size());

    // User has 2 of 4 ingredients
    Set<String> pantry2 = Set.of("food_rajma", "food_toor_dal");
    List<RecipeMatchResult> results2 =
        matcher.matchRecipesDetailed(pantry2, List.of(dalMakhani), Set.of());
    assertEquals(RecipeMatchLabel.MISSING_TWO, results2.get(0).matchLabel());
    assertEquals(2, results2.get(0).missingIngredients().size());
  }

  @Test
  @DisplayName("Prioritize leftover recipes with bonus score")
  void testLeftoverPrioritization() {
    RecipeCandidate friedRice =
        new RecipeCandidate(
            "r_fried_rice",
            "Vegetable Fried Rice",
            List.of("food_cooked_rice", "food_onion"),
            "DINNER",
            "MAIN",
            List.of());

    Set<String> pantry = Set.of("food_cooked_rice", "food_onion");
    Set<String> leftovers = Set.of("food_cooked_rice");

    List<RecipeMatchResult> results =
        matcher.matchRecipesDetailed(pantry, List.of(friedRice), leftovers);
    assertEquals(1, results.size());
    assertTrue(results.get(0).usesLeftover());
    assertEquals(1.0, results.get(0).matchScore());
  }
}
