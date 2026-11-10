package com.easynutri.ai.tool;

import com.easynutri.dto.IngredientMatchRequest;
import com.easynutri.dto.RecipeMatchResponseDto;
import com.easynutri.service.IngredientService;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class MatchUserIngredientsTool implements AiTool {

  private final IngredientService ingredientService;

  public MatchUserIngredientsTool(IngredientService ingredientService) {
    this.ingredientService = ingredientService;
  }

  @Override
  public String getName() {
    return "match_user_ingredients";
  }

  @Override
  public String getDescription() {
    return "Matches a list of ingredient names against the verified recipe database to find meals the user can cook right now.";
  }

  @Override
  @SuppressWarnings("unchecked")
  public String execute(Long userId, Map<String, Object> arguments) {
    List<String> ingredients = new ArrayList<>();
    if (arguments.containsKey("ingredients")) {
      Object raw = arguments.get("ingredients");
      if (raw instanceof List<?>) {
        for (Object item : (List<?>) raw) {
          if (item != null) ingredients.add(item.toString());
        }
      } else if (raw != null) {
        for (String s : raw.toString().split(",")) {
          if (!s.isBlank()) ingredients.add(s.trim());
        }
      }
    }

    if (ingredients.isEmpty()) {
      return "No ingredients provided for matching.";
    }

    IngredientMatchRequest req =
        new IngredientMatchRequest(ingredients, false, List.of(), 0, "FRESH");
    List<RecipeMatchResponseDto> matches = ingredientService.findMatchingRecipes(userId, req);

    if (matches.isEmpty()) {
      return "No safe recipe matches found for ingredients: " + String.join(", ", ingredients);
    }

    return matches.stream()
        .limit(5)
        .map(
            (RecipeMatchResponseDto m) ->
                String.format(
                    Locale.US,
                    "• [%s] %s | Match: %s (%.0f%%) | Missing: %s",
                    m.recipeId(),
                    m.titleEn(),
                    m.matchLabel(),
                    m.matchScore() * 100,
                    m.missingIngredients().isEmpty()
                        ? "None (Ready Now!)"
                        : String.join(", ", m.missingIngredients())))
        .collect(Collectors.joining("\n"));
  }
}
