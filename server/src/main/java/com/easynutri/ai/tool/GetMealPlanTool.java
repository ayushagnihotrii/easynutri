package com.easynutri.ai.tool;

import com.easynutri.dto.MealPlanItemDto;
import com.easynutri.service.MealPlanService;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class GetMealPlanTool implements AiTool {

  private final MealPlanService mealPlanService;

  public GetMealPlanTool(MealPlanService mealPlanService) {
    this.mealPlanService = mealPlanService;
  }

  @Override
  public String getName() {
    return "get_meal_plan";
  }

  @Override
  public String getDescription() {
    return "Retrieves the user's scheduled meal plan for a given date (defaults to today).";
  }

  @Override
  public String execute(Long userId, Map<String, Object> arguments) {
    LocalDate targetDate = LocalDate.now();
    if (arguments.containsKey("date")) {
      try {
        targetDate = LocalDate.parse(arguments.get("date").toString());
      } catch (Exception ignored) {
      }
    }

    List<MealPlanItemDto> items = mealPlanService.getMealPlanForDate(userId, targetDate);
    if (items.isEmpty()) {
      return "No meals scheduled on your plan for " + targetDate + ".";
    }

    return "Scheduled Plan for "
        + targetDate
        + ":\n"
        + items.stream()
            .map(
                m ->
                    String.format(
                        Locale.US,
                        "• [%s] %s (%d serv) | %.0f kcal, %.1fg P • %s",
                        m.mealSlot(),
                        m.recipeTitle(),
                        m.servings(),
                        m.calories(),
                        m.proteinG(),
                        m.completed() ? "✓ Completed" : "Pending"))
            .collect(Collectors.joining("\n"));
  }
}
