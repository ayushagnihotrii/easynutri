package com.easynutri.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.easynutri.domain.household.HouseholdDietaryAggregator;
import com.easynutri.domain.household.HouseholdDietaryAggregator.*;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HouseholdDietaryAggregatorTest {

  @Test
  @DisplayName("resolveMostRestrictiveDiet picks Jain over Vegan over Vegetarian over Non-Veg")
  void testResolveDietHierarchy() {
    var p1 = new MemberDietaryProfile(1L, "Alex", 30, "NON_VEG", Set.of(), Set.of(), 2000);
    var p2 = new MemberDietaryProfile(2L, "Sam", 28, "VEGETARIAN", Set.of(), Set.of(), 1800);
    var p3 = new MemberDietaryProfile(3L, "Maya", 25, "VEGAN", Set.of(), Set.of(), 1600);
    var p4 = new MemberDietaryProfile(4L, "Grandma", 70, "JAIN", Set.of(), Set.of(), 1500);

    assertEquals(
        "VEGETARIAN", HouseholdDietaryAggregator.resolveMostRestrictiveDiet(List.of(p1, p2)));
    assertEquals(
        "VEGAN", HouseholdDietaryAggregator.resolveMostRestrictiveDiet(List.of(p1, p2, p3)));
    assertEquals(
        "JAIN", HouseholdDietaryAggregator.resolveMostRestrictiveDiet(List.of(p1, p2, p3, p4)));
    assertEquals("NON_VEG", HouseholdDietaryAggregator.resolveMostRestrictiveDiet(List.of(p1)));
  }

  @Test
  @DisplayName("aggregateCombinedAllergens returns case-insensitive union of all member allergens")
  void testAggregateAllergens() {
    var p1 =
        new MemberDietaryProfile(
            1L, "Alex", 30, "VEG", Set.of("Peanut", "Tree Nuts"), Set.of(), 2000);
    var p2 =
        new MemberDietaryProfile(2L, "Sam", 28, "VEG", Set.of("Dairy", "peanut"), Set.of(), 1800);

    Set<String> allergens = HouseholdDietaryAggregator.aggregateCombinedAllergens(List.of(p1, p2));
    assertEquals(3, allergens.size());
    assertTrue(allergens.stream().anyMatch(a -> a.equalsIgnoreCase("Peanut")));
    assertTrue(allergens.stream().anyMatch(a -> a.equalsIgnoreCase("Tree Nuts")));
    assertTrue(allergens.stream().anyMatch(a -> a.equalsIgnoreCase("Dairy")));
  }

  @Test
  @DisplayName("evaluateHouseholdMeal hard-blocks conflicting allergens and dietary violations")
  void testEvaluateMealSafety() {
    var dad =
        new MemberDietaryProfile(1L, "Dad", 40, "VEGETARIAN", Set.of("Peanuts"), Set.of(), 2200);
    var mom = new MemberDietaryProfile(2L, "Mom", 38, "VEGAN", Set.of("Gluten"), Set.of(), 1800);

    // Recipe containing Peanuts
    var peanutCurry =
        new RecipeForEvaluation("r1", "Peanut Curry", Set.of("Peanuts"), "VEGAN", 400.0, 15.0);
    var eval1 = HouseholdDietaryAggregator.evaluateHouseholdMeal(List.of(dad, mom), peanutCurry);
    assertFalse(eval1.isSafe());
    assertTrue(eval1.safetyViolations().get(0).contains("Peanuts"));

    // Recipe that is Vegetarian (contains Dairy) but mom is Vegan
    var paneerDish =
        new RecipeForEvaluation(
            "r2", "Paneer Butter Masala", Set.of("Dairy"), "VEGETARIAN", 450.0, 18.0);
    var eval2 = HouseholdDietaryAggregator.evaluateHouseholdMeal(List.of(dad, mom), paneerDish);
    assertFalse(eval2.isSafe());
    assertTrue(eval2.safetyViolations().stream().anyMatch(v -> v.contains("VEGAN")));

    // Safe Vegan, Peanut-free, Gluten-free Lentil Dahl
    var lentilDahl =
        new RecipeForEvaluation("r3", "Yellow Moong Dahl", Set.of(), "VEGAN", 320.0, 14.0);
    var eval3 = HouseholdDietaryAggregator.evaluateHouseholdMeal(List.of(dad, mom), lentilDahl);
    assertTrue(eval3.isSafe());
    assertTrue(eval3.safetyViolations().isEmpty());
  }

  @Test
  @DisplayName("computeIndividualPortions scales servings by age and caloric targets")
  void testPortionCalculation() {
    var adult = new MemberDietaryProfile(1L, "Adult", 35, "VEG", Set.of(), Set.of(), 2000);
    var child = new MemberDietaryProfile(2L, "Kid", 6, "VEG", Set.of(), Set.of(), 1200);

    var recipe = new RecipeForEvaluation("r1", "Khichdi", Set.of(), "VEG", 350.0, 12.0);
    var portions =
        HouseholdDietaryAggregator.computeIndividualPortions(List.of(adult, child), recipe);

    assertEquals(2, portions.size());
    // Adult portion ~ 1.0 serving
    assertEquals(1.0, portions.get(0).recommendedServings());
    assertEquals(350.0, portions.get(0).individualCalories());

    // Child portion ~ 0.6 serving
    assertEquals(0.6, portions.get(1).recommendedServings());
    assertEquals(210.0, portions.get(1).individualCalories());
  }
}
