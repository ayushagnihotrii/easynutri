package com.easynutri.service;

import com.easynutri.entity.FoodEntity;
import com.easynutri.entity.PantryItemEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.FoodRepository;
import com.easynutri.repository.PantryItemRepository;
import com.easynutri.repository.UserRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PantryService {

  public record PantryItemDto(
      Long id,
      String foodId,
      String nameEn,
      String nameHi,
      double quantityG,
      LocalDate expirationDate,
      long daysUntilExpiry,
      String urgencyLevel) {}

  private final PantryItemRepository pantryItemRepository;
  private final UserRepository userRepository;
  private final FoodRepository foodRepository;

  public PantryService(
      PantryItemRepository pantryItemRepository,
      UserRepository userRepository,
      FoodRepository foodRepository) {
    this.pantryItemRepository = pantryItemRepository;
    this.userRepository = userRepository;
    this.foodRepository = foodRepository;
  }

  @Transactional(readOnly = true)
  public List<PantryItemDto> getUserPantry(String username) {
    LocalDate today = LocalDate.now();
    List<PantryItemEntity> items = pantryItemRepository.findByUserUsername(username);
    List<PantryItemDto> dtos = new ArrayList<>();

    for (PantryItemEntity item : items) {
      long days = 999;
      String urgency = "info";
      if (item.getExpirationDate() != null) {
        days = ChronoUnit.DAYS.between(today, item.getExpirationDate());
        if (days <= 1) {
          urgency = "danger";
        } else if (days <= 3) {
          urgency = "warning";
        }
      }

      dtos.add(
          new PantryItemDto(
              item.getId(),
              item.getFood().getId(),
              item.getFood().getNameEn(),
              item.getFood().getNameHi(),
              item.getQuantityG(),
              item.getExpirationDate(),
              days,
              urgency));
    }
    return dtos;
  }

  @Transactional
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
}
