package com.easynutri.ai.tool;

import com.easynutri.dto.HydrationSummaryDto;
import com.easynutri.service.HydrationService;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class GetHydrationSummaryTool implements AiTool {

  private final HydrationService hydrationService;

  public GetHydrationSummaryTool(HydrationService hydrationService) {
    this.hydrationService = hydrationService;
  }

  @Override
  public String getName() {
    return "get_hydration_summary";
  }

  @Override
  public String getDescription() {
    return "Fetches the user's hydration log, current fluid intake, target amount, and progress for today or a specific date.";
  }

  @Override
  public String execute(Long userId, Map<String, Object> arguments) {
    if (userId == null) {
      return "No user logged in to fetch hydration summary.";
    }
    String dateStr = (String) arguments.get("date");
    LocalDate date =
        dateStr != null && !dateStr.isBlank() ? LocalDate.parse(dateStr) : LocalDate.now();

    HydrationSummaryDto summary = hydrationService.getHydrationSummary(userId, date);

    return String.format(
        Locale.US,
        "Hydration Status (%s):\n"
            + "• Intake: %d ml / %d ml target (%d%%)\n"
            + "• Clinically Restricted: %s\n"
            + "• Recommendation: %s",
        summary.formattedDate(),
        summary.currentIntakeMl(),
        summary.dailyTargetMl(),
        summary.progressPercent(),
        summary.isClinicallyRestricted() ? "Yes (" + summary.clinicalNotice() + ")" : "No",
        summary.recommendation() != null
            ? summary.recommendation()
            : "Maintain regular water intake.");
  }
}
