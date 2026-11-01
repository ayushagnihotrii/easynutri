package com.easynutri.domain.recipe;

import com.easynutri.domain.model.ProfileContext;
import com.easynutri.domain.model.RecipeCandidate;
import java.util.Map;

/**
 * Contract for adapting recipes and substituting ingredients to meet dietary and allergy
 * constraints.
 */
public interface RecipeAdapter {

  /**
   * Adapts a recipe by substituting allergenic or restricted ingredients.
   *
   * @param recipe original recipe candidate
   * @param profile user's allergy and clinical constraints
   * @param substitutionDatabase known safe ingredient substitution rules
   * @return adapted recipe candidate verified against safety rules
   */
  RecipeCandidate adaptRecipe(
      RecipeCandidate recipe, ProfileContext profile, Map<String, String> substitutionDatabase);

  /**
   * Scales ingredient measurements and nutritional facts by serving ratio.
   *
   * @param recipe base recipe
   * @param targetServings desired number of servings
   * @return scaled recipe candidate
   */
  RecipeCandidate scaleServings(RecipeCandidate recipe, int targetServings);
}
