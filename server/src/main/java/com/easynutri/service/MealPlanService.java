package com.easynutri.service;

import com.easynutri.dto.AddMealPlanRequest;
import com.easynutri.dto.MealPlanItemDto;
import com.easynutri.entity.MealPlanItemEntity;
import com.easynutri.entity.RecipeEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.MealPlanItemRepository;
import com.easynutri.repository.RecipeRepository;
import com.easynutri.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MealPlanService {

  private final MealPlanItemRepository mealPlanItemRepository;
  private final UserRepository userRepository;
  private final RecipeRepository recipeRepository;

  public MealPlanService(
      MealPlanItemRepository mealPlanItemRepository,
      UserRepository userRepository,
      RecipeRepository recipeRepository) {
    this.mealPlanItemRepository = mealPlanItemRepository;
    this.userRepository = userRepository;
    this.recipeRepository = recipeRepository;
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

  public void removeMealPlanItem(Long userId, Long itemId) {
    mealPlanItemRepository
        .findById(itemId)
        .filter(item -> item.getUser().getId().equals(userId))
        .ifPresent(mealPlanItemRepository::delete);
  }

  private MealPlanItemDto toDto(MealPlanItemEntity entity) {
    return new MealPlanItemDto(
        entity.getId(),
        entity.getRecipe().getId(),
        entity.getRecipe().getTitleEn(),
        entity.getPlannedDate(),
        entity.getMealSlot(),
        entity.getServings());
  }
}
