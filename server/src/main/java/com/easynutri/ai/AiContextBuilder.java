package com.easynutri.ai;

import com.easynutri.dto.ActivitySummaryDto;
import com.easynutri.dto.CookBeforeExpiresDto;
import com.easynutri.dto.DiaryDayDto;
import com.easynutri.dto.HydrationSummaryDto;
import com.easynutri.entity.UserProfileEntity;
import com.easynutri.repository.UserProfileRepository;
import com.easynutri.service.ActivityTrackingService;
import com.easynutri.service.DiaryService;
import com.easynutri.service.HydrationService;
import com.easynutri.service.PantryService;
import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class AiContextBuilder {

  private final UserProfileRepository profileRepository;
  private final DiaryService diaryService;
  private final HydrationService hydrationService;
  private final ActivityTrackingService activityService;
  private final PantryService pantryService;

  public AiContextBuilder(
      UserProfileRepository profileRepository,
      DiaryService diaryService,
      HydrationService hydrationService,
      ActivityTrackingService activityService,
      PantryService pantryService) {
    this.profileRepository = profileRepository;
    this.diaryService = diaryService;
    this.hydrationService = hydrationService;
    this.activityService = activityService;
    this.pantryService = pantryService;
  }

  public UserAiContext buildContext(Long userId) {
    if (userId == null) {
      return new UserAiContext(
          false,
          "Guest",
          "VEGETARIAN",
          Collections.emptyList(),
          Collections.emptyList(),
          Collections.emptyMap());
    }

    Optional<UserProfileEntity> profileOpt = profileRepository.findByUserId(userId);
    boolean sensitiveMode = profileOpt.map(UserProfileEntity::isSensitiveMode).orElse(false);
    String diet = profileOpt.map(UserProfileEntity::getDietaryPreference).orElse("VEGETARIAN");
    List<String> allergies =
        profileOpt
            .map(
                p ->
                    p.getAllergies() != null
                        ? new ArrayList<>(p.getAllergies())
                        : List.<String>of())
            .orElse(List.of());
    List<String> conditions =
        profileOpt
            .map(
                p ->
                    p.getHealthConditions() != null
                        ? new ArrayList<>(p.getHealthConditions())
                        : List.<String>of())
            .orElse(List.of());

    Map<String, Object> summary = new LinkedHashMap<>();
    LocalDate today = LocalDate.now();

    try {
      DiaryDayDto diary = diaryService.getDayDiary(userId, today);
      if (sensitiveMode) {
        summary.put(
            "diaryStatus",
            "Logged meals: "
                + diary.slots().stream().mapToInt(s -> s.items().size()).sum()
                + " items");
      } else {
        summary.put("caloriesLogged", diary.totalCalories());
        summary.put("calorieTarget", diary.targetCalories());
        summary.put("proteinG", diary.totalProteinG());
        summary.put("carbsG", diary.totalCarbsG());
        summary.put("fatG", diary.totalFatG());
      }
    } catch (Exception ignored) {
    }

    try {
      HydrationSummaryDto hydration = hydrationService.getHydrationSummary(userId, today);
      summary.put("waterLoggedMl", hydration.currentIntakeMl());
      summary.put("waterTargetMl", hydration.dailyTargetMl());
    } catch (Exception ignored) {
    }

    try {
      ActivitySummaryDto activity = activityService.getActivitySummary(userId, today);
      summary.put("stepCount", activity.steps());
      summary.put("stepTarget", activity.goalSteps());
      if (!sensitiveMode) {
        summary.put("burnedKcal", activity.totalExpenditureKcal());
      }
    } catch (Exception ignored) {
    }

    try {
      List<CookBeforeExpiresDto> expiring = pantryService.getCookBeforeItExpires(userId);
      List<String> expiringNames =
          expiring.stream().flatMap(c -> c.expiringIngredientsUsed().stream()).distinct().toList();
      summary.put("expiringPantryItems", expiringNames);
    } catch (Exception ignored) {
    }

    return new UserAiContext(
        sensitiveMode,
        profileOpt.map(p -> p.getUser().getFullName()).orElse("User"),
        diet,
        allergies,
        conditions,
        summary);
  }

  public record UserAiContext(
      boolean sensitiveMode,
      String userName,
      String diet,
      List<String> allergies,
      List<String> conditions,
      Map<String, Object> liveMetrics) {}
}
