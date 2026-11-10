package com.easynutri.ai.tool;

import com.easynutri.domain.model.FoodCandidate;
import com.easynutri.domain.model.ProfileContext;
import com.easynutri.domain.nutrition.DefaultSafetyGate;
import com.easynutri.domain.nutrition.SafetyGate;
import com.easynutri.domain.rules.SafetyResult;
import com.easynutri.entity.FoodEntity;
import com.easynutri.entity.UserProfileEntity;
import com.easynutri.repository.FoodRepository;
import com.easynutri.repository.RecipeRepository;
import com.easynutri.repository.UserProfileRepository;
import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class CheckFoodSafetyTool implements AiTool {

  private final FoodRepository foodRepository;
  private final RecipeRepository recipeRepository;
  private final UserProfileRepository userProfileRepository;
  private final SafetyGate safetyGate = new DefaultSafetyGate();

  public CheckFoodSafetyTool(
      FoodRepository foodRepository,
      RecipeRepository recipeRepository,
      UserProfileRepository userProfileRepository) {
    this.foodRepository = foodRepository;
    this.recipeRepository = recipeRepository;
    this.userProfileRepository = userProfileRepository;
  }

  @Override
  public String getName() {
    return "check_food_safety";
  }

  @Override
  public String getDescription() {
    return "Evaluates safety of a food or recipe against the user's specific health conditions and allergies using pure Java safety rules.";
  }

  @Override
  public String execute(Long userId, Map<String, Object> arguments) {
    String foodId = (String) arguments.get("foodId");
    String foodName = (String) arguments.get("foodName");

    Optional<UserProfileEntity> profileOpt =
        userId != null ? userProfileRepository.findByUserId(userId) : Optional.empty();

    ProfileContext profileContext =
        profileOpt
            .map(
                p ->
                    new ProfileContext(
                        String.valueOf(p.getUser().getId()),
                        p.getAge(),
                        p.getBiologicalSex(),
                        p.getHeightCm(),
                        p.getWeightKg(),
                        p.getDietaryPreference() != null ? p.getDietaryPreference() : "VEGETARIAN",
                        p.getAllergies() != null ? new ArrayList<>(p.getAllergies()) : List.of(),
                        p.getHealthConditions() != null
                            ? new ArrayList<>(p.getHealthConditions())
                            : List.of()))
            .orElseGet(
                () ->
                    new ProfileContext(
                        "guest", 30, "MALE", 170.0, 70.0, "VEGETARIAN", List.of(), List.of()));

    Optional<FoodEntity> foodOpt = Optional.empty();
    if (foodId != null && !foodId.isBlank()) {
      foodOpt = foodRepository.findById(foodId);
    } else if (foodName != null && !foodName.isBlank()) {
      List<FoodEntity> foods = foodRepository.searchFoods(foodName.trim());
      if (!foods.isEmpty()) {
        foodOpt = Optional.of(foods.get(0));
      }
    }

    if (foodOpt.isEmpty()) {
      return "Food item not found for identifier: " + (foodId != null ? foodId : foodName);
    }

    FoodEntity food = foodOpt.get();
    List<String> allergens =
        food.getAllergenTags() != null
            ? Arrays.stream(food.getAllergenTags().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList()
            : List.of();

    List<String> diets =
        food.getDietFlags() != null
            ? Arrays.stream(food.getDietFlags().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList()
            : List.of();

    Map<String, Double> nutrients = new LinkedHashMap<>();
    nutrients.put("calories", food.getEnergyKcal());
    nutrients.put("protein", food.getProteinG());
    nutrients.put("carbs", food.getCarbohydrateG());
    nutrients.put("fat", food.getFatG());
    nutrients.put("sodium", food.getSodiumMg());
    nutrients.put("potassium", food.getPotassiumMg());

    FoodCandidate candidate =
        new FoodCandidate(
            food.getId(), food.getNameEn(), food.getCategory(), nutrients, allergens, diets);

    SafetyResult result = safetyGate.verifyFood(profileContext, candidate);

    StringBuilder sb = new StringBuilder();
    sb.append(String.format("Safety Evaluation for %s:\n", food.getNameEn()));
    sb.append(String.format("• Verdict: %s\n", result.verdict().name()));
    if (!result.reasons().isEmpty()) {
      sb.append(String.format("• Reasons: %s\n", String.join("; ", result.reasons())));
    }
    if (!result.suggestedSwaps().isEmpty()) {
      sb.append(
          String.format("• Suggested Swaps: %s\n", String.join("; ", result.suggestedSwaps())));
    }
    return sb.toString();
  }
}
