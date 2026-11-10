package com.easynutri.ai.tool;

import com.easynutri.dto.CookBeforeExpiresDto;
import com.easynutri.service.PantryService;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class GetExpiringItemsTool implements AiTool {

  private final PantryService pantryService;

  public GetExpiringItemsTool(PantryService pantryService) {
    this.pantryService = pantryService;
  }

  @Override
  public String getName() {
    return "get_expiring_items";
  }

  @Override
  public String getDescription() {
    return "Retrieves pantry ingredients nearing expiration and recommended recipes to prevent food waste.";
  }

  @Override
  public String execute(Long userId, Map<String, Object> arguments) {
    List<CookBeforeExpiresDto> expiring = pantryService.getCookBeforeItExpires(userId);
    if (expiring.isEmpty()) {
      return "No pantry items are currently approaching expiration.";
    }

    return expiring.stream()
        .map(
            (CookBeforeExpiresDto e) ->
                String.format(
                    Locale.US,
                    "• Recipe: %s (%s, %d mins) | Expiring Used: %s | Match: %s",
                    e.recipeTitle(),
                    e.mealSlot(),
                    e.cookMinutes(),
                    String.join(", ", e.expiringIngredientsUsed()),
                    e.matchLabel()))
        .collect(Collectors.joining("\n"));
  }
}
