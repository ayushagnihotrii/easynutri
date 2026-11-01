package com.easynutri.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.easynutri.domain.model.RecipeCandidate;
import com.easynutri.domain.recipe.DefaultRecipeMatcher;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DefaultRecipeMatcherTest {

  @Test
  @DisplayName("Verify ingredient match score and threshold filtering")
  void testMatchScoreAndRanking() {
    DefaultRecipeMatcher matcher = new DefaultRecipeMatcher();

    RecipeCandidate khichdi =
        new RecipeCandidate(
            "r1",
            "Moong Khichdi",
            List.of("food_moong_dal", "food_rice", "food_ghee"),
            "DINNER",
            "MAIN",
            List.of());

    RecipeCandidate dal =
        new RecipeCandidate(
            "r2",
            "Tadka Dal",
            List.of("food_toor_dal", "food_tomato", "food_onion"),
            "LUNCH",
            "MAIN",
            List.of());

    Set<String> userPantry = Set.of("moong_dal", "rice");

    double khichdiScore = matcher.calculateMatchScore(userPantry, khichdi);
    // 2 out of 3 = 0.67
    assertEquals(0.67, khichdiScore, 0.05);

    double dalScore = matcher.calculateMatchScore(userPantry, dal);
    assertEquals(0.0, dalScore, 0.01);

    List<RecipeCandidate> matches = matcher.matchRecipes(userPantry, List.of(khichdi, dal), 0.5);
    assertEquals(1, matches.size());
    assertEquals("r1", matches.get(0).candidateId());
  }
}
