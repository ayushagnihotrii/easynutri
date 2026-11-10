package com.easynutri.ai.tool;

import com.easynutri.dto.ActivitySummaryDto;
import com.easynutri.service.ActivityTrackingService;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class GetActivitySummaryTool implements AiTool {

  private final ActivityTrackingService activityTrackingService;

  public GetActivitySummaryTool(ActivityTrackingService activityTrackingService) {
    this.activityTrackingService = activityTrackingService;
  }

  @Override
  public String getName() {
    return "get_activity_summary";
  }

  @Override
  public String getDescription() {
    return "Fetches user's daily step count, calories burned, workout logs, and energy expenditure status.";
  }

  @Override
  public String execute(Long userId, Map<String, Object> arguments) {
    if (userId == null) {
      return "No user logged in to fetch activity summary.";
    }
    String dateStr = (String) arguments.get("date");
    LocalDate date =
        dateStr != null && !dateStr.isBlank() ? LocalDate.parse(dateStr) : LocalDate.now();

    ActivitySummaryDto summary = activityTrackingService.getActivitySummary(userId, date);

    if (summary.sensitiveMode()) {
      return String.format(
          Locale.US,
          "Activity Summary (%s):\n"
              + "• Steps: %,d / %,d goal (%d%%)\n"
              + "• Active Workouts: %d session(s)\n"
              + "• Wellbeing Status: %s",
          summary.formattedDate(),
          summary.steps(),
          summary.goalSteps(),
          summary.stepProgressPercent(),
          summary.workouts().size(),
          summary.energyBalanceStatus());
    }

    return String.format(
        Locale.US,
        "Activity & Expenditure Summary (%s):\n"
            + "• Steps: %,d / %,d goal (%d%%, %.0f kcal)\n"
            + "• Workouts: %d session(s) (%.0f kcal)\n"
            + "• Total Expenditure: %.0f kcal | Food: %.0f kcal\n"
            + "• Energy Balance: %s",
        summary.formattedDate(),
        summary.steps(),
        summary.goalSteps(),
        summary.stepProgressPercent(),
        summary.stepCalories(),
        summary.workouts().size(),
        summary.totalWorkoutCalories(),
        summary.totalExpenditureKcal(),
        summary.foodCalories(),
        summary.energyBalanceStatus());
  }
}
