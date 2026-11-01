package com.easynutri.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.easynutri.domain.model.FoodCandidate;
import com.easynutri.domain.model.ProfileContext;
import com.easynutri.domain.model.RecipeCandidate;
import com.easynutri.domain.rules.DefaultRulesEngine;
import com.easynutri.domain.rules.RuleVerdict;
import com.easynutri.domain.rules.SafetyResult;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DefaultRulesEngineTest {

  private final DefaultRulesEngine engine = new DefaultRulesEngine();

  @Test
  @DisplayName("Verify allergen synonym matching triggers hard BLOCK for peanut / moongphali")
  void testAllergenHardBlock() {
    ProfileContext profile =
        new ProfileContext(
            "u1", 28, "MALE", 175.0, 72.0, "VEGETARIAN", List.of("peanut"), List.of());

    FoodCandidate peanutChikki =
        new FoodCandidate(
            "food_moongphali_chikki",
            "Moongphali (Peanut) Chikki",
            "SNACK",
            Map.of("energy_kcal", 450.0, "sugar_g", 35.0),
            List.of("PEANUT"),
            List.of("VEG"));

    SafetyResult result = engine.evaluateFood(profile, peanutChikki);

    assertEquals(RuleVerdict.BLOCK, result.verdict());
    assertFalse(result.isSafe());
    assertTrue(result.reasons().get(0).contains("Allergy Hard-Block"));
    assertFalse(result.suggestedSwaps().isEmpty());
  }

  @Test
  @DisplayName("Verify dietary preference block: non-veg item blocked for vegetarian user")
  void testVegetarianBlock() {
    ProfileContext profile =
        new ProfileContext("u2", 35, "FEMALE", 160.0, 62.0, "VEGETARIAN", List.of(), List.of());

    FoodCandidate chickenCurry =
        new FoodCandidate(
            "food_chicken_curry",
            "Chicken Curry",
            "MEAT",
            Map.of("energy_kcal", 240.0, "protein_g", 25.0),
            List.of(),
            List.of("NON_VEG"));

    SafetyResult result = engine.evaluateFood(profile, chickenCurry);
    assertEquals(RuleVerdict.BLOCK, result.verdict());
    assertTrue(result.reasons().get(0).contains("Vegetarian"));
  }

  @Test
  @DisplayName(
      "Verify clinical health condition rule: excessive sodium blocked or warned for hypertension")
  void testHypertensionSodiumRules() {
    ProfileContext profile =
        new ProfileContext(
            "u3", 55, "MALE", 170.0, 85.0, "VEGETARIAN", List.of(), List.of("HYPERTENSION"));

    FoodCandidate saltyPickle =
        new FoodCandidate(
            "food_mango_pickle",
            "Spicy Mango Pickle",
            "CONDIMENT",
            Map.of("energy_kcal", 120.0, "sodium_mg", 950.0),
            List.of(),
            List.of("VEG"));

    SafetyResult result = engine.evaluateFood(profile, saltyPickle);
    assertEquals(RuleVerdict.BLOCK, result.verdict());
    assertTrue(result.reasons().get(0).contains("Hypertension Rule Block"));
  }

  @Test
  @DisplayName("Verify recipe evaluation blocks recipe containing allergenic ingredient")
  void testRecipeAllergyBlock() {
    ProfileContext profile =
        new ProfileContext(
            "u4", 24, "FEMALE", 168.0, 58.0, "VEGETARIAN", List.of("dairy"), List.of());

    RecipeCandidate kheer =
        new RecipeCandidate(
            "r_kheer",
            "Rice Kheer",
            List.of("food_rice", "food_dairy_milk", "food_sugar"),
            "DESSERT",
            "DESSERT",
            List.of());

    SafetyResult result = engine.evaluateRecipe(profile, kheer);
    assertEquals(RuleVerdict.BLOCK, result.verdict());
    assertTrue(result.reasons().get(0).contains("food_dairy_milk"));
  }
}
