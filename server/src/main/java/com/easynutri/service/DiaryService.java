package com.easynutri.service;

import com.easynutri.domain.diary.DiaryAggregator;
import com.easynutri.domain.diary.DiaryDaySummary;
import com.easynutri.domain.diary.DiaryItemSnapshot;
import com.easynutri.domain.diary.MealSlot;
import com.easynutri.domain.policy.DisplayPolicy;
import com.easynutri.dto.*;
import com.easynutri.entity.*;
import com.easynutri.repository.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DiaryService {

  private final FoodDiaryEntryRepository diaryEntryRepository;
  private final FoodDiaryItemRepository diaryItemRepository;
  private final FoodRepository foodRepository;
  private final RecipeService recipeService;
  private final UserRepository userRepository;
  private final UserProfileRepository userProfileRepository;
  private final CookingSessionRepository cookingSessionRepository;

  public DiaryService(
      FoodDiaryEntryRepository diaryEntryRepository,
      FoodDiaryItemRepository diaryItemRepository,
      FoodRepository foodRepository,
      RecipeService recipeService,
      UserRepository userRepository,
      UserProfileRepository userProfileRepository,
      CookingSessionRepository cookingSessionRepository) {
    this.diaryEntryRepository = diaryEntryRepository;
    this.diaryItemRepository = diaryItemRepository;
    this.foodRepository = foodRepository;
    this.recipeService = recipeService;
    this.userRepository = userRepository;
    this.userProfileRepository = userProfileRepository;
    this.cookingSessionRepository = cookingSessionRepository;
  }

  @Transactional(readOnly = true)
  public DiaryDayDto getDayDiary(Long userId, LocalDate date) {
    LocalDate targetDate = date != null ? date : LocalDate.now();

    List<FoodDiaryEntryEntity> entries =
        diaryEntryRepository.findByUserIdAndDiaryDate(userId, targetDate);

    Optional<UserProfileEntity> profileOpt = userProfileRepository.findByUserId(userId);
    boolean sensitiveMode = profileOpt.map(UserProfileEntity::isSensitiveMode).orElse(false);
    Integer targetCal = profileOpt.map(UserProfileEntity::getTargetCalories).orElse(2000);
    Integer targetProtein = profileOpt.map(UserProfileEntity::getTargetProteinG).orElse(65);
    Integer targetCarbs = profileOpt.map(UserProfileEntity::getTargetCarbG).orElse(250);
    Integer targetFat = profileOpt.map(UserProfileEntity::getTargetFatG).orElse(55);

    Map<MealSlot, List<DiaryItemSnapshot>> domainSlotMap = new EnumMap<>(MealSlot.class);
    Map<MealSlot, List<DiaryItemDto>> dtoSlotMap = new EnumMap<>(MealSlot.class);

    for (MealSlot slot : MealSlot.values()) {
      domainSlotMap.put(slot, new ArrayList<>());
      dtoSlotMap.put(slot, new ArrayList<>());
    }

    for (FoodDiaryEntryEntity entry : entries) {
      MealSlot slot = MealSlot.fromString(entry.getMealSlot());
      for (FoodDiaryItemEntity item : entry.getItems()) {
        DiaryItemSnapshot snap =
            new DiaryItemSnapshot(
                item.getFoodId(),
                item.getRecipeId(),
                item.getItemName(),
                item.getAmountG(),
                item.getServings(),
                item.getCalories(),
                item.getProteinG(),
                item.getCarbsG(),
                item.getFatG(),
                item.getFiberG(),
                item.getSodiumMg());

        domainSlotMap.get(slot).add(snap);

        dtoSlotMap
            .get(slot)
            .add(
                new DiaryItemDto(
                    item.getId(),
                    entry.getId(),
                    item.getFoodId(),
                    item.getRecipeId(),
                    item.getItemName(),
                    item.getAmountG(),
                    item.getServings(),
                    item.getCalories(),
                    item.getProteinG(),
                    item.getCarbsG(),
                    item.getFatG(),
                    item.getFiberG(),
                    item.getSodiumMg()));
      }
    }

    DiaryDaySummary summary = DiaryAggregator.aggregateDay(domainSlotMap);

    List<DiarySlotDto> slotDtos = new ArrayList<>();
    for (MealSlot slot : MealSlot.values()) {
      List<DiaryItemDto> items = dtoSlotMap.get(slot);
      double slotCal = 0.0,
          slotProt = 0.0,
          slotCarb = 0.0,
          slotFat = 0.0,
          slotFiber = 0.0,
          slotSod = 0.0;
      for (DiaryItemDto it : items) {
        slotCal += it.calories();
        slotProt += it.proteinG();
        slotCarb += it.carbsG();
        slotFat += it.fatG();
        slotFiber += it.fiberG();
        slotSod += it.sodiumMg();
      }

      slotDtos.add(
          new DiarySlotDto(
              slot,
              slot.name(),
              slot.getDisplayName(),
              slot.getIcon(),
              items,
              Math.round(slotCal * 10.0) / 10.0,
              Math.round(slotProt * 10.0) / 10.0,
              Math.round(slotCarb * 10.0) / 10.0,
              Math.round(slotFat * 10.0) / 10.0,
              Math.round(slotFiber * 10.0) / 10.0,
              Math.round(slotSod * 10.0) / 10.0));
    }

    int calProgress =
        (targetCal != null && targetCal > 0)
            ? (int) Math.min(100, (summary.totalCalories() / targetCal) * 100)
            : 0;
    int proteinProgress =
        (targetProtein != null && targetProtein > 0)
            ? (int) Math.min(100, (summary.totalProteinG() / targetProtein) * 100)
            : 0;
    int carbsProgress =
        (targetCarbs != null && targetCarbs > 0)
            ? (int) Math.min(100, (summary.totalCarbsG() / targetCarbs) * 100)
            : 0;
    int fatProgress =
        (targetFat != null && targetFat > 0)
            ? (int) Math.min(100, (summary.totalFatG() / targetFat) * 100)
            : 0;

    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.US);

    return new DiaryDayDto(
        targetDate,
        targetDate.format(fmt),
        slotDtos,
        summary.totalCalories(),
        summary.totalProteinG(),
        summary.totalCarbsG(),
        summary.totalFatG(),
        summary.totalFiberG(),
        summary.totalSodiumMg(),
        targetCal,
        targetProtein,
        targetCarbs,
        targetFat,
        sensitiveMode,
        DisplayPolicy.formatCalories(summary.totalCalories(), sensitiveMode),
        DisplayPolicy.formatMacroGrams(summary.totalProteinG(), sensitiveMode),
        DisplayPolicy.formatMacroGrams(summary.totalCarbsG(), sensitiveMode),
        DisplayPolicy.formatMacroGrams(summary.totalFatG(), sensitiveMode),
        calProgress,
        proteinProgress,
        carbsProgress,
        fatProgress);
  }

  public DiaryItemDto logItem(Long userId, LogFoodItemRequest request) {
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    LocalDate logDate = request.date() != null ? request.date() : LocalDate.now();
    MealSlot slot = MealSlot.fromString(request.mealSlot());

    FoodDiaryEntryEntity entry =
        diaryEntryRepository
            .findByUserIdAndDiaryDateAndMealSlot(userId, logDate, slot.name())
            .orElseGet(
                () -> {
                  FoodDiaryEntryEntity newEntry =
                      new FoodDiaryEntryEntity(user, logDate, slot.name());
                  return diaryEntryRepository.save(newEntry);
                });

    FoodDiaryItemEntity item = new FoodDiaryItemEntity();
    entry.addItem(item);

    double amountG =
        (request.amountG() != null && request.amountG() > 0) ? request.amountG() : 100.0;
    double servings =
        (request.servings() != null && request.servings() > 0) ? request.servings() : 1.0;
    item.setAmountG(amountG);
    item.setServings(servings);

    if (request.foodId() != null && !request.foodId().isBlank()) {
      FoodEntity food =
          foodRepository
              .findById(request.foodId())
              .orElseThrow(
                  () -> new IllegalArgumentException("Food not found: " + request.foodId()));

      item.setFoodId(food.getId());
      item.setItemName(food.getNameEn() != null ? food.getNameEn() : food.getNameHi());

      double factor = amountG / 100.0;
      item.setCalories(Math.round(food.getEnergyKcal() * factor * 10.0) / 10.0);
      item.setProteinG(Math.round(food.getProteinG() * factor * 10.0) / 10.0);
      item.setCarbsG(Math.round(food.getCarbohydrateG() * factor * 10.0) / 10.0);
      item.setFatG(Math.round(food.getFatG() * factor * 10.0) / 10.0);
      item.setFiberG(Math.round(food.getFibreG() * factor * 10.0) / 10.0);
      item.setSodiumMg(Math.round(food.getSodiumMg() * factor * 10.0) / 10.0);

    } else if (request.recipeId() != null && !request.recipeId().isBlank()) {
      RecipeDetailDto recipe =
          recipeService
              .findByIdAndServings(request.recipeId(), (int) Math.max(1, Math.round(servings)))
              .orElseThrow(
                  () -> new IllegalArgumentException("Recipe not found: " + request.recipeId()));

      item.setRecipeId(recipe.id());
      item.setItemName(recipe.titleEn());

      double servingFactor = servings;
      item.setCalories(Math.round(recipe.perServingCalories() * servingFactor * 10.0) / 10.0);
      item.setProteinG(Math.round(recipe.perServingProteinG() * servingFactor * 10.0) / 10.0);
      item.setCarbsG(Math.round(recipe.perServingCarbG() * servingFactor * 10.0) / 10.0);
      item.setFatG(Math.round(recipe.perServingFatG() * servingFactor * 10.0) / 10.0);
      double fiberPerServing =
          recipe.currentServings() > 0 ? (recipe.totalFiberG() / recipe.currentServings()) : 0.0;
      item.setFiberG(Math.round(fiberPerServing * servingFactor * 10.0) / 10.0);
      item.setSodiumMg(Math.round(150.0 * servingFactor * 10.0) / 10.0);

    } else {
      item.setItemName(
          (request.customName() != null && !request.customName().isBlank())
              ? request.customName()
              : "Quick Meal Portion");
      item.setCalories(200.0 * servings);
      item.setProteinG(8.0 * servings);
      item.setCarbsG(25.0 * servings);
      item.setFatG(6.0 * servings);
      item.setFiberG(2.0 * servings);
      item.setSodiumMg(150.0 * servings);
    }

    FoodDiaryItemEntity saved = diaryItemRepository.save(item);
    return new DiaryItemDto(
        saved.getId(),
        entry.getId(),
        saved.getFoodId(),
        saved.getRecipeId(),
        saved.getItemName(),
        saved.getAmountG(),
        saved.getServings(),
        saved.getCalories(),
        saved.getProteinG(),
        saved.getCarbsG(),
        saved.getFatG(),
        saved.getFiberG(),
        saved.getSodiumMg());
  }

  public DiaryItemDto logCookingSessionMeal(Long userId, Long sessionId, String requestedSlot) {
    CookingSessionEntity session =
        cookingSessionRepository
            .findByIdAndUserId(sessionId, userId)
            .orElseThrow(
                () -> new IllegalArgumentException("Cooking session not found: " + sessionId));

    if (session.isDiaryLogged()) {
      throw new IllegalStateException("This cooking session has already been logged to the diary.");
    }

    MealSlot slot =
        requestedSlot != null && !requestedSlot.isBlank()
            ? MealSlot.fromString(requestedSlot)
            : determineCurrentMealSlot();

    LogFoodItemRequest req =
        new LogFoodItemRequest(
            LocalDate.now(),
            slot.name(),
            null,
            session.getRecipe().getId(),
            session.getRecipe().getTitleEn(),
            (double) (session.getServings() * 200),
            (double) session.getServings());

    DiaryItemDto dto = logItem(userId, req);
    session.setDiaryLogged(true);
    cookingSessionRepository.save(session);
    return dto;
  }

  public void deleteItem(Long userId, Long itemId) {
    FoodDiaryItemEntity item =
        diaryItemRepository
            .findById(itemId)
            .orElseThrow(() -> new IllegalArgumentException("Diary item not found: " + itemId));

    if (!item.getEntry().getUser().getId().equals(userId)) {
      throw new SecurityException("Unauthorized to delete diary item.");
    }

    FoodDiaryEntryEntity entry = item.getEntry();
    entry.removeItem(item);
    diaryItemRepository.delete(item);

    if (entry.getItems().isEmpty()) {
      diaryEntryRepository.delete(entry);
    }
  }

  public void duplicateMeal(Long userId, CopyMealRequest request) {
    LocalDate srcDate = request.sourceDate() != null ? request.sourceDate() : LocalDate.now();
    LocalDate tgtDate = request.targetDate() != null ? request.targetDate() : LocalDate.now();
    MealSlot srcSlot = MealSlot.fromString(request.sourceSlot());
    MealSlot tgtSlot = MealSlot.fromString(request.targetSlot());

    Optional<FoodDiaryEntryEntity> srcEntryOpt =
        diaryEntryRepository.findByUserIdAndDiaryDateAndMealSlot(userId, srcDate, srcSlot.name());

    if (srcEntryOpt.isEmpty() || srcEntryOpt.get().getItems().isEmpty()) {
      return;
    }

    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    FoodDiaryEntryEntity tgtEntry =
        diaryEntryRepository
            .findByUserIdAndDiaryDateAndMealSlot(userId, tgtDate, tgtSlot.name())
            .orElseGet(
                () -> {
                  FoodDiaryEntryEntity newEntry =
                      new FoodDiaryEntryEntity(user, tgtDate, tgtSlot.name());
                  return diaryEntryRepository.save(newEntry);
                });

    for (FoodDiaryItemEntity srcItem : srcEntryOpt.get().getItems()) {
      FoodDiaryItemEntity copy = new FoodDiaryItemEntity();
      tgtEntry.addItem(copy);
      copy.setFoodId(srcItem.getFoodId());
      copy.setRecipeId(srcItem.getRecipeId());
      copy.setItemName(srcItem.getItemName());
      copy.setAmountG(srcItem.getAmountG());
      copy.setServings(srcItem.getServings());
      copy.setCalories(srcItem.getCalories());
      copy.setProteinG(srcItem.getProteinG());
      copy.setCarbsG(srcItem.getCarbsG());
      copy.setFatG(srcItem.getFatG());
      copy.setFiberG(srcItem.getFiberG());
      copy.setSodiumMg(srcItem.getSodiumMg());
      diaryItemRepository.save(copy);
    }
  }

  private MealSlot determineCurrentMealSlot() {
    int hour = LocalTime.now().getHour();
    if (hour >= 5 && hour < 11) {
      return MealSlot.BREAKFAST;
    } else if (hour >= 11 && hour < 16) {
      return MealSlot.LUNCH;
    } else if (hour >= 16 && hour < 22) {
      return MealSlot.DINNER;
    } else {
      return MealSlot.SNACK;
    }
  }
}
