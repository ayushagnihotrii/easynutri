package com.easynutri.ai.tool;

import com.easynutri.entity.FoodEntity;
import com.easynutri.repository.FoodRepository;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class GetNutritionFactsTool implements AiTool {

  private final FoodRepository foodRepository;

  public GetNutritionFactsTool(FoodRepository foodRepository) {
    this.foodRepository = foodRepository;
  }

  @Override
  public String getName() {
    return "get_nutrition_facts";
  }

  @Override
  public String getDescription() {
    return "Retrieves canonical verified nutrition values for a food ID scaled to specified gram amount.";
  }

  @Override
  public String execute(Long userId, Map<String, Object> arguments) {
    String foodId = arguments.getOrDefault("foodId", "").toString();
    double amountG = 100.0;
    if (arguments.containsKey("amountG")) {
      try {
        amountG = Double.parseDouble(arguments.get("amountG").toString());
      } catch (NumberFormatException ignored) {
      }
    }

    if (foodId.isBlank()) {
      return "Error: foodId parameter is required.";
    }

    FoodEntity food = foodRepository.findById(foodId).orElse(null);

    if (food == null) {
      return "Food not found for ID: " + foodId;
    }

    double scale = amountG / 100.0;
    return String.format(
        Locale.US,
        "Nutritional Facts for %s (%.0fg):\n"
            + "• Energy: %.0f kcal\n"
            + "• Protein: %.1fg\n"
            + "• Carbohydrates: %.1fg (Sugar: %.1fg)\n"
            + "• Fat: %.1fg (Saturated: %.1fg)\n"
            + "• Fibre: %.1fg\n"
            + "• Sodium: %.1f mg\n"
            + "• Potassium: %.1f mg | Calcium: %.1f mg | Iron: %.1f mg\n"
            + "• Allergens: %s\n"
            + "• Diet Flags: %s",
        food.getNameEn(),
        amountG,
        food.getEnergyKcal() * scale,
        food.getProteinG() * scale,
        food.getCarbohydrateG() * scale,
        food.getSugarG() * scale,
        food.getFatG() * scale,
        food.getSaturatedFatG() * scale,
        food.getFibreG() * scale,
        food.getSodiumMg() * scale,
        food.getPotassiumMg() * scale,
        food.getCalciumMg() * scale,
        food.getIronMg() * scale,
        food.getAllergenTags() != null ? food.getAllergenTags() : "None",
        food.getDietFlags() != null ? food.getDietFlags() : "Standard");
  }
}
