package com.easynutri.service;

import com.easynutri.domain.nutrition.DefaultSafetyGate;
import com.easynutri.domain.nutrition.SafetyGate;
import com.easynutri.dto.RecipeAdaptationDto;
import com.easynutri.dto.RecipeDetailDto;
import com.easynutri.entity.*;
import com.easynutri.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RecipeAdaptationService {

  private final RecipeRepository recipeRepository;
  private final RecipeAdaptationRepository adaptationRepository;
  private final FoodRepository foodRepository;
  private final UserRepository userRepository;
  private final UserProfileRepository profileRepository;
  private final RecipeService recipeService;
  private final SafetyGate safetyGate = new DefaultSafetyGate();
  private final ObjectMapper objectMapper = new ObjectMapper();

  public RecipeAdaptationService(
      RecipeRepository recipeRepository,
      RecipeAdaptationRepository adaptationRepository,
      FoodRepository foodRepository,
      UserRepository userRepository,
      UserProfileRepository profileRepository,
      RecipeService recipeService) {
    this.recipeRepository = recipeRepository;
    this.adaptationRepository = adaptationRepository;
    this.foodRepository = foodRepository;
    this.userRepository = userRepository;
    this.profileRepository = profileRepository;
    this.recipeService = recipeService;
  }

  @Transactional(readOnly = true)
  public RecipeAdaptationDto previewAdaptation(
      Long userId, String recipeId, String adaptationType, String customPrompt) {
    RecipeEntity recipe =
        recipeRepository
            .findById(recipeId)
            .orElseThrow(() -> new IllegalArgumentException("Recipe not found: " + recipeId));

    Optional<UserProfileEntity> profileOpt =
        userId != null ? profileRepository.findByUserId(userId) : Optional.empty();

    return computeAdaptation(recipe, profileOpt.orElse(null), adaptationType, customPrompt, null);
  }

  public RecipeAdaptationDto saveAdaptation(
      Long userId, String recipeId, String adaptationType, String customPrompt) {
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    RecipeEntity recipe =
        recipeRepository
            .findById(recipeId)
            .orElseThrow(() -> new IllegalArgumentException("Recipe not found: " + recipeId));

    UserProfileEntity profile = profileRepository.findByUserId(userId).orElse(null);

    RecipeAdaptationDto preview =
        computeAdaptation(recipe, profile, adaptationType, customPrompt, null);

    String modificationsJson;
    try {
      modificationsJson = objectMapper.writeValueAsString(preview.ingredientDiffs());
    } catch (Exception e) {
      modificationsJson = "[]";
    }

    String instructions =
        recipe.getInstructionsText() != null
            ? recipe.getInstructionsText()
            : "Follow standard recipe steps.";

    RecipeAdaptationEntity entity =
        new RecipeAdaptationEntity(
            user,
            recipe,
            preview.adaptationTitle(),
            preview.adaptationType(),
            modificationsJson,
            instructions,
            preview.adaptedCalories(),
            preview.adaptedProtein(),
            preview.adaptedCarbs(),
            preview.adaptedFat(),
            preview.adaptedFiber(),
            preview.adaptedSodium(),
            preview.safetyVerdict());

    RecipeAdaptationEntity saved = adaptationRepository.save(entity);
    return computeAdaptation(recipe, profile, adaptationType, customPrompt, saved.getId());
  }

  @Transactional(readOnly = true)
  public List<RecipeAdaptationEntity> getUserAdaptations(Long userId) {
    return adaptationRepository.findByUserIdOrderByCreatedAtDesc(userId);
  }

  @Transactional(readOnly = true)
  public Optional<RecipeAdaptationEntity> getAdaptationById(Long adaptationId) {
    return adaptationRepository.findById(adaptationId);
  }

  private RecipeAdaptationDto computeAdaptation(
      RecipeEntity recipe,
      UserProfileEntity profile,
      String type,
      String customPrompt,
      Long savedId) {
    String cleanType = type != null ? type.toUpperCase(Locale.ROOT) : "CUSTOM";
    RecipeDetailDto original =
        recipeService.computeScaledNutrition(recipe, recipe.getBaseServings());

    List<RecipeAdaptationDto.SubstitutionDiffDto> diffs = new ArrayList<>();
    double totalCalories = 0.0;
    double totalProtein = 0.0;
    double totalCarbs = 0.0;
    double totalFat = 0.0;
    double totalFiber = 0.0;
    double totalSodium = 0.0;

    int servings = Math.max(1, recipe.getBaseServings());

    for (RecipeIngredientEntity ing : recipe.getIngredients()) {
      FoodEntity origFood = ing.getFood();
      double origQty = ing.getAmountG();
      String origUnit = "g";

      String replacementId = getReplacementFoodId(origFood, cleanType, customPrompt);

      if (replacementId != null && !replacementId.equalsIgnoreCase(origFood.getId())) {
        Optional<FoodEntity> replFoodOpt = foodRepository.findById(replacementId);
        if (replFoodOpt.isEmpty()) {
          List<FoodEntity> matches = foodRepository.searchFoods(replacementId);
          if (!matches.isEmpty()) {
            replFoodOpt = Optional.of(matches.get(0));
          }
        }

        FoodEntity replFood = replFoodOpt.orElse(origFood);
        double grams = origQty;
        double cal = (replFood.getEnergyKcal() * grams) / 100.0;
        double pro = (replFood.getProteinG() * grams) / 100.0;
        double car = (replFood.getCarbohydrateG() * grams) / 100.0;
        double fat = (replFood.getFatG() * grams) / 100.0;
        double fib = (replFood.getFibreG() * grams) / 100.0;
        double sod = (replFood.getSodiumMg() * grams) / 100.0;

        totalCalories += cal;
        totalProtein += pro;
        totalCarbs += car;
        totalFat += fat;
        totalFiber += fib;
        totalSodium += sod;

        diffs.add(
            new RecipeAdaptationDto.SubstitutionDiffDto(
                origFood.getId(),
                origFood.getNameEn(),
                origQty,
                origUnit,
                replFood.getId(),
                replFood.getNameEn(),
                origQty,
                origUnit,
                getReason(cleanType, origFood.getNameEn(), replFood.getNameEn())));
      } else {
        double grams = origQty;
        totalCalories += (origFood.getEnergyKcal() * grams) / 100.0;
        totalProtein += (origFood.getProteinG() * grams) / 100.0;
        totalCarbs += (origFood.getCarbohydrateG() * grams) / 100.0;
        totalFat += (origFood.getFatG() * grams) / 100.0;
        totalFiber += (origFood.getFibreG() * grams) / 100.0;
        totalSodium += (origFood.getSodiumMg() * grams) / 100.0;

        diffs.add(
            new RecipeAdaptationDto.SubstitutionDiffDto(
                origFood.getId(),
                origFood.getNameEn(),
                origQty,
                origUnit,
                origFood.getId(),
                origFood.getNameEn(),
                origQty,
                origUnit,
                "Retained original"));
      }
    }

    double adaptedCalPerServing = round(totalCalories / servings);
    double adaptedProPerServing = round(totalProtein / servings);
    double adaptedCarbPerServing = round(totalCarbs / servings);
    double adaptedFatPerServing = round(totalFat / servings);
    double adaptedFibPerServing = round(totalFiber / servings);
    double adaptedSodPerServing = round(totalSodium / servings);

    // Safety checks
    String safetyVerdict = "ALLOW";
    List<String> warnings = new ArrayList<>();
    if (profile != null) {
      if (profile.getAllergies() != null) {
        for (String allergy : profile.getAllergies()) {
          for (RecipeAdaptationDto.SubstitutionDiffDto diff : diffs) {
            if (diff.adaptedFoodName().toLowerCase().contains(allergy.toLowerCase())
                || diff.adaptedFoodId().toLowerCase().contains(allergy.toLowerCase())) {
              safetyVerdict = "BLOCK";
              warnings.add("Contains allergen for user: " + allergy);
            }
          }
        }
      }
      if (profile.getHealthConditions() != null) {
        if (profile.getHealthConditions().contains("HYPERTENSION")
            && adaptedSodPerServing > 600.0) {
          if (!"BLOCK".equals(safetyVerdict)) safetyVerdict = "WARN";
          warnings.add(
              "High sodium content for hypertension management (" + adaptedSodPerServing + " mg)");
        }
      }
    }

    String title = recipe.getTitleEn() + " (" + cleanType + " Variant)";

    List<String> instructions =
        recipe.getInstructionsText() != null
            ? Arrays.asList(recipe.getInstructionsText().split("\n"))
            : List.of("Follow preparation steps with substitute ingredients.");

    return new RecipeAdaptationDto(
        savedId,
        recipe.getId(),
        recipe.getTitleEn(),
        title,
        cleanType,
        servings,
        original.perServingCalories(),
        original.perServingProteinG(),
        original.perServingCarbG(),
        original.perServingFatG(),
        original.totalFiberG() / servings,
        0.0,
        adaptedCalPerServing,
        adaptedProPerServing,
        adaptedCarbPerServing,
        adaptedFatPerServing,
        adaptedFibPerServing,
        adaptedSodPerServing,
        safetyVerdict,
        warnings,
        diffs,
        instructions,
        savedId != null);
  }

  private String getReplacementFoodId(FoodEntity food, String type, String customPrompt) {
    String id = food.getId().toLowerCase(Locale.ROOT);
    String name = food.getNameEn().toLowerCase(Locale.ROOT);

    return switch (type) {
      case "VEGAN" -> {
        if (id.contains("paneer") || name.contains("paneer")) yield "tofu";
        if (id.contains("milk") || name.contains("milk")) yield "soy_milk";
        if (id.contains("curd")
            || id.contains("yogurt")
            || name.contains("curd")
            || name.contains("yogurt")) yield "soy_curd";
        if (id.contains("ghee")
            || id.contains("butter")
            || name.contains("ghee")
            || name.contains("butter")) yield "mustard_oil";
        yield null;
      }
      case "JAIN" -> {
        if (id.contains("onion") || name.contains("onion")) yield "tomato";
        if (id.contains("garlic") || name.contains("garlic")) yield "ginger";
        if (id.contains("potato") || name.contains("potato")) yield "bottle_gourd";
        if (id.contains("carrot") || name.contains("carrot")) yield "bottle_gourd";
        yield null;
      }
      case "DAIRY_FREE" -> {
        if (id.contains("paneer") || name.contains("paneer")) yield "tofu";
        if (id.contains("milk") || name.contains("milk")) yield "soy_milk";
        if (id.contains("curd")
            || id.contains("yogurt")
            || name.contains("curd")
            || name.contains("yogurt")) yield "soy_curd";
        if (id.contains("ghee")
            || id.contains("butter")
            || name.contains("ghee")
            || name.contains("butter")) yield "vegetable_oil";
        yield null;
      }
      case "LOW_SODIUM" -> {
        if (id.contains("salt") || name.contains("salt")) yield "lemon_juice";
        yield null;
      }
      default -> {
        if (customPrompt != null && !customPrompt.isBlank()) {
          String lower = customPrompt.toLowerCase(Locale.ROOT);
          if (lower.contains("swap " + id) || lower.contains("substitute " + id)) {
            yield "tofu";
          }
        }
        yield null;
      }
    };
  }

  private String getReason(String type, String orig, String repl) {
    return switch (type) {
      case "VEGAN" ->
          "Replaced animal/dairy " + orig + " with plant-based " + repl + " for vegan suitability.";
      case "JAIN" ->
          "Replaced root/allium "
              + orig
              + " with sattvic "
              + repl
              + " complying with Jain dietary rules.";
      case "DAIRY_FREE" -> "Substituted lactose/dairy " + orig + " with dairy-free " + repl + ".";
      case "LOW_SODIUM" ->
          "Reduced sodium by replacing " + orig + " with natural citrus/herbs (" + repl + ").";
      default -> "Substituted " + orig + " with " + repl + ".";
    };
  }

  private double round(double val) {
    return BigDecimal.valueOf(val).setScale(1, RoundingMode.HALF_UP).doubleValue();
  }
}
