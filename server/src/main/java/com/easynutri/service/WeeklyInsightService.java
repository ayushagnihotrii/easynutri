package com.easynutri.service;

import com.easynutri.dto.ActivitySummaryDto;
import com.easynutri.dto.CookBeforeExpiresDto;
import com.easynutri.dto.DiaryDayDto;
import com.easynutri.dto.HydrationSummaryDto;
import com.easynutri.entity.UserEntity;
import com.easynutri.entity.UserProfileEntity;
import com.easynutri.entity.WeeklyInsightEntity;
import com.easynutri.repository.UserProfileRepository;
import com.easynutri.repository.UserRepository;
import com.easynutri.repository.WeeklyInsightRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WeeklyInsightService {

  private final WeeklyInsightRepository insightRepository;
  private final UserRepository userRepository;
  private final UserProfileRepository profileRepository;
  private final DiaryService diaryService;
  private final HydrationService hydrationService;
  private final ActivityTrackingService activityService;
  private final PantryService pantryService;

  public WeeklyInsightService(
      WeeklyInsightRepository insightRepository,
      UserRepository userRepository,
      UserProfileRepository profileRepository,
      DiaryService diaryService,
      HydrationService hydrationService,
      ActivityTrackingService activityService,
      PantryService pantryService) {
    this.insightRepository = insightRepository;
    this.userRepository = userRepository;
    this.profileRepository = profileRepository;
    this.diaryService = diaryService;
    this.hydrationService = hydrationService;
    this.activityService = activityService;
    this.pantryService = pantryService;
  }

  public List<WeeklyInsightEntity> getOrGenerateInsights(Long userId, LocalDate date) {
    LocalDate targetDate = date != null ? date : LocalDate.now();
    LocalDate weekStart = targetDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

    List<WeeklyInsightEntity> existing =
        insightRepository.findByUserIdAndWeekStartDateOrderByCreatedAtDesc(userId, weekStart);
    if (!existing.isEmpty()) {
      return existing;
    }

    return generateInsightsForWeek(userId, weekStart);
  }

  public List<WeeklyInsightEntity> generateInsightsForWeek(Long userId, LocalDate weekStart) {
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    Optional<UserProfileEntity> profileOpt = profileRepository.findByUserId(userId);
    boolean sensitiveMode = profileOpt.map(UserProfileEntity::isSensitiveMode).orElse(false);

    List<WeeklyInsightEntity> generated = new ArrayList<>();

    // 1. Evaluate 7-day diary
    double totalProtein = 0.0;
    int daysWithDiary = 0;

    for (int i = 0; i < 7; i++) {
      LocalDate d = weekStart.plusDays(i);
      try {
        DiaryDayDto dayDto = diaryService.getDayDiary(userId, d);
        if (dayDto != null && !dayDto.slots().stream().allMatch(s -> s.items().isEmpty())) {
          daysWithDiary++;
          totalProtein += dayDto.totalProteinG();
        }
      } catch (Exception ignored) {
      }
    }

    if (daysWithDiary > 0) {
      double avgProtein = totalProtein / daysWithDiary;
      if (avgProtein >= 50.0) {
        generated.add(
            new WeeklyInsightEntity(
                user,
                weekStart,
                "PROTEIN",
                sensitiveMode
                    ? "Consistent protein intake noticed across your meals, supporting muscle recovery and satiety."
                    : String.format(
                        "Excellent protein consistency! You averaged %.1fg of protein across %d logged days.",
                        avgProtein, daysWithDiary),
                "RECIPE_SEARCH",
                "/recipes",
                true));
      } else {
        generated.add(
            new WeeklyInsightEntity(
                user,
                weekStart,
                "PROTEIN",
                sensitiveMode
                    ? "Incorporate more nourishing legumes, tofu, or dairy into your meals for sustained strength."
                    : String.format(
                        "Your daily protein averaged %.1fg. Adding lentils, edamame, or paneer can elevate your intake.",
                        avgProtein),
                "RECIPE_SEARCH",
                "/recipes?search=dal",
                false));
      }
    }

    // 2. Hydration evaluation
    int totalWater = 0;
    int hydrationDays = 0;
    for (int i = 0; i < 7; i++) {
      LocalDate d = weekStart.plusDays(i);
      try {
        HydrationSummaryDto hyd = hydrationService.getHydrationSummary(userId, d);
        if (hyd != null && hyd.currentIntakeMl() > 0) {
          hydrationDays++;
          totalWater += hyd.currentIntakeMl();
        }
      } catch (Exception ignored) {
      }
    }

    if (hydrationDays > 0) {
      double avgWater = (double) totalWater / hydrationDays;
      if (avgWater >= 2000.0) {
        generated.add(
            new WeeklyInsightEntity(
                user,
                weekStart,
                "HYDRATION",
                String.format(
                    "Great hydration habits! You averaged %d ml per day across %d active days.",
                    (int) avgWater, hydrationDays),
                "ASSISTANT_CHAT",
                "/hydration",
                true));
      } else {
        generated.add(
            new WeeklyInsightEntity(
                user,
                weekStart,
                "HYDRATION",
                "Try keeping a water bottle nearby during work hours to steadily reach your daily fluid intake.",
                "ASSISTANT_CHAT",
                "/hydration",
                false));
      }
    }

    // 3. Activity / Movement
    int totalSteps = 0;
    int stepDays = 0;
    for (int i = 0; i < 7; i++) {
      LocalDate d = weekStart.plusDays(i);
      try {
        ActivitySummaryDto act = activityService.getActivitySummary(userId, d);
        if (act != null && act.steps() > 0) {
          stepDays++;
          totalSteps += act.steps();
        }
      } catch (Exception ignored) {
      }
    }

    if (stepDays > 0) {
      int avgSteps = totalSteps / stepDays;
      if (avgSteps >= 7500) {
        generated.add(
            new WeeklyInsightEntity(
                user,
                weekStart,
                "ACTIVITY",
                String.format(
                    "High vitality week! You achieved an average of %,d steps across %d recorded days.",
                    avgSteps, stepDays),
                "ASSISTANT_CHAT",
                "/activity",
                true));
      } else {
        generated.add(
            new WeeklyInsightEntity(
                user,
                weekStart,
                "ACTIVITY",
                "A gentle 15-minute stroll after lunch or dinner supports metabolic balance and mental clarity.",
                "ASSISTANT_CHAT",
                "/activity",
                true));
      }
    }

    // 4. Pantry & Food Waste
    try {
      List<CookBeforeExpiresDto> expiring = pantryService.getCookBeforeItExpires(userId);
      if (!expiring.isEmpty()) {
        String names =
            expiring.stream()
                .limit(3)
                .flatMap(c -> c.expiringIngredientsUsed().stream())
                .distinct()
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        generated.add(
            new WeeklyInsightEntity(
                user,
                weekStart,
                "PANTRY",
                "You have ingredients expiring soon: "
                    + names
                    + ". Transform them into delicious meals before they spoil!",
                "RECIPE_SEARCH",
                "/ingredients",
                false));
      }
    } catch (Exception ignored) {
    }

    // 5. Default encouraging insight if log volume is modest
    if (generated.isEmpty()) {
      generated.add(
          new WeeklyInsightEntity(
              user,
              weekStart,
              "CONSISTENCY",
              "Welcome to your weekly health check-in! Consistent logging in your Food Diary, Hydration, and Steps helps build long-term wellness patterns.",
              "RECIPE_SEARCH",
              "/diary",
              true));
    }

    return insightRepository.saveAll(generated);
  }
}
