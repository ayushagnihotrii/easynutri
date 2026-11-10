package com.easynutri.ai.tool;

import com.easynutri.dto.RecipeDetailDto;
import com.easynutri.service.RecipeService;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class FindRecipesTool implements AiTool {

  private final RecipeService recipeService;

  public FindRecipesTool(RecipeService recipeService) {
    this.recipeService = recipeService;
  }

  @Override
  public String getName() {
    return "find_recipes";
  }

  @Override
  public String getDescription() {
    return "Searches recipes by text query, meal slot (BREAKFAST, LUNCH, DINNER, SNACK), or ingredients.";
  }

  @Override
  public String execute(Long userId, Map<String, Object> arguments) {
    String query = arguments.getOrDefault("query", "").toString();
    String slot = arguments.getOrDefault("mealSlot", "").toString();

    List<RecipeDetailDto> recipes = recipeService.searchRecipes(query);
    if (!slot.isBlank()) {
      recipes =
          recipes.stream()
              .filter(r -> r.mealSlot() != null && r.mealSlot().equalsIgnoreCase(slot))
              .toList();
    }

    if (recipes.isEmpty()) {
      return "No recipes found matching query: " + query + " (Slot: " + slot + ")";
    }

    return recipes.stream()
        .limit(5)
        .map(
            (RecipeDetailDto r) ->
                String.format(
                    Locale.US,
                    "• [%s] %s | %s | %.0f kcal/serving | %.1fg P • %.1fg C • %.1fg F | Ready in %d mins",
                    r.id(),
                    r.titleEn(),
                    r.mealSlot(),
                    r.perServingCalories(),
                    r.perServingProteinG(),
                    r.perServingCarbG(),
                    r.perServingFatG(),
                    r.prepMinutes() + r.cookMinutes()))
        .collect(Collectors.joining("\n"));
  }
}
