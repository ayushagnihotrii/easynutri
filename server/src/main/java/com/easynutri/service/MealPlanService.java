package com.easynutri.service;

import com.easynutri.domain.model.ProfileContext;
import com.easynutri.domain.model.RecipeCandidate;
import com.easynutri.domain.nutrition.DefaultSafetyGate;
import com.easynutri.domain.nutrition.SafetyGate;
import com.easynutri.domain.planner.DailyMealPlan;
import com.easynutri.domain.planner.DefaultMealPlanner;
import com.easynutri.domain.planner.MealSlotProposal;
import com.easynutri.domain.planner.WeeklyMealPlan;
import com.easynutri.domain.rules.RuleVerdict;
import com.easynutri.dto.AddMealPlanRequest;
import com.easynutri.dto.DailyPlanDto;
import com.easynutri.dto.MealPlanItemDto;
import com.easynutri.dto.RecipeDetailDto;
import com.easynutri.dto.WeeklyMealPlanResponseDto;
import com.easynutri.entity.MealPlanItemEntity;
import com.easynutri.entity.PantryItemEntity;
import com.easynutri.entity.RecipeEntity;
import com.easynutri.entity.RecipeIngredientEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.MealPlanItemRepository;
import com.easynutri.repository.PantryItemRepository;
import com.easynutri.repository.RecipeRepository;
import com.easynutri.repository.UserProfileRepository;
import com.easynutri.repository.UserRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MealPlanService {

  private final MealPlanItemRepository mealPlanItemRepository;
  private final UserRepository userRepository;
  private final RecipeRepository recipeRepository;
  private final PantryItemRepository pantryItemRepository;
  private final UserProfileRepository userProfileRepository;
  private final RecipeService recipeService;
  private final SafetyGate safetyGate = new DefaultSafetyGate();
  private final DefaultMealPlanner mealPlanner = new DefaultMealPlanner();

  public MealPlanService(
      MealPlanItemRepository mealPlanItemRepository,
      UserRepository userRepository,
      RecipeRepository recipeRepository,
      PantryItemRepository pantryItemRepository,
      UserProfileRepository userProfileRepository,
      RecipeService recipeService) {
    this.mealPlanItemRepository = mealPlanItemRepository;
    this.userRepository = userRepository;
    this.recipeRepository = recipeRepository;
    this.pantryItemRepository = pantryItemRepository;
    this.userProfileRepository = userProfileRepository;
    this.recipeService = recipeService;
  }

  @Transactional(readOnly = true)
  public List<MealPlanItemDto> getMealPlanForDate(Long userId, LocalDate date) {
    return mealPlanItemRepository
        .findByUserIdAndPlannedDateOrderByMealSlotAsc(userId, date)
        .stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<MealPlanItemDto> getMealPlanForRange(Long userId, LocalDate start, LocalDate end) {
    return mealPlanItemRepository
        .findByUserIdAndPlannedDateBetweenOrderByPlannedDateAscMealSlotAsc(userId, start, end)
        .stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  public MealPlanItemDto addMealPlanItem(Long userId, AddMealPlanRequest request) {
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    RecipeEntity recipe =
        recipeRepository
            .findById(request.recipeId())
            .orElseThrow(
                () -> new IllegalArgumentException("Recipe not found: " + request.recipeId()));

    MealPlanItemEntity entity =
        new MealPlanItemEntity(
            user,
            recipe,
            request.plannedDate(),
            request.mealSlot(),
            request.servings() != null ? request.servings() : 2);

    MealPlanItemEntity saved = mealPlanItemRepository.save(entity);
    return toDto(saved);
  }

  public WeeklyMealPlanResponseDto generateIntelligentWeek(Long userId, LocalDate inputWeekStart) {
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    LocalDate weekStart =
        inputWeekStart != null
            ? inputWeekStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            : LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    LocalDate weekEnd = weekStart.plusDays(6);

    ProfileContext profile = getProfileContext(userId);
    int targetCalories = 2000;

    // 1. Convert all recipes to RecipeCandidate domain models
    List<RecipeEntity> allRecipes = recipeRepository.findAll();
    List<RecipeCandidate> candidateRecipes =
        allRecipes.stream().map(this::toCandidate).collect(Collectors.toList());

    // 2. Fetch existing locked meals in range to preserve
    List<MealPlanItemEntity> existing =
        mealPlanItemRepository.findByUserIdAndPlannedDateBetweenOrderByPlannedDateAscMealSlotAsc(
            userId, weekStart, weekEnd);

    Map<LocalDate, Map<String, MealSlotProposal>> lockedMeals = new HashMap<>();
    for (MealPlanItemEntity item : existing) {
      if (item.isLocked()) {
        RecipeCandidate cand = toCandidate(item.getRecipe());
        MealSlotProposal proposal =
            new MealSlotProposal(
                cand,
                item.getMealSlot(),
                item.getServings(),
                item.getSelectionReason() != null ? item.getSelectionReason() : "User locked meal",
                true,
                item.isCompleted());
        lockedMeals
            .computeIfAbsent(item.getPlannedDate(), k -> new HashMap<>())
            .put(item.getMealSlot().toUpperCase(Locale.ROOT), proposal);
      }
    }

    // 3. Fetch pantry and expiry data
    List<PantryItemEntity> pantryItems = pantryItemRepository.findByUserId(userId);
    Map<String, Double> pantryStock = new HashMap<>();
    Set<String> expiringIngredients = new HashSet<>();
    LocalDate today = LocalDate.now();

    for (PantryItemEntity pi : pantryItems) {
      pantryStock.put(pi.getFood().getId(), pi.getQuantityG());
      if (pi.getExpirationDate() != null
          && ChronoUnit.DAYS.between(today, pi.getExpirationDate()) <= 3) {
        expiringIngredients.add(pi.getFood().getNameEn());
        expiringIngredients.add(pi.getFood().getId());
      }
    }

    // 4. Generate pure Java domain weekly plan
    WeeklyMealPlan plan =
        mealPlanner.generateIntelligentWeeklyPlan(
            weekStart,
            profile,
            candidateRecipes,
            pantryStock,
            expiringIngredients,
            lockedMeals,
            targetCalories);

    // 5. Delete unlocked existing meals and persist new proposals
    mealPlanItemRepository.deleteByUserIdAndPlannedDateBetweenAndLockedFalse(
        userId, weekStart, weekEnd);

    for (DailyMealPlan day : plan.days()) {
      for (MealSlotProposal meal : day.meals()) {
        if (meal.locked()) continue; // Already preserved

        RecipeEntity r = recipeRepository.findById(meal.recipe().candidateId()).orElse(null);
        if (r != null) {
          MealPlanItemEntity item =
              new MealPlanItemEntity(user, r, day.date(), meal.mealSlot(), meal.servings());
          item.setSelectionReason(meal.selectionReason());
          mealPlanItemRepository.save(item);
        }
      }
    }

    return getWeeklyPlan(userId, weekStart);
  }

  @Transactional(readOnly = true)
  public WeeklyMealPlanResponseDto getWeeklyPlan(Long userId, LocalDate inputWeekStart) {
    LocalDate weekStart =
        inputWeekStart != null
            ? inputWeekStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            : LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    LocalDate weekEnd = weekStart.plusDays(6);

    List<MealPlanItemEntity> items =
        mealPlanItemRepository.findByUserIdAndPlannedDateBetweenOrderByPlannedDateAscMealSlotAsc(
            userId, weekStart, weekEnd);

    Map<LocalDate, List<MealPlanItemEntity>> itemsByDate =
        items.stream().collect(Collectors.groupingBy(MealPlanItemEntity::getPlannedDate));

    List<DailyPlanDto> days = new ArrayList<>();
    double totalCalories = 0.0;
    double totalProtein = 0.0;

    for (int i = 0; i < 7; i++) {
      LocalDate currentDate = weekStart.plusDays(i);
      List<MealPlanItemEntity> dayItems =
          itemsByDate.getOrDefault(currentDate, List.of()).stream()
              .sorted((a, b) -> slotOrder(a.getMealSlot()) - slotOrder(b.getMealSlot()))
              .collect(Collectors.toList());

      List<MealPlanItemDto> mealDtos = new ArrayList<>();
      double dayCal = 0.0;
      double dayPro = 0.0;
      double dayCarb = 0.0;
      double dayFat = 0.0;

      for (MealPlanItemEntity item : dayItems) {
        MealPlanItemDto dto = toDto(item);
        mealDtos.add(dto);
        dayCal += dto.calories();
        dayPro += dto.proteinG();
        dayCarb += dto.carbsG();
        dayFat += dto.fatG();
      }

      totalCalories += dayCal;
      totalProtein += dayPro;

      days.add(
          new DailyPlanDto(
              currentDate,
              currentDate.getDayOfWeek().name(),
              mealDtos,
              Math.round(dayCal * 10.0) / 10.0,
              Math.round(dayPro * 10.0) / 10.0,
              Math.round(dayCarb * 10.0) / 10.0,
              Math.round(dayFat * 10.0) / 10.0));
    }

    return new WeeklyMealPlanResponseDto(
        weekStart,
        weekEnd,
        days,
        Math.round(totalCalories * 10.0) / 10.0,
        Math.round(totalProtein * 10.0) / 10.0,
        2100.0,
        75.0);
  }

  public MealPlanItemDto replaceMeal(Long userId, Long itemId, String newRecipeId) {
    MealPlanItemEntity item =
        mealPlanItemRepository
            .findByIdAndUserId(itemId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Meal plan item not found: " + itemId));

    RecipeEntity newRecipe =
        recipeRepository
            .findById(newRecipeId)
            .orElseThrow(() -> new IllegalArgumentException("Recipe not found: " + newRecipeId));

    item.setRecipe(newRecipe);
    item.setSelectionReason("Swapped alternative meal");
    return toDto(mealPlanItemRepository.save(item));
  }

  @Transactional(readOnly = true)
  public List<RecipeDetailDto> getReplacementAlternatives(Long userId, Long itemId) {
    MealPlanItemEntity item =
        mealPlanItemRepository
            .findByIdAndUserId(itemId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Meal plan item not found: " + itemId));

    ProfileContext profile = getProfileContext(userId);
    String currentRecipeId = item.getRecipe().getId();
    String slot = item.getMealSlot();

    List<RecipeEntity> candidates = recipeRepository.findAll();
    List<RecipeDetailDto> safeAlternatives = new ArrayList<>();

    for (RecipeEntity r : candidates) {
      if (r.getId().equals(currentRecipeId)) continue;
      if (slot != null && !slot.equalsIgnoreCase(r.getMealSlot())) continue;

      RecipeCandidate cand = toCandidate(r);
      if (safetyGate.verifyRecipe(profile, cand).verdict() != RuleVerdict.BLOCK) {
        safeAlternatives.add(recipeService.computeScaledNutrition(r, item.getServings()));
      }

      if (safeAlternatives.size() >= 3) break;
    }

    return safeAlternatives;
  }

  public boolean toggleLock(Long userId, Long itemId) {
    MealPlanItemEntity item =
        mealPlanItemRepository
            .findByIdAndUserId(itemId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Meal plan item not found: " + itemId));

    item.setLocked(!item.isLocked());
    mealPlanItemRepository.save(item);
    return item.isLocked();
  }

  public boolean toggleComplete(Long userId, Long itemId) {
    MealPlanItemEntity item =
        mealPlanItemRepository
            .findByIdAndUserId(itemId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Meal plan item not found: " + itemId));

    item.setCompleted(!item.isCompleted());
    mealPlanItemRepository.save(item);
    return item.isCompleted();
  }

  public void updateServings(Long userId, Long itemId, int servings) {
    MealPlanItemEntity item =
        mealPlanItemRepository
            .findByIdAndUserId(itemId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Meal plan item not found: " + itemId));

    item.setServings(Math.max(1, servings));
    mealPlanItemRepository.save(item);
  }

  public void removeMealPlanItem(Long userId, Long itemId) {
    mealPlanItemRepository
        .findById(itemId)
        .filter(item -> item.getUser().getId().equals(userId))
        .ifPresent(mealPlanItemRepository::delete);
  }

  private int slotOrder(String slot) {
    if (slot == null) return 99;
    return switch (slot.toUpperCase(Locale.ROOT)) {
      case "BREAKFAST" -> 1;
      case "LUNCH" -> 2;
      case "SNACK" -> 3;
      case "DINNER" -> 4;
      default -> 5;
    };
  }

  private RecipeCandidate toCandidate(RecipeEntity entity) {
    List<String> foodIds =
        entity.getIngredients().stream()
            .map(RecipeIngredientEntity::getFood)
            .map(f -> f.getId())
            .collect(Collectors.toList());

    List<String> tags =
        entity.getTags() != null
            ? Arrays.stream(entity.getTags().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList())
            : List.of();

    RecipeDetailDto detail = recipeService.computeScaledNutrition(entity, entity.getBaseServings());

    return new RecipeCandidate(
        entity.getId(),
        entity.getTitleEn(),
        foodIds,
        entity.getMealSlot(),
        entity.getMealRole(),
        tags,
        detail.perServingCalories(),
        detail.perServingProteinG(),
        detail.perServingCarbG(),
        detail.perServingFatG());
  }

  private ProfileContext getProfileContext(Long userId) {
    if (userId == null) {
      return new ProfileContext(
          "default", 28, "MALE", 175.0, 70.0, "VEGETARIAN", List.of(), List.of());
    }

    return userProfileRepository
        .findByUserId(userId)
        .map(
            up ->
                new ProfileContext(
                    String.valueOf(up.getId()),
                    up.getAge(),
                    up.getBiologicalSex(),
                    up.getHeightCm(),
                    up.getWeightKg(),
                    up.getDietaryPreference(),
                    List.copyOf(up.getAllergies()),
                    List.copyOf(up.getHealthConditions())))
        .orElseGet(
            () ->
                new ProfileContext(
                    String.valueOf(userId),
                    28,
                    "MALE",
                    175.0,
                    70.0,
                    "VEGETARIAN",
                    List.of(),
                    List.of()));
  }

  private MealPlanItemDto toDto(MealPlanItemEntity entity) {
    RecipeEntity r = entity.getRecipe();
    RecipeDetailDto detail = recipeService.computeScaledNutrition(r, entity.getServings());

    return new MealPlanItemDto(
        entity.getId(),
        r.getId(),
        r.getTitleEn(),
        entity.getPlannedDate(),
        entity.getMealSlot(),
        entity.getServings(),
        entity.isLocked(),
        entity.isCompleted(),
        entity.getSelectionReason() != null ? entity.getSelectionReason() : "Planned meal",
        entity.getNotes() != null ? entity.getNotes() : "",
        detail.totalCalories(),
        detail.totalProteinG(),
        detail.totalCarbG(),
        detail.totalFatG(),
        r.getPrepMinutes(),
        r.getCookMinutes());
  }
}
