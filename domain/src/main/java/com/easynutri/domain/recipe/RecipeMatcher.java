package com.easynutri.domain.recipe;

import com.easynutri.domain.model.RecipeCandidate;
import java.util.List;
import java.util.Set;

/** Contract for ingredient-based recipe discovery and pantry leftover matching. */
public interface RecipeMatcher {

  /**
   * Matches candidate recipes against a set of user-provided ingredients.
   *
   * @param availableIngredients set of normalized ingredient names or IDs
   * @param candidates candidate recipes to evaluate
   * @param minMatchPercentage minimum percentage of required ingredients matched (0.0 to 1.0)
   * @return ranked list of matching recipes
   */
  List<RecipeCandidate> matchRecipes(
      Set<String> availableIngredients,
      List<RecipeCandidate> candidates,
      double minMatchPercentage);

  /**
   * Calculates the ingredient match score for a single recipe.
   *
   * @param availableIngredients user's available ingredients
   * @param candidate candidate recipe
   * @return score between 0.0 (no match) and 1.0 (exact match)
   */
  double calculateMatchScore(Set<String> availableIngredients, RecipeCandidate candidate);
}
