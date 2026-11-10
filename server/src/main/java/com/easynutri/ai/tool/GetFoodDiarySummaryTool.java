package com.easynutri.ai.tool;

import com.easynutri.dto.DiaryDayDto;
import com.easynutri.service.DiaryService;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class GetFoodDiarySummaryTool implements AiTool {

  private final DiaryService diaryService;

  public GetFoodDiarySummaryTool(DiaryService diaryService) {
    this.diaryService = diaryService;
  }

  @Override
  public String getName() {
    return "get_food_diary_summary";
  }

  @Override
  public String getDescription() {
    return "Retrieves the user's daily food diary totals, slot breakdown, and target progress.";
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

    DiaryDayDto day = diaryService.getDayDiary(userId, targetDate);

    if (day.sensitiveMode()) {
      return String.format(
          Locale.US,
          "Daily Food Diary (%s):\n"
              + "• Sensitive Mode: Active (Numerical calories and grams masked for wellbeing)\n"
              + "• Status: Balanced nutritional intake logged across %d meal categories.",
          day.formattedDate(),
          day.slots().stream().filter(s -> !s.items().isEmpty()).count());
    }

    StringBuilder sb = new StringBuilder();
    sb.append(
        String.format(
            Locale.US,
            "Daily Food Diary Summary for %s:\n"
                + "• Energy Intake: %.0f kcal / %d kcal target (%d%%)\n"
                + "• Protein: %.1fg / %dg target (%d%%)\n"
                + "• Carbohydrates: %.1fg / %dg target\n"
                + "• Fat: %.1fg / %dg target\n"
                + "• Dietary Fiber: %.1fg | Sodium: %.1f mg\n\n"
                + "Meals Logged:\n",
            day.formattedDate(),
            day.totalCalories(),
            day.targetCalories(),
            day.calorieProgressPercent(),
            day.totalProteinG(),
            day.targetProteinG(),
            day.proteinProgressPercent(),
            day.totalCarbsG(),
            day.targetCarbG(),
            day.totalFatG(),
            day.targetFatG(),
            day.totalFiberG(),
            day.totalSodiumMg()));

    for (var slot : day.slots()) {
      if (!slot.items().isEmpty()) {
        sb.append(
            String.format(
                Locale.US,
                "• %s: %.0f kcal (%d item(s))\n",
                slot.displayName(),
                slot.totalCalories(),
                slot.items().size()));
      }
    }

    return sb.toString();
  }
}
