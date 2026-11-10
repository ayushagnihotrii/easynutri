package com.easynutri.ai.tool;

import com.easynutri.dto.PantryItemDto;
import com.easynutri.service.PantryService;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class GetPantryInventoryTool implements AiTool {

  private final PantryService pantryService;

  public GetPantryInventoryTool(PantryService pantryService) {
    this.pantryService = pantryService;
  }

  @Override
  public String getName() {
    return "get_pantry_inventory";
  }

  @Override
  public String getDescription() {
    return "Retrieves the user's current pantry items, quantities, and freshness status.";
  }

  @Override
  public String execute(Long userId, Map<String, Object> arguments) {
    List<PantryItemDto> items = pantryService.getUserPantry(userId);
    if (items.isEmpty()) {
      return "Pantry is currently empty.";
    }

    return items.stream()
        .map(
            (PantryItemDto p) ->
                String.format(
                    Locale.US,
                    "• %s: %.0f %s (%s, %s)",
                    p.foodName(),
                    p.quantityG(),
                    p.unit(),
                    p.storageLocation(),
                    p.daysRemaining() < 0 ? "EXPIRED" : p.daysRemaining() + " days remaining"))
        .collect(Collectors.joining("\n"));
  }
}
