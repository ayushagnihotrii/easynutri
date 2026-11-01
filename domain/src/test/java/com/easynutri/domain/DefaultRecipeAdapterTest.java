package com.easynutri.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.easynutri.domain.model.ProfileContext;
import com.easynutri.domain.model.RecipeCandidate;
import com.easynutri.domain.recipe.DefaultRecipeAdapter;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DefaultRecipeAdapterTest {

  @Test
  @DisplayName("Verify serving scaling updates tags and retains ingredient list")
  void testScaleServings() {
    DefaultRecipeAdapter adapter = new DefaultRecipeAdapter();
    RecipeCandidate base =
        new RecipeCandidate(
            "r1",
            "Moong Khichdi",
            List.of("food_moong_dal", "food_rice"),
            "DINNER",
            "MAIN",
            List.of());

    RecipeCandidate scaled = adapter.scaleServings(base, 4);

    assertEquals("r1", scaled.candidateId());
    assertEquals("Moong Khichdi", scaled.candidateName());
    assertTrue(scaled.derivedTags().contains("SERVINGS_4"));
  }

  @Test
  @DisplayName("Verify ingredient substitution for allergic user")
  void testRecipeAdaptation() {
    DefaultRecipeAdapter adapter = new DefaultRecipeAdapter();
    RecipeCandidate base =
        new RecipeCandidate(
            "r2",
            "Paneer Bhurji",
            List.of("food_dairy_paneer", "food_onion"),
            "LUNCH",
            "MAIN",
            List.of());

    ProfileContext profile =
        new ProfileContext("p1", 25, "FEMALE", 165.0, 60.0, "VEGAN", List.of("dairy"), List.of());

    Map<String, String> substitutions = Map.of("food_dairy_paneer", "food_tofu");

    RecipeCandidate adapted = adapter.adaptRecipe(base, profile, substitutions);

    assertTrue(adapted.candidateName().contains("(Adapted)"));
    assertTrue(adapted.ingredientFoodIds().contains("food_tofu"));
    assertFalse(adapted.ingredientFoodIds().contains("food_dairy_paneer"));
    assertTrue(adapted.derivedTags().contains("ADAPTED_SAFE"));
  }
}
