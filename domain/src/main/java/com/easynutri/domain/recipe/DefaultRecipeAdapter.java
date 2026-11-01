package com.easynutri.domain.recipe;

import com.easynutri.domain.model.ProfileContext;
import com.easynutri.domain.model.RecipeCandidate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Pure Java implementation of recipe adaptation and serving scaling. */
public final class DefaultRecipeAdapter implements RecipeAdapter {

  @Override
  public RecipeCandidate adaptRecipe(
      RecipeCandidate recipe, ProfileContext profile, Map<String, String> substitutionDatabase) {
    Objects.requireNonNull(recipe, "recipe cannot be null");
    Objects.requireNonNull(profile, "profile cannot be null");
    Map<String, String> subs = substitutionDatabase != null ? substitutionDatabase : Map.of();

    List<String> updatedIngredients = new ArrayList<>();
    List<String> updatedTags = new ArrayList<>(recipe.derivedTags());
    boolean wasAdapted = false;

    for (String foodId : recipe.ingredientFoodIds()) {
      boolean hasAllergy =
          profile.allergies().stream()
              .anyMatch(allergy -> foodId.toLowerCase().contains(allergy.toLowerCase()));

      if (hasAllergy && subs.containsKey(foodId)) {
        String replacement = subs.get(foodId);
        updatedIngredients.add(replacement);
        wasAdapted = true;
      } else {
        updatedIngredients.add(foodId);
      }
    }

    if (wasAdapted) {
      updatedTags.add("ADAPTED_SAFE");
    }

    return new RecipeCandidate(
        recipe.candidateId(),
        recipe.candidateName() + (wasAdapted ? " (Adapted)" : ""),
        updatedIngredients,
        recipe.mealSlot(),
        recipe.mealRole(),
        updatedTags);
  }

  @Override
  public RecipeCandidate scaleServings(RecipeCandidate recipe, int targetServings) {
    Objects.requireNonNull(recipe, "recipe cannot be null");
    if (targetServings <= 0) {
      throw new IllegalArgumentException(
          "Target servings must be at least 1. Provided: " + targetServings);
    }

    List<String> tags = new ArrayList<>(recipe.derivedTags());
    tags.removeIf(t -> t.startsWith("SERVINGS_"));
    tags.add("SERVINGS_" + targetServings);

    return new RecipeCandidate(
        recipe.candidateId(),
        recipe.candidateName(),
        recipe.ingredientFoodIds(),
        recipe.mealSlot(),
        recipe.mealRole(),
        tags);
  }
}
