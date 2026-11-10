package com.easynutri.controller.api;

import com.easynutri.dto.*;
import com.easynutri.entity.UserEntity;
import com.easynutri.entity.UserProfileEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(
    name = "Mobile Dashboard",
    description = "Unified endpoint delivering daily health and nutrition status")
public class DashboardApiController {

  private final UserProfileService profileService;
  private final DiaryService diaryService;
  private final HydrationService hydrationService;
  private final ActivityTrackingService activityTrackingService;
  private final GamificationService gamificationService;
  private final MealPlanService mealPlanService;
  private final PantryService pantryService;
  private final RecipeService recipeService;
  private final UserRepository userRepository;

  public DashboardApiController(
      UserProfileService profileService,
      DiaryService diaryService,
      HydrationService hydrationService,
      ActivityTrackingService activityTrackingService,
      GamificationService gamificationService,
      MealPlanService mealPlanService,
      PantryService pantryService,
      RecipeService recipeService,
      UserRepository userRepository) {
    this.profileService = profileService;
    this.diaryService = diaryService;
    this.hydrationService = hydrationService;
    this.activityTrackingService = activityTrackingService;
    this.gamificationService = gamificationService;
    this.mealPlanService = mealPlanService;
    this.pantryService = pantryService;
    this.recipeService = recipeService;
    this.userRepository = userRepository;
  }

  @GetMapping
  @Operation(summary = "Get unified daily summary for home dashboard")
  public ResponseEntity<DashboardDto> getDashboard(Principal principal) {
    String username = principal != null ? principal.getName() : "demo";
    Long userId = userRepository.findByUsername(username).map(UserEntity::getId).orElse(1L);

    LocalDate today = LocalDate.now();

    Optional<UserProfileEntity> profileOpt = profileService.findByUsername(username);
    UserProfileEntity p = profileOpt.orElse(null);

    UserProfileDto profileDto =
        p != null
            ? new UserProfileDto(
                p.getId(),
                username,
                p.getAge(),
                p.getBiologicalSex(),
                p.getHeightCm(),
                p.getWeightKg(),
                p.getActivityLevel(),
                p.getHealthGoal(),
                p.getDietaryPreference(),
                p.isPregnant(),
                p.isSensitiveMode(),
                p.isOnboardingCompleted(),
                p.getTargetCalories(),
                p.getTargetProteinG(),
                p.getTargetCarbG(),
                p.getTargetFatG(),
                p.getTargetWaterL(),
                p.getAllergies(),
                p.getHealthConditions())
            : null;

    int targetCal = p != null && p.getTargetCalories() != null ? p.getTargetCalories() : 2000;
    int targetProt = p != null && p.getTargetProteinG() != null ? p.getTargetProteinG() : 120;
    int targetCarb = p != null && p.getTargetCarbG() != null ? p.getTargetCarbG() : 220;
    int targetFat = p != null && p.getTargetFatG() != null ? p.getTargetFatG() : 65;
    double targetWater = p != null && p.getTargetWaterL() != null ? p.getTargetWaterL() : 3.0;

    DiaryDayDto diary = diaryService.getDayDiary(userId, today);
    int consumedCal = diary != null ? (int) diary.totalCalories() : 0;
    int consumedProt = diary != null ? (int) diary.totalProteinG() : 0;
    int consumedCarb = diary != null ? (int) diary.totalCarbsG() : 0;
    int consumedFat = diary != null ? (int) diary.totalFatG() : 0;

    HydrationSummaryDto hydration = hydrationService.getHydrationSummary(userId, today);
    double consumedWaterL = hydration != null ? (double) hydration.currentIntakeMl() / 1000.0 : 0.0;

    ActivitySummaryDto activity = activityTrackingService.getActivitySummary(userId, today);
    int burnedCal = activity != null ? (int) activity.totalExpenditureKcal() : 0;
    int currentSteps = activity != null ? activity.steps() : 0;
    int goalSteps = activity != null ? activity.goalSteps() : 10000;

    int netCal = consumedCal - burnedCal;

    int streakDays =
        gamificationService.getUserStreaks(userId).stream()
            .mapToInt(UserStreakDto::currentStreakDays)
            .max()
            .orElse(0);

    int expiringCount = pantryService.getCookBeforeItExpires(userId).size();

    List<MealPlanItemDto> todayMeals = mealPlanService.getMealPlanForDate(userId, today);
    List<RecipeDetailDto> suggested = recipeService.findAll().stream().limit(5).toList();

    return ResponseEntity.ok(
        new DashboardDto(
            profileDto,
            targetCal,
            consumedCal,
            burnedCal,
            netCal,
            targetProt,
            consumedProt,
            targetCarb,
            consumedCarb,
            targetFat,
            consumedFat,
            targetWater,
            consumedWaterL,
            goalSteps,
            currentSteps,
            streakDays,
            expiringCount,
            todayMeals,
            suggested));
  }
}
