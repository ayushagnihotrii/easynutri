package com.easynutri.service;

import com.easynutri.dto.AddPantryItemRequest;
import com.easynutri.dto.CookBeforeExpiresDto;
import com.easynutri.dto.PantryItemDto;
import com.easynutri.dto.UpdatePantryItemRequest;
import com.easynutri.entity.FoodEntity;
import com.easynutri.entity.PantryItemEntity;
import com.easynutri.entity.RecipeEntity;
import com.easynutri.entity.RecipeIngredientEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.FoodRepository;
import com.easynutri.repository.PantryItemRepository;
import com.easynutri.repository.RecipeRepository;
import com.easynutri.repository.UserRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PantryService {

  private final PantryItemRepository pantryItemRepository;
  private final UserRepository userRepository;
  private final FoodRepository foodRepository;
  private final RecipeRepository recipeRepository;

  public PantryService(
      PantryItemRepository pantryItemRepository,
      UserRepository userRepository,
      FoodRepository foodRepository,
      RecipeRepository recipeRepository) {
    this.pantryItemRepository = pantryItemRepository;
    this.userRepository = userRepository;
    this.foodRepository = foodRepository;
    this.recipeRepository = recipeRepository;
  }

  @Transactional(readOnly = true)
  public List<PantryItemDto> getUserPantry(Long userId) {
    LocalDate today = LocalDate.now();
    List<PantryItemEntity> items =
        pantryItemRepository.findByUserIdOrderByExpirationDateAsc(userId);
    return items.stream().map(item -> toDto(item, today)).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<PantryItemDto> getUserPantry(String username) {
    LocalDate today = LocalDate.now();
    List<PantryItemEntity> items = pantryItemRepository.findByUserUsername(username);
    return items.stream().map(item -> toDto(item, today)).collect(Collectors.toList());
  }

  public PantryItemDto addPantryItem(Long userId, AddPantryItemRequest req) {
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    FoodEntity food =
        foodRepository
            .findById(req.foodId())
            .orElseThrow(() -> new IllegalArgumentException("Food not found: " + req.foodId()));

    // Check if food already in pantry - if so, add to quantity
    Optional<PantryItemEntity> existing =
        pantryItemRepository.findByUserIdAndFoodId(userId, req.foodId());
    if (existing.isPresent()) {
      PantryItemEntity item = existing.get();
      item.setQuantityG(item.getQuantityG() + (req.quantityG() != null ? req.quantityG() : 100.0));
      if (req.expirationDate() != null) {
        item.setExpirationDate(req.expirationDate());
      }
      if (req.storageLocation() != null) {
        item.setStorageLocation(req.storageLocation());
      }
      return toDto(pantryItemRepository.save(item), LocalDate.now());
    }

    double qty = req.quantityG() != null && req.quantityG() > 0 ? req.quantityG() : 100.0;
    PantryItemEntity item =
        new PantryItemEntity(
            user,
            food,
            qty,
            req.unit() != null ? req.unit() : "g",
            req.storageLocation() != null ? req.storageLocation() : "CUPBOARD",
            req.expirationDate(),
            req.purchaseDate() != null ? req.purchaseDate() : LocalDate.now(),
            req.notes());

    return toDto(pantryItemRepository.save(item), LocalDate.now());
  }

  public PantryItemEntity addPantryItem(
      String username, String foodId, double quantityG, LocalDate expirationDate) {
    UserEntity user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    FoodEntity food =
        foodRepository
            .findById(foodId)
            .orElseThrow(() -> new IllegalArgumentException("Food not found: " + foodId));

    PantryItemEntity item = new PantryItemEntity(user, food, quantityG, expirationDate);
    return pantryItemRepository.save(item);
  }

  public PantryItemDto updatePantryItem(Long userId, Long itemId, UpdatePantryItemRequest req) {
    PantryItemEntity item =
        pantryItemRepository
            .findByIdAndUserId(itemId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Pantry item not found: " + itemId));

    if (req.quantityG() != null) item.setQuantityG(req.quantityG());
    if (req.unit() != null) item.setUnit(req.unit());
    if (req.storageLocation() != null) item.setStorageLocation(req.storageLocation());
    if (req.expirationDate() != null) item.setExpirationDate(req.expirationDate());
    if (req.notes() != null) item.setNotes(req.notes());

    return toDto(pantryItemRepository.save(item), LocalDate.now());
  }

  public void adjustQuantity(Long userId, Long itemId, double deltaG) {
    pantryItemRepository
        .findByIdAndUserId(itemId, userId)
        .ifPresent(
            item -> {
              double newQty = item.getQuantityG() + deltaG;
              if (newQty <= 0) {
                pantryItemRepository.delete(item);
              } else {
                item.setQuantityG(newQty);
                pantryItemRepository.save(item);
              }
            });
  }

  public void deletePantryItem(Long userId, Long itemId) {
    pantryItemRepository.findByIdAndUserId(itemId, userId).ifPresent(pantryItemRepository::delete);
  }

  public void markConsumed(Long userId, Long itemId) {
    deletePantryItem(userId, itemId);
  }

  @Transactional(readOnly = true)
  public List<CookBeforeExpiresDto> getCookBeforeItExpires(Long userId) {
    LocalDate today = LocalDate.now();
    List<PantryItemEntity> pantry = pantryItemRepository.findByUserId(userId);
    if (pantry.isEmpty()) {
      return List.of();
    }

    // Identify items expiring within 4 days or already expired
    List<PantryItemEntity> expiringItems =
        pantry.stream()
            .filter(
                p ->
                    p.getExpirationDate() != null
                        && ChronoUnit.DAYS.between(today, p.getExpirationDate()) <= 4)
            .sorted(Comparator.comparing(PantryItemEntity::getExpirationDate))
            .collect(Collectors.toList());

    if (expiringItems.isEmpty()) {
      // Pick top 3 closest to expiry
      expiringItems =
          pantry.stream()
              .filter(p -> p.getExpirationDate() != null)
              .sorted(Comparator.comparing(PantryItemEntity::getExpirationDate))
              .limit(3)
              .collect(Collectors.toList());
    }

    Set<String> expiringFoodIds =
        expiringItems.stream().map(p -> p.getFood().getId()).collect(Collectors.toSet());
    Set<String> allPantryFoodIds =
        pantry.stream().map(p -> p.getFood().getId()).collect(Collectors.toSet());

    List<RecipeEntity> allRecipes = recipeRepository.findAll();
    List<CookBeforeExpiresDto> results = new ArrayList<>();

    for (RecipeEntity recipe : allRecipes) {
      List<String> recipeExpiringMatches = new ArrayList<>();
      int missingCount = 0;

      for (RecipeIngredientEntity ing : recipe.getIngredients()) {
        String foodId = ing.getFood().getId();
        if (expiringFoodIds.contains(foodId)) {
          recipeExpiringMatches.add(ing.getFood().getNameEn());
        }
        if (!allPantryFoodIds.contains(foodId) && !ing.isOptional()) {
          missingCount++;
        }
      }

      if (!recipeExpiringMatches.isEmpty()) {
        String matchLabel =
            missingCount == 0
                ? "Ready Now"
                : (missingCount == 1
                    ? "Missing 1 Ingredient"
                    : "Missing " + missingCount + " Ingredients");
        results.add(
            new CookBeforeExpiresDto(
                recipe.getId(),
                recipe.getTitleEn(),
                recipe.getMealSlot(),
                recipe.getCookMinutes(),
                recipeExpiringMatches,
                missingCount,
                matchLabel));
      }
    }

    // Sort by: lowest missing first, then highest expiring ingredients used
    results.sort(
        Comparator.comparingInt(CookBeforeExpiresDto::missingCount)
            .thenComparing((CookBeforeExpiresDto c) -> -c.expiringIngredientsUsed().size()));

    return results.stream().limit(6).collect(Collectors.toList());
  }

  public void deductIngredients(Long userId, String recipeId, int servings) {
    RecipeEntity recipe =
        recipeRepository
            .findById(recipeId)
            .orElseThrow(() -> new IllegalArgumentException("Recipe not found: " + recipeId));

    double scale = (double) servings / Math.max(1, recipe.getBaseServings());

    for (RecipeIngredientEntity ing : recipe.getIngredients()) {
      String foodId = ing.getFood().getId();
      double neededG = ing.getAmountG() * scale;

      Optional<PantryItemEntity> pantryOpt =
          pantryItemRepository.findByUserIdAndFoodId(userId, foodId);
      if (pantryOpt.isPresent()) {
        PantryItemEntity item = pantryOpt.get();
        double remaining = item.getQuantityG() - neededG;
        if (remaining <= 0) {
          pantryItemRepository.delete(item);
        } else {
          item.setQuantityG(remaining);
          pantryItemRepository.save(item);
        }
      }
    }
  }

  private PantryItemDto toDto(PantryItemEntity item, LocalDate today) {
    long days = 999;
    String urgency = "info";
    if (item.getExpirationDate() != null) {
      days = ChronoUnit.DAYS.between(today, item.getExpirationDate());
      if (days < 0) {
        urgency = "danger"; // expired
      } else if (days <= 1) {
        urgency = "danger"; // expires today or tomorrow
      } else if (days <= 3) {
        urgency = "warning"; // expiring soon
      } else {
        urgency = "success"; // fresh
      }
    }

    return new PantryItemDto(
        item.getId(),
        item.getFood().getId(),
        item.getFood().getNameEn(),
        item.getFood().getNameHi(),
        item.getQuantityG(),
        item.getUnit() != null ? item.getUnit() : "g",
        item.getStorageLocation() != null ? item.getStorageLocation() : "CUPBOARD",
        item.getExpirationDate(),
        item.getPurchaseDate(),
        days,
        urgency,
        item.getNotes() != null ? item.getNotes() : "");
  }
}
