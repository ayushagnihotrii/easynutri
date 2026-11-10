package com.easynutri.ai.tool;

import com.easynutri.entity.FoodEntity;
import com.easynutri.repository.FoodRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class SearchFoodsTool implements AiTool {

  private final FoodRepository foodRepository;

  public SearchFoodsTool(FoodRepository foodRepository) {
    this.foodRepository = foodRepository;
  }

  @Override
  public String getName() {
    return "search_foods";
  }

  @Override
  public String getDescription() {
    return "Searches verified Indian and international foods in the food database by name or category.";
  }

  @Override
  public String execute(Long userId, Map<String, Object> arguments) {
    String query = arguments.getOrDefault("query", "").toString();
    int limit = 5;
    if (arguments.containsKey("limit")) {
      try {
        limit = Math.min(10, Integer.parseInt(arguments.get("limit").toString()));
      } catch (NumberFormatException ignored) {
      }
    }

    List<FoodEntity> foods;
    if (query.isBlank()) {
      foods = foodRepository.findAll().stream().limit(limit).toList();
    } else {
      foods =
          foodRepository.findAll().stream()
              .filter(
                  f ->
                      (f.getNameEn() != null
                              && f.getNameEn().toLowerCase().contains(query.toLowerCase()))
                          || (f.getNameHi() != null && f.getNameHi().contains(query))
                          || (f.getCategory() != null
                              && f.getCategory().toLowerCase().contains(query.toLowerCase())))
              .limit(limit)
              .toList();
    }

    if (foods.isEmpty()) {
      return "No matching foods found in catalog for query: " + query;
    }

    return foods.stream()
        .map(
            f ->
                String.format(
                    java.util.Locale.US,
                    "• ID: %s | %s (%s) | %s | %.0f kcal, %.1fg P, %.1fg C, %.1fg F per 100g",
                    f.getId(),
                    f.getNameEn(),
                    f.getNameHi() != null ? f.getNameHi() : "",
                    f.getCategory(),
                    f.getEnergyKcal(),
                    f.getProteinG(),
                    f.getCarbohydrateG(),
                    f.getFatG()))
        .collect(Collectors.joining("\n"));
  }
}
