package com.easynutri.domain.rules;

import com.easynutri.domain.model.FoodCandidate;
import com.easynutri.domain.model.ProfileContext;
import com.easynutri.domain.model.RecipeCandidate;
import com.easynutri.domain.model.SafetyCandidate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Deterministic pure Java clinical rules engine. Evaluates candidates against allergies (Hard
 * BLOCK), dietary types, and medical condition rules. Priority order: BLOCK > WARN > ALLOW.
 */
public final class DefaultRulesEngine implements RulesEngine {

  // Common Indian and international allergen synonym lookup
  private static final Map<String, Set<String>> ALLERGEN_SYNONYMS =
      Map.of(
          "peanut", Set.of("peanut", "groundnut", "moongphali", "mungphali", "arachis"),
          "dairy",
              Set.of(
                  "dairy",
                  "milk",
                  "paneer",
                  "curd",
                  "dahi",
                  "cheese",
                  "ghee",
                  "butter",
                  "whey",
                  "lactose",
                  "makhana_cheese"),
          "gluten",
              Set.of("gluten", "wheat", "atta", "maida", "sooji", "suji", "rava", "barley", "rye"),
          "soy", Set.of("soy", "soya", "tofu", "edamame"),
          "egg", Set.of("egg", "anda", "albumin"),
          "shellfish", Set.of("shellfish", "prawn", "shrimp", "crab", "lobster"));

  @Override
  public SafetyResult evaluateFood(ProfileContext profile, FoodCandidate candidate) {
    Objects.requireNonNull(profile, "profile cannot be null");
    Objects.requireNonNull(candidate, "candidate cannot be null");

    List<String> blockReasons = new ArrayList<>();
    List<String> warnReasons = new ArrayList<>();
    List<String> suggestedSwaps = new ArrayList<>();

    // 1. Hard Allergen Verification
    checkAllergens(profile.allergies(), candidate, blockReasons, suggestedSwaps);

    // 2. Dietary Preference Verification
    checkDietaryPreference(profile.dietType(), candidate, blockReasons, suggestedSwaps);

    // 3. Clinical Health Condition Rules
    checkHealthConditions(profile.healthConditions(), candidate, blockReasons, warnReasons);

    if (!blockReasons.isEmpty()) {
      return new SafetyResult(RuleVerdict.BLOCK, blockReasons, suggestedSwaps);
    }
    if (!warnReasons.isEmpty()) {
      return new SafetyResult(RuleVerdict.WARN, warnReasons, suggestedSwaps);
    }
    return SafetyResult.allow();
  }

  @Override
  public SafetyResult evaluateRecipe(ProfileContext profile, RecipeCandidate candidate) {
    Objects.requireNonNull(profile, "profile cannot be null");
    Objects.requireNonNull(candidate, "candidate cannot be null");

    List<String> blockReasons = new ArrayList<>();
    List<String> warnReasons = new ArrayList<>();
    List<String> suggestedSwaps = new ArrayList<>();

    // Check recipe ingredient IDs against allergies
    for (String allergy : profile.allergies()) {
      String cleanAllergy = allergy.trim().toLowerCase(Locale.ROOT);
      Set<String> synonyms = ALLERGEN_SYNONYMS.getOrDefault(cleanAllergy, Set.of(cleanAllergy));

      for (String ingredientId : candidate.ingredientFoodIds()) {
        String cleanIng = ingredientId.toLowerCase(Locale.ROOT);
        for (String syn : synonyms) {
          if (cleanIng.contains(syn)) {
            blockReasons.add(
                String.format(
                    "Allergy Hard-Block: Recipe contains ingredient '%s' which conflicts with diagnosed '%s' allergy.",
                    ingredientId, allergy));
            suggestedSwaps.add(
                "Substitute " + ingredientId + " with a certified allergen-free alternative.");
            break;
          }
        }
      }
    }

    if (!blockReasons.isEmpty()) {
      return new SafetyResult(RuleVerdict.BLOCK, blockReasons, suggestedSwaps);
    }
    return SafetyResult.allow();
  }

  @Override
  public SafetyResult evaluate(ProfileContext profile, SafetyCandidate candidate) {
    if (candidate instanceof FoodCandidate food) {
      return evaluateFood(profile, food);
    } else if (candidate instanceof RecipeCandidate recipe) {
      return evaluateRecipe(profile, recipe);
    }
    throw new IllegalArgumentException(
        "Unsupported safety candidate type: " + candidate.getClass().getName());
  }

  private void checkAllergens(
      List<String> userAllergies,
      FoodCandidate candidate,
      List<String> blockReasons,
      List<String> suggestedSwaps) {

    for (String allergy : userAllergies) {
      String cleanAllergy = allergy.trim().toLowerCase(Locale.ROOT);
      Set<String> synonyms = ALLERGEN_SYNONYMS.getOrDefault(cleanAllergy, Set.of(cleanAllergy));

      // Match against candidate allergen tags
      boolean tagMatch =
          candidate.allergenTags().stream()
              .map(t -> t.toLowerCase(Locale.ROOT))
              .anyMatch(t -> synonyms.contains(t) || t.contains(cleanAllergy));

      // Match against candidate name or id
      boolean textMatch =
          synonyms.stream()
              .anyMatch(
                  syn ->
                      candidate.candidateName().toLowerCase(Locale.ROOT).contains(syn)
                          || candidate.candidateId().toLowerCase(Locale.ROOT).contains(syn));

      if (tagMatch || textMatch) {
        blockReasons.add(
            String.format(
                "Allergy Hard-Block: '%s' contains allergen '%s'. Clinical rule blocks consumption to prevent allergic response.",
                candidate.candidateName(), allergy));

        if (cleanAllergy.contains("dairy")) {
          suggestedSwaps.add("Plant-based alternatives (Oat milk, Soy curd, Tofu)");
        } else if (cleanAllergy.contains("peanut")) {
          suggestedSwaps.add("Seed butter alternatives (Sunflower seed butter, Tahini)");
        } else if (cleanAllergy.contains("gluten")) {
          suggestedSwaps.add("Gluten-free grains (Brown Rice, Quinoa, Millet, Jowar)");
        }
      }
    }
  }

  private void checkDietaryPreference(
      String dietType,
      FoodCandidate candidate,
      List<String> blockReasons,
      List<String> suggestedSwaps) {

    if (dietType == null || dietType.isBlank()) {
      return;
    }
    String cleanDiet = dietType.trim().toUpperCase(Locale.ROOT);

    if (cleanDiet.contains("VEGAN")) {
      boolean hasDairyOrMeat =
          candidate.allergenTags().contains("DAIRY")
              || candidate.allergenTags().contains("EGG")
              || candidate.dietaryFlags().contains("NON_VEG");
      if (hasDairyOrMeat) {
        blockReasons.add(
            "Dietary Block: Vegan preference strictly excludes animal-derived or dairy ingredients.");
        suggestedSwaps.add("Vegan equivalent alternative");
      }
    } else if (cleanDiet.contains("VEGETARIAN")) {
      if (candidate.dietaryFlags().contains("NON_VEG")) {
        blockReasons.add("Dietary Block: Vegetarian diet excludes meat, fish, and poultry.");
        suggestedSwaps.add("Vegetarian paneer or legume protein alternative");
      }
    } else if (cleanDiet.contains("JAIN")) {
      boolean hasRootVeg =
          candidate
                  .candidateName()
                  .toLowerCase(Locale.ROOT)
                  .matches(".*(onion|garlic|potato|carrot|radish).*")
              || candidate.dietaryFlags().contains("ROOT_VEG");
      if (hasRootVeg) {
        blockReasons.add(
            "Dietary Block: Jain diet excludes root vegetables (onions, garlic, potatoes).");
        suggestedSwaps.add("Raw banana or tomato-cumin based seasoning");
      }
    }
  }

  private void checkHealthConditions(
      List<String> conditions,
      FoodCandidate candidate,
      List<String> blockReasons,
      List<String> warnReasons) {

    Map<String, Double> n = candidate.nutrientsPer100g();
    double sodium = n.getOrDefault("sodium_mg", 0.0);
    double sugar = n.getOrDefault("sugar_g", 0.0);
    double satFat = n.getOrDefault("saturated_fat_g", 0.0);
    double potassium = n.getOrDefault("potassium_mg", 0.0);

    for (String condition : conditions) {
      String cleanCond = condition.trim().toUpperCase(Locale.ROOT);

      if (cleanCond.contains("HYPERTENSION") || cleanCond.contains("HIGH_BP")) {
        if (sodium > 800.0) {
          blockReasons.add(
              String.format(
                  "Hypertension Rule Block: Sodium content (%.0f mg/100g) exceeds safety limit for high blood pressure.",
                  sodium));
        } else if (sodium > 400.0) {
          warnReasons.add(
              String.format(
                  "Hypertension Guidance: Elevated sodium (%.0f mg/100g). Monitor total daily sodium intake (<2000 mg).",
                  sodium));
        }
      }

      if (cleanCond.contains("DIABETES")) {
        if (sugar > 25.0) {
          blockReasons.add(
              String.format(
                  "Diabetes Rule Block: High simple sugar content (%.1f g/100g) risks severe hyperglycemia.",
                  sugar));
        } else if (sugar > 10.0) {
          warnReasons.add(
              String.format(
                  "Diabetes Guidance: Moderate sugar content (%.1f g/100g). Pair with dietary fiber and complex carbs.",
                  sugar));
        }
      }

      if (cleanCond.contains("KIDNEY") || cleanCond.contains("CKD")) {
        if (potassium > 450.0) {
          warnReasons.add(
              String.format(
                  "Renal Guidance: High potassium (%.0f mg/100g). Requires clinical kidney function monitoring.",
                  potassium));
        }
      }

      if (cleanCond.contains("CHOLESTEROL") || cleanCond.contains("HEART")) {
        if (satFat > 8.0) {
          warnReasons.add(
              String.format(
                  "Cardiovascular Guidance: Elevated saturated fat (%.1f g/100g). ICMR recommends limiting saturated fat.",
                  satFat));
        }
      }
    }
  }
}
