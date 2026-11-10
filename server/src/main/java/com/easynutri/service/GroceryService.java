package com.easynutri.service;

import com.easynutri.dto.AddGroceryItemRequest;
import com.easynutri.dto.AddMissingGroceryRequest;
import com.easynutri.dto.GroceryItemDto;
import com.easynutri.entity.FoodEntity;
import com.easynutri.entity.GroceryItemEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.FoodRepository;
import com.easynutri.repository.GroceryItemRepository;
import com.easynutri.repository.UserRepository;
import java.util.List;
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

  public GroceryService(
      GroceryItemRepository groceryItemRepository,
      UserRepository userRepository,
      FoodRepository foodRepository) {
    this.groceryItemRepository = groceryItemRepository;
    this.userRepository = userRepository;
    this.foodRepository = foodRepository;
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
            request.category());

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
      // Try to find matching food entity
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
          new GroceryItemEntity(user, food, displayName, 1.0, "serving", category);
      groceryItemRepository.save(entity);
      count++;
    }
    return count;
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
        entity.getFood() != null ? entity.getFood().getId() : null);
  }
}
