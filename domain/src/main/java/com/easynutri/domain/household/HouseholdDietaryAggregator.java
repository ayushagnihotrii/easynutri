package com.easynutri.domain.household;

import java.util.*;

/**
 * Pure Java domain utility for evaluating combined household dietary restrictions, hard-blocking
 * allergens across multiple members, resolving restrictive diets, and calculating individual
 * serving portions.
 */
public final class HouseholdDietaryAggregator {

  private HouseholdDietaryAggregator() {}

  public record MemberDietaryProfile(
      Long memberId,
      String displayName,
      int age,
      String dietaryPreference,
      Set<String> allergies,
      Set<String> healthConditions,
      int targetCalories) {
    public MemberDietaryProfile {
      allergies = allergies != null ? Set.copyOf(allergies) : Set.of();
      healthConditions = healthConditions != null ? Set.copyOf(healthConditions) : Set.of();
      dietaryPreference =
          dietaryPreference != null ? dietaryPreference.toUpperCase(Locale.ROOT) : "NON_VEG";
    }
  }

  public record HouseholdSafetyEvaluation(
      boolean isSafe,
      Set<String> blockedAllergens,
      String conflictingDiet,
      List<String> safetyViolations,
      String explanation) {}

  public record RecipeForEvaluation(
      String recipeId,
      String title,
      Set<String> recipeAllergens,
      String recipeDiet, // e.g. VEGAN, JAIN, VEGETARIAN, NON_VEG
      double baseCaloriesPerServing,
      double baseProteinPerServing) {}

  public record MemberPortion(
      Long memberId,
      String displayName,
      double recommendedServings,
      double individualCalories,
      double individualProteinG) {}

  /**
   * Resolves the most restrictive common diet among participating members. Hierarchy: JAIN > VEGAN
   * > VEGETARIAN > PESCATARIAN > NON_VEG.
   */
  public static String resolveMostRestrictiveDiet(List<MemberDietaryProfile> members) {
    if (members == null || members.isEmpty()) {
      return "NON_VEG";
    }

    boolean hasJain = false;
    boolean hasVegan = false;
    boolean hasVegetarian = false;

    for (MemberDietaryProfile m : members) {
      String diet = m.dietaryPreference();
      if ("JAIN".equalsIgnoreCase(diet)) {
        hasJain = true;
      } else if ("VEGAN".equalsIgnoreCase(diet)) {
        hasVegan = true;
      } else if ("VEGETARIAN".equalsIgnoreCase(diet) || "VEG".equalsIgnoreCase(diet)) {
        hasVegetarian = true;
      }
    }

    if (hasJain) return "JAIN";
    if (hasVegan) return "VEGAN";
    if (hasVegetarian) return "VEGETARIAN";
    return "NON_VEG";
  }

  /** Aggregates the union of all allergens across participating household members. */
  public static Set<String> aggregateCombinedAllergens(List<MemberDietaryProfile> members) {
    if (members == null || members.isEmpty()) {
      return Set.of();
    }
    Set<String> combined = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    for (MemberDietaryProfile m : members) {
      combined.addAll(m.allergies());
    }
    return Collections.unmodifiableSet(combined);
  }

  /** Evaluates whether a recipe is safe for all participating household members. */
  public static HouseholdSafetyEvaluation evaluateHouseholdMeal(
      List<MemberDietaryProfile> members, RecipeForEvaluation recipe) {

    if (members == null || members.isEmpty()) {
      return new HouseholdSafetyEvaluation(
          true, Set.of(), null, List.of(), "No members selected; recipe is universally safe.");
    }

    Set<String> combinedAllergens = aggregateCombinedAllergens(members);
    String mostRestrictiveDiet = resolveMostRestrictiveDiet(members);

    List<String> violations = new ArrayList<>();
    Set<String> matchedAllergens = new HashSet<>();

    // 1. Hard-Block Allergens Check
    if (recipe.recipeAllergens() != null) {
      for (String recipeAllergen : recipe.recipeAllergens()) {
        for (String memberAllergen : combinedAllergens) {
          if (recipeAllergen.equalsIgnoreCase(memberAllergen)
              || recipeAllergen
                  .toLowerCase(Locale.ROOT)
                  .contains(memberAllergen.toLowerCase(Locale.ROOT))
              || memberAllergen
                  .toLowerCase(Locale.ROOT)
                  .contains(recipeAllergen.toLowerCase(Locale.ROOT))) {
            matchedAllergens.add(memberAllergen);
            // Identify which members are affected
            List<String> affectedMembers =
                members.stream()
                    .filter(
                        m ->
                            m.allergies().stream()
                                .anyMatch(a -> a.equalsIgnoreCase(memberAllergen)))
                    .map(MemberDietaryProfile::displayName)
                    .toList();
            violations.add(
                "Recipe contains allergen '"
                    + recipeAllergen
                    + "' which affects: "
                    + String.join(", ", affectedMembers));
          }
        }
      }
    }

    // 2. Restrictive Diet Hierarchy Check
    String recipeDiet =
        recipe.recipeDiet() != null ? recipe.recipeDiet().toUpperCase(Locale.ROOT) : "NON_VEG";
    boolean dietViolation = false;
    String dietReason = null;

    if ("JAIN".equals(mostRestrictiveDiet) && !"JAIN".equals(recipeDiet)) {
      dietViolation = true;
      dietReason =
          "Household requires JAIN meal (selected members include Jain preference), but recipe is "
              + recipeDiet;
      violations.add(dietReason);
    } else if ("VEGAN".equals(mostRestrictiveDiet)
        && (!"VEGAN".equals(recipeDiet) && !"JAIN".equals(recipeDiet))) {
      dietViolation = true;
      dietReason = "Household requires VEGAN meal, but recipe is " + recipeDiet;
      violations.add(dietReason);
    } else if ("VEGETARIAN".equals(mostRestrictiveDiet)
        && (!"VEGETARIAN".equals(recipeDiet)
            && !"VEGAN".equals(recipeDiet)
            && !"JAIN".equals(recipeDiet))) {
      dietViolation = true;
      dietReason = "Household requires VEGETARIAN meal, but recipe is " + recipeDiet;
      violations.add(dietReason);
    }

    boolean isSafe = violations.isEmpty();
    String explanation =
        isSafe
            ? "Recipe safely satisfies all combined allergens and dietary constraints for "
                + members.size()
                + " member(s)."
            : "Recipe blocked for household meal: " + String.join(" | ", violations);

    return new HouseholdSafetyEvaluation(
        isSafe,
        matchedAllergens,
        dietViolation ? mostRestrictiveDiet : null,
        violations,
        explanation);
  }

  /** Computes individual serving portions and personal nutrition for each participating member. */
  public static List<MemberPortion> computeIndividualPortions(
      List<MemberDietaryProfile> members, RecipeForEvaluation recipe) {

    if (members == null || members.isEmpty()) {
      return List.of();
    }

    List<MemberPortion> portions = new ArrayList<>();
    for (MemberDietaryProfile m : members) {
      double scaleFactor;
      if (m.age() > 0 && m.age() < 10) {
        scaleFactor = 0.6; // Child portion
      } else if (m.age() >= 10 && m.age() < 16) {
        scaleFactor = 0.85; // Adolescent portion
      } else if (m.targetCalories() > 0) {
        scaleFactor = Math.max(0.5, Math.min(2.0, m.targetCalories() / 2000.0));
      } else {
        scaleFactor = 1.0; // Standard adult portion
      }

      double roundedServings = Math.round(scaleFactor * 10.0) / 10.0;
      double indCalories = Math.round(recipe.baseCaloriesPerServing() * roundedServings);
      double indProtein =
          Math.round(recipe.baseProteinPerServing() * roundedServings * 10.0) / 10.0;

      portions.add(
          new MemberPortion(
              m.memberId(), m.displayName(), roundedServings, indCalories, indProtein));
    }

    return portions;
  }
}
