package com.easynutri.ai.tool;

import com.easynutri.dto.RecipeAdaptationDto;
import com.easynutri.service.RecipeAdaptationService;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class AdaptRecipeTool implements AiTool {

  private final RecipeAdaptationService adaptationService;

  public AdaptRecipeTool(RecipeAdaptationService adaptationService) {
    this.adaptationService = adaptationService;
  }

  @Override
  public String getName() {
    return "adapt_recipe";
  }

  @Override
  public String getDescription() {
    return "Adapts a recipe to dietary constraints (VEGAN, JAIN, DAIRY_FREE, LOW_SODIUM, CUSTOM), recalculates nutrition using pure Java, and verifies safety against the user profile.";
  }

  @Override
  public String execute(Long userId, Map<String, Object> arguments) {
    String recipeId = (String) arguments.get("recipeId");
    if (recipeId == null || recipeId.isBlank()) {
      return "recipeId is required for adaptation";
    }

    String adaptationType = (String) arguments.getOrDefault("adaptationType", "CUSTOM");
    String customPrompt = (String) arguments.get("customPrompt");

    try {
      RecipeAdaptationDto dto =
          adaptationService.previewAdaptation(userId, recipeId, adaptationType, customPrompt);

      return String.format(
          Locale.US,
          "Recipe Adaptation: %s\n"
              + "• Style: %s | Safety Verdict: %s\n"
              + "• Energy: %.0f kcal (Orig: %.0f kcal)\n"
              + "• Protein: %.1fg (Orig: %.1fg)\n"
              + "• Fiber: %.1fg | Carbs: %.1fg | Fat: %.1fg\n"
              + "• Substitutions Applied: %d ingredient(s)",
          dto.adaptationTitle(),
          dto.adaptationType(),
          dto.safetyVerdict(),
          dto.adaptedCalories(),
          dto.originalCalories(),
          dto.adaptedProtein(),
          dto.originalProtein(),
          dto.adaptedFiber(),
          dto.adaptedCarbs(),
          dto.adaptedFat(),
          dto.ingredientDiffs().stream()
              .filter(d -> !d.originalFoodId().equalsIgnoreCase(d.adaptedFoodId()))
              .count());
    } catch (Exception e) {
      return "Adaptation failed: " + e.getMessage();
    }
  }
}
