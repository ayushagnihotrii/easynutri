package com.easynutri.service;

import com.easynutri.dto.LogFoodItemRequest;
import com.easynutri.dto.LogStepsRequest;
import com.easynutri.dto.LogWaterRequest;
import com.easynutri.dto.LogWorkoutRequest;
import com.easynutri.entity.CommunityRecipeEntity;
import com.easynutri.entity.SavedRecipeEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.CommunityRecipeRepository;
import com.easynutri.repository.FoodDiaryEntryRepository;
import com.easynutri.repository.GroceryItemRepository;
import com.easynutri.repository.MealPlanItemRepository;
import com.easynutri.repository.RecipeRepository;
import com.easynutri.repository.SavedRecipeRepository;
import com.easynutri.repository.StepRecordRepository;
import com.easynutri.repository.UserRepository;
import com.easynutri.repository.WaterLogRepository;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DemoDataSeederService {

  private static final Logger log = LoggerFactory.getLogger(DemoDataSeederService.class);

  private final UserRepository userRepository;
  private final RecipeRepository recipeRepository;
  private final DiaryService diaryService;
  private final HydrationService hydrationService;
  private final ActivityTrackingService activityTrackingService;
  private final MealPlanService mealPlanService;
  private final SavedRecipeRepository savedRecipeRepository;
  private final GamificationService gamificationService;
  private final CommunityRecipeRepository communityRecipeRepository;
  private final FoodDiaryEntryRepository diaryEntryRepository;
  private final WaterLogRepository waterLogRepository;
  private final StepRecordRepository stepRecordRepository;
  private final MealPlanItemRepository mealPlanItemRepository;
  private final GroceryService groceryService;
  private final GroceryItemRepository groceryItemRepository;

  public DemoDataSeederService(
      UserRepository userRepository,
      RecipeRepository recipeRepository,
      DiaryService diaryService,
      HydrationService hydrationService,
      ActivityTrackingService activityTrackingService,
      MealPlanService mealPlanService,
      SavedRecipeRepository savedRecipeRepository,
      GamificationService gamificationService,
      CommunityRecipeRepository communityRecipeRepository,
      FoodDiaryEntryRepository diaryEntryRepository,
      WaterLogRepository waterLogRepository,
      StepRecordRepository stepRecordRepository,
      MealPlanItemRepository mealPlanItemRepository,
      GroceryService groceryService,
      GroceryItemRepository groceryItemRepository) {
    this.userRepository = userRepository;
    this.recipeRepository = recipeRepository;
    this.diaryService = diaryService;
    this.hydrationService = hydrationService;
    this.activityTrackingService = activityTrackingService;
    this.mealPlanService = mealPlanService;
    this.savedRecipeRepository = savedRecipeRepository;
    this.gamificationService = gamificationService;
    this.communityRecipeRepository = communityRecipeRepository;
    this.diaryEntryRepository = diaryEntryRepository;
    this.waterLogRepository = waterLogRepository;
    this.stepRecordRepository = stepRecordRepository;
    this.mealPlanItemRepository = mealPlanItemRepository;
    this.groceryService = groceryService;
    this.groceryItemRepository = groceryItemRepository;
  }

  public void seedPresentationDemoData() {
    try {
      UserEntity demoUser = userRepository.findByUsername("demo").orElse(null);
      if (demoUser == null) {
        return;
      }
      Long userId = demoUser.getId();
      LocalDate today = LocalDate.now();

      // 1. Food Diary: Seed today's clinical meals if empty
      if (diaryEntryRepository.findByUserIdAndDiaryDate(userId, today).isEmpty()) {
        diaryService.logItem(
            userId,
            new LogFoodItemRequest(
                today, "BREAKFAST", null, "recipe_veg_poha", "Vegetable Poha", 250.0, 1.0));
        diaryService.logItem(
            userId,
            new LogFoodItemRequest(
                today,
                "LUNCH",
                null,
                "recipe_dal_tadka",
                "Homestyle Dal Tadka with Steamed Rice",
                350.0,
                1.0));
        diaryService.logItem(
            userId,
            new LogFoodItemRequest(
                today, "SNACK", "food_curd_dahi", null, "Fresh Curd with Banana", 200.0, 1.0));

        log.info("Initialized rich clinical food diary entries for 'demo' user.");
      }

      // 2. Hydration: Seed today's water intake if empty
      if (waterLogRepository.findByUserIdAndRecordDateOrderByLoggedAtAsc(userId, today).isEmpty()) {
        hydrationService.logWater(
            userId, new LogWaterRequest(today, 350, "Morning warm water with lemon"));
        hydrationService.logWater(
            userId, new LogWaterRequest(today, 500, "Mid-morning hydration break"));
        hydrationService.logWater(
            userId, new LogWaterRequest(today, 400, "Post-lunch cellular hydration"));
        hydrationService.logWater(
            userId, new LogWaterRequest(today, 500, "Afternoon wellness hydration"));

        log.info("Initialized 1,750 ml water logs for 'demo' user.");
      }

      // 3. Activity & Steps: Seed today's steps & workout
      if (stepRecordRepository.findByUserIdAndRecordDate(userId, today).isEmpty()) {
        activityTrackingService.logSteps(userId, new LogStepsRequest(today, 6420, 10000, "MANUAL"));
        activityTrackingService.logWorkout(
            userId,
            new LogWorkoutRequest(
                today, "WALKING", "MODERATE", 30, 2.4, 145.0, "Morning brisk park walk"));

        log.info("Initialized 6,420 steps & morning brisk workout for 'demo' user.");
      }

      // 4. Meal Planner: Generate week plan if empty
      if (mealPlanItemRepository.findByUserId(userId).isEmpty()) {
        try {
          mealPlanService.generateIntelligentWeek(userId, today);
          log.info("Generated weekly intelligent meal plan for 'demo' user.");
        } catch (Exception e) {
          log.warn("Auto meal plan generation note: {}", e.getMessage());
        }
      }

      // 4b. Grocery List: Auto-reconcile missing items against pantry stock
      if (groceryItemRepository.findByUserIdOrderByCheckedAscCreatedAtDesc(userId).isEmpty()) {
        try {
          groceryService.reconcileWithWeeklyPlan(userId, today, today.plusDays(6));
          log.info("Auto-reconciled grocery shopping list for 'demo' user.");
        } catch (Exception e) {
          log.warn("Auto grocery reconciliation note: {}", e.getMessage());
        }
      }

      // 5. Saved Recipes: Bookmarks
      if (savedRecipeRepository.findByUserIdOrderByCreatedAtDesc(userId).isEmpty()) {
        recipeRepository
            .findById("recipe_spinach_khichdi")
            .ifPresent(
                r ->
                    savedRecipeRepository.save(
                        new SavedRecipeEntity(demoUser, r, "High-iron quick dinner")));
        recipeRepository
            .findById("recipe_dal_tadka")
            .ifPresent(
                r ->
                    savedRecipeRepository.save(
                        new SavedRecipeEntity(demoUser, r, "Protein lunch staple")));
        recipeRepository
            .findById("recipe_veg_pulao")
            .ifPresent(
                r ->
                    savedRecipeRepository.save(
                        new SavedRecipeEntity(demoUser, r, "Balanced family weekend meal")));
        log.info("Bookmarked 3 clinical Indian recipes for 'demo' user.");
      }

      // 6. Gamification Badges
      try {
        gamificationService.checkAndAwardBadges(userId);
        log.info("Awarded clinical gamification badges for 'demo' user.");
      } catch (Exception e) {
        log.warn("Gamification check note: {}", e.getMessage());
      }

      // 7. Community Recipes Feed
      if (communityRecipeRepository.count() == 0) {
        CommunityRecipeEntity comm1 = new CommunityRecipeEntity();
        comm1.setAuthor(demoUser);
        comm1.setTitle("High-Protein Moong Dal Cheela");
        comm1.setDescription(
            "Savory Indian lentil crepes loaded with grated paneer and coriander leaves. Packed with 16g plant protein per serving.");
        comm1.setBaseServings(2);
        comm1.setPrepMinutes(10);
        comm1.setCookMinutes(10);
        comm1.setMealSlot("BREAKFAST");
        comm1.setDietaryTags("high_protein,vegetarian,gluten_free,quick_cook");
        comm1.setInstructionsText(
            "1. Soak yellow moong dal for 2 hours and grind with ginger and green chilli into a smooth batter.\n2. Spread a ladleful onto a hot greased griddle.\n3. Top with freshly grated paneer and cook both sides until golden.\n4. Serve hot with mint chutney.");
        comm1.setCaloriesPerServing(280.0);
        comm1.setProteinG(16.5);
        comm1.setCarbsG(32.0);
        comm1.setFatG(8.0);
        comm1.setFiberG(6.0);
        comm1.setStatus("APPROVED");
        comm1.setLikeCount(24);
        comm1.setCommentCount(3);
        communityRecipeRepository.save(comm1);
        log.info("Seeded approved community showcase recipe.");
      }

    } catch (Exception e) {
      log.error("Failed to seed presentation demo data: {}", e.getMessage(), e);
    }
  }
}
