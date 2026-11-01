package com.easynutri.domain.grocery;

import com.easynutri.domain.model.RecipeCandidate;
import java.util.List;
import java.util.Map;

/**
 * Contract for compiling smart grocery lists by subtracting pantry inventory from planned meals.
 */
public interface GroceryBuilder {

  /**
   * Compiles missing ingredients required for scheduled recipes minus current pantry stock.
   *
   * @param plannedRecipes recipes planned for the period
   * @param pantryStock available pantry item names and quantities
   * @return map of missing item names to required purchase quantities
   */
  Map<String, Double> buildGroceryList(
      List<RecipeCandidate> plannedRecipes, Map<String, Double> pantryStock);

  /**
   * Consolidates redundant ingredient measurements across multiple recipes.
   *
   * @param ingredientDemands list of ingredient requirements
   * @return consolidated unique ingredient map
   */
  Map<String, Double> consolidateIngredients(List<Map<String, Double>> ingredientDemands);
}
