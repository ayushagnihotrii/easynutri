package com.easynutri.service;

import com.easynutri.domain.grocery.DefaultGroceryBuilder;
import com.easynutri.domain.grocery.DefaultGroceryBuilder.ReconciledItem;
import com.easynutri.dto.AddGroceryItemRequest;
import com.easynutri.dto.AddMissingGroceryRequest;
import com.easynutri.dto.AddPantryItemRequest;
import com.easynutri.dto.GroceryItemDto;
import com.easynutri.entity.FoodEntity;
import com.easynutri.entity.GroceryItemEntity;
import com.easynutri.entity.MealPlanItemEntity;
import com.easynutri.entity.PantryItemEntity;
import com.easynutri.entity.RecipeEntity;
import com.easynutri.entity.RecipeIngredientEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.FoodRepository;
import com.easynutri.repository.GroceryItemRepository;
import com.easynutri.repository.MealPlanItemRepository;
import com.easynutri.repository.PantryItemRepository;
import com.easynutri.repository.RecipeRepository;
import com.easynutri.repository.UserRepository;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GroceryService {

  private final GroceryItemRepository groceryItemRepository;
  private final UserRepository userRepository;
  private final FoodRepository foodRepository;
  private final MealPlanItemRepository mealPlanItemRepository;
  private final RecipeRepository recipeRepository;
  private final PantryItemRepository pantryItemRepository;
  private final PantryService pantryService;
  private final DefaultGroceryBuilder groceryBuilder = new DefaultGroceryBuilder();

  public GroceryService(
      GroceryItemRepository groceryItemRepository,
      UserRepository userRepository,
      FoodRepository foodRepository,
      MealPlanItemRepository mealPlanItemRepository,
      RecipeRepository recipeRepository,
      PantryItemRepository pantryItemRepository,
      PantryService pantryService) {
    this.groceryItemRepository = groceryItemRepository;
    this.userRepository = userRepository;
    this.foodRepository = foodRepository;
    this.mealPlanItemRepository = mealPlanItemRepository;
    this.recipeRepository = recipeRepository;
    this.pantryItemRepository = pantryItemRepository;
    this.pantryService = pantryService;
  }

  @Transactional(readOnly = true)
  public List<GroceryItemDto> getGroceryItems(Long userId) {
    return groceryItemRepository.findByUserIdOrderByCheckedAscCreatedAtDesc(userId).stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  public GroceryItemDto addItem(Long userId, AddGroceryItemRequest request) {
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    FoodEntity food = null;
    if (request.foodId() != null && !request.foodId().isBlank()) {
      food = foodRepository.findById(request.foodId()).orElse(null);
    }

    GroceryItemEntity item =
        new GroceryItemEntity(
            user,
            food,
            request.itemName(),
            request.quantity() != null ? request.quantity() : 1.0,
            request.unit(),
            request.category(),
            false);

    GroceryItemEntity saved = groceryItemRepository.save(item);
    return toDto(saved);
  }

  public int addMissingIngredients(Long userId, AddMissingGroceryRequest request) {
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    int count = 0;
    for (String missingName : request.missingIngredients()) {
      if (missingName == null || missingName.isBlank()) continue;
      String clean = missingName.trim();
      Optional<FoodEntity> foodOpt = foodRepository.findById(clean);
      if (foodOpt.isEmpty()) {
        List<FoodEntity> foods = foodRepository.findByNameEnContainingIgnoreCase(clean);
        if (!foods.isEmpty()) {
          foodOpt = Optional.of(foods.get(0));
        }
      }

      FoodEntity food = foodOpt.orElse(null);
      String displayName = food != null ? food.getNameEn() : clean;
      String category = food != null ? food.getCategory() : "GENERAL";

      GroceryItemEntity entity =
          new GroceryItemEntity(user, food, displayName, 1.0, "serving", category, false);
      groceryItemRepository.save(entity);
      count++;
    }
    return count;
  }

  public int reconcileWithWeeklyPlan(Long userId, LocalDate weekStart, LocalDate weekEnd) {
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    // 1. Fetch meal plan items in date range
    List<MealPlanItemEntity> plannedMeals =
        mealPlanItemRepository.findByUserIdAndPlannedDateBetweenOrderByPlannedDateAscMealSlotAsc(
            userId, weekStart, weekEnd);

    if (plannedMeals.isEmpty()) {
      return 0;
    }

    // 2. Aggregate demand grams from planned meals
    Map<String, Double> demandGrams = new HashMap<>();
    Map<String, String> foodCategories = new HashMap<>();
    Map<String, FoodEntity> foodEntities = new HashMap<>();

    for (MealPlanItemEntity meal : plannedMeals) {
      RecipeEntity recipe = meal.getRecipe();
      double scale = (double) meal.getServings() / Math.max(1, recipe.getBaseServings());

      for (RecipeIngredientEntity ing : recipe.getIngredients()) {
        FoodEntity food = ing.getFood();
        String foodId = food.getId();
        double requiredG = ing.getAmountG() * scale;

        demandGrams.put(foodId, demandGrams.getOrDefault(foodId, 0.0) + requiredG);
        foodCategories.putIfAbsent(foodId, food.getCategory());
        foodEntities.putIfAbsent(foodId, food);
      }
    }

    // 3. Collect user's pantry inventory grams
    List<PantryItemEntity> pantryItems = pantryItemRepository.findByUserId(userId);
    Map<String, Double> pantryStockGrams = new HashMap<>();
    for (PantryItemEntity pi : pantryItems) {
      String fid = pi.getFood().getId();
      pantryStockGrams.put(fid, pantryStockGrams.getOrDefault(fid, 0.0) + pi.getQuantityG());
    }

    // 4. Run reconciliation calculation via pure Java domain builder
    List<ReconciledItem> reconciled =
        groceryBuilder.buildReconciledList(demandGrams, foodCategories, pantryStockGrams);

    // 5. Delete previous auto-generated items (preserving manual additions and checked items)
    groceryItemRepository.deleteByUserIdAndIsAutoGeneratedTrue(userId);

    // 6. Save items that need purchase
    int createdCount = 0;
    for (ReconciledItem r : reconciled) {
      if (r.toBuyGrams() > 0) {
        FoodEntity food = foodEntities.get(r.foodId());
        String name = food != null ? food.getNameEn() : r.itemName();
        GroceryItemEntity entity =
            new GroceryItemEntity(user, food, name, r.toBuyGrams(), "g", r.category(), true);
        groceryItemRepository.save(entity);
        createdCount++;
      }
    }

    return createdCount;
  }

  public void transferToPantry(
      Long userId, Long groceryItemId, LocalDate expirationDate, String storageLocation) {
    GroceryItemEntity item =
        groceryItemRepository
            .findByIdAndUserId(groceryItemId, userId)
            .orElseThrow(
                () -> new IllegalArgumentException("Grocery item not found: " + groceryItemId));

    String foodId = item.getFood() != null ? item.getFood().getId() : null;
    if (foodId == null) {
      List<FoodEntity> foods = foodRepository.findByNameEnContainingIgnoreCase(item.getItemName());
      if (!foods.isEmpty()) {
        foodId = foods.get(0).getId();
      }
    }

    if (foodId != null) {
      LocalDate expiry = expirationDate != null ? expirationDate : LocalDate.now().plusDays(7);
      double qty =
          item.getQuantity() > 0
              ? (item.getQuantity() < 10.0 ? item.getQuantity() * 100.0 : item.getQuantity())
              : 250.0;

      pantryService.addPantryItem(
          userId,
          new AddPantryItemRequest(
              foodId,
              qty,
              item.getUnit(),
              storageLocation != null ? storageLocation : "CUPBOARD",
              expiry,
              LocalDate.now(),
              "Transferred from grocery checklist"));
    }

    item.setChecked(true);
    groceryItemRepository.save(item);
  }

  public boolean toggleChecked(Long userId, Long itemId) {
    return groceryItemRepository
        .findById(itemId)
        .filter(item -> item.getUser().getId().equals(userId))
        .map(
            item -> {
              item.setChecked(!item.isChecked());
              groceryItemRepository.save(item);
              return item.isChecked();
            })
        .orElse(false);
  }

  public void deleteItem(Long userId, Long itemId) {
    groceryItemRepository
        .findById(itemId)
        .filter(item -> item.getUser().getId().equals(userId))
        .ifPresent(groceryItemRepository::delete);
  }

  public void clearCompleted(Long userId) {
    groceryItemRepository.deleteByUserIdAndCheckedTrue(userId);
  }

  private GroceryItemDto toDto(GroceryItemEntity entity) {
    return new GroceryItemDto(
        entity.getId(),
        entity.getItemName(),
        entity.getQuantity(),
        entity.getUnit(),
        entity.isChecked(),
        entity.getCategory(),
        entity.getFood() != null ? entity.getFood().getId() : null,
        entity.isAutoGenerated());
  }
}
