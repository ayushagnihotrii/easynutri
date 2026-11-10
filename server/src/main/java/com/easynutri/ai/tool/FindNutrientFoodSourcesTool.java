package com.easynutri.ai.tool;

import com.easynutri.entity.FoodEntity;
import com.easynutri.entity.UserProfileEntity;
import com.easynutri.repository.FoodRepository;
import com.easynutri.repository.UserProfileRepository;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class FindNutrientFoodSourcesTool implements AiTool {

  private final FoodRepository foodRepository;
  private final UserProfileRepository userProfileRepository;

  public FindNutrientFoodSourcesTool(
      FoodRepository foodRepository, UserProfileRepository userProfileRepository) {
    this.foodRepository = foodRepository;
    this.userProfileRepository = userProfileRepository;
  }

  @Override
  public String getName() {
    return "find_nutrient_food_sources";
  }

  @Override
  public String getDescription() {
    return "Find safe whole foods rich in a specific target nutrient (e.g., protein, iron, calcium, fiber, potassium) respecting user diet and allergies.";
  }

  @Override
  public String execute(Long userId, Map<String, Object> arguments) {
    String nutrient = (String) arguments.getOrDefault("nutrient", "protein");
    int limit = arguments.containsKey("limit") ? ((Number) arguments.get("limit")).intValue() : 5;
    if (limit <= 0) limit = 5;

    Optional<UserProfileEntity> profileOpt =
        userId != null ? userProfileRepository.findByUserId(userId) : Optional.empty();

    List<FoodEntity> allFoods = foodRepository.findAll();
    String normalizedNutrient = nutrient.toLowerCase(Locale.ROOT);

    Comparator<FoodEntity> comparator =
        switch (normalizedNutrient) {
          case "protein" -> Comparator.comparingDouble(FoodEntity::getProteinG).reversed();
          case "fiber", "fibre" -> Comparator.comparingDouble(FoodEntity::getFibreG).reversed();
          case "iron" -> Comparator.comparingDouble(FoodEntity::getIronMg).reversed();
          case "calcium" -> Comparator.comparingDouble(FoodEntity::getCalciumMg).reversed();
          case "potassium" -> Comparator.comparingDouble(FoodEntity::getPotassiumMg).reversed();
          default -> Comparator.comparingDouble(FoodEntity::getProteinG).reversed();
        };

    List<FoodEntity> filtered =
        allFoods.stream()
            .filter(
                food -> {
                  if (profileOpt.isPresent()) {
                    UserProfileEntity p = profileOpt.get();
                    if (p.getAllergies() != null) {
                      for (String allergy : p.getAllergies()) {
                        if (food.getAllergenTags() != null
                            && food.getAllergenTags()
                                .toLowerCase()
                                .contains(allergy.toLowerCase())) {
                          return false;
                        }
                      }
                    }
                  }
                  return true;
                })
            .sorted(comparator)
            .limit(limit)
            .toList();

    if (filtered.isEmpty()) {
      return "No matching whole food sources found for nutrient: " + nutrient;
    }

    return filtered.stream()
        .map(
            f ->
                String.format(
                    Locale.US,
                    "• %s (%s): %.1f kcal, %.1fg protein, %.1fg fiber, %.1fmg iron, %.1fmg calcium per 100g",
                    f.getNameEn(),
                    f.getCategory(),
                    f.getEnergyKcal(),
                    f.getProteinG(),
                    f.getFibreG(),
                    f.getIronMg(),
                    f.getCalciumMg()))
        .collect(Collectors.joining("\n"));
  }
}
