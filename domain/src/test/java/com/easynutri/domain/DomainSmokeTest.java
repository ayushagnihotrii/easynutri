package com.easynutri.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.easynutri.domain.model.FoodCandidate;
import com.easynutri.domain.model.ProfileContext;
import com.easynutri.domain.model.RecipeCandidate;
import com.easynutri.domain.rules.RuleVerdict;
import com.easynutri.domain.rules.SafetyResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DomainSmokeTest {

  @Test
  @DisplayName("Verify FoodCandidate record immutability and defensive copying")
  void verifyFoodCandidateImmutability() {
    List<String> allergens = new ArrayList<>();
    allergens.add("DAIRY");

    FoodCandidate candidate =
        new FoodCandidate(
            "food_1", "Paneer", "DAIRY", Map.of("protein_g", 18.0), allergens, List.of("VEG"));

    // Modifying the original list must not affect the record
    allergens.add("PEANUT");
    assertEquals(1, candidate.allergenTags().size());
    assertEquals("DAIRY", candidate.allergenTags().get(0));

    // Attempting to modify the returned list must throw UnsupportedOperationException
    assertThrows(UnsupportedOperationException.class, () -> candidate.allergenTags().add("SOY"));
  }

  @Test
  @DisplayName("Verify RecipeCandidate record creation")
  void verifyRecipeCandidateCreation() {
    RecipeCandidate recipe =
        new RecipeCandidate(
            "recipe_1",
            "Khichdi",
            List.of("food_moong_dal", "food_rice"),
            "DINNER",
            "MAIN",
            List.of("comfort_food"));

    assertEquals("recipe_1", recipe.candidateId());
    assertEquals("Khichdi", recipe.candidateName());
    assertEquals(2, recipe.ingredientFoodIds().size());
  }

  @Test
  @DisplayName("Verify ProfileContext creation and immutability")
  void verifyProfileContext() {
    ProfileContext profile =
        new ProfileContext(
            "user_123",
            28,
            "FEMALE",
            165.0,
            60.0,
            "VEG",
            List.of("PEANUT"),
            List.of("HYPERTENSION"));

    assertEquals("user_123", profile.profileId());
    assertEquals(28, profile.age());
    assertEquals(1, profile.allergies().size());
  }

  @Test
  @DisplayName("Verify SafetyResult factory methods and verdict behavior")
  void verifySafetyResultBehavior() {
    SafetyResult allow = SafetyResult.allow();
    assertTrue(allow.isSafe());
    assertEquals(RuleVerdict.ALLOW, allow.verdict());

    SafetyResult block = SafetyResult.block("Contains undeclared allergen");
    assertFalse(block.isSafe());
    assertEquals(RuleVerdict.BLOCK, block.verdict());
    assertEquals(1, block.reasons().size());

    SafetyResult warn = SafetyResult.warn("High in sodium for hypertension profile");
    assertTrue(warn.isSafe());
    assertEquals(RuleVerdict.WARN, warn.verdict());
  }
}
