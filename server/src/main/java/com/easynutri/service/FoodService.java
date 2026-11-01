package com.easynutri.service;

import com.easynutri.dto.FoodDto;
import com.easynutri.entity.FoodEntity;
import com.easynutri.repository.FoodRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FoodService {

  private final FoodRepository foodRepository;

  public FoodService(FoodRepository foodRepository) {
    this.foodRepository = foodRepository;
  }

  @Transactional(readOnly = true)
  public List<FoodDto> findAll() {
    return foodRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<FoodDto> findByCategory(String category) {
    if (category == null || category.isBlank() || "ALL".equalsIgnoreCase(category)) {
      return findAll();
    }
    return foodRepository.findByCategoryIgnoreCase(category).stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<FoodDto> searchFoods(String query) {
    if (query == null || query.isBlank()) {
      return findAll();
    }
    return foodRepository.searchFoods(query.trim()).stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public Optional<FoodDto> findById(String id) {
    return foodRepository.findById(id).map(this::toDto);
  }

  public FoodDto toDto(FoodEntity entity) {
    List<String> allergens =
        entity.getAllergenTags() != null && !entity.getAllergenTags().isBlank()
            ? Arrays.stream(entity.getAllergenTags().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList())
            : List.of();

    List<String> dietFlags =
        entity.getDietFlags() != null && !entity.getDietFlags().isBlank()
            ? Arrays.stream(entity.getDietFlags().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList())
            : List.of();

    return new FoodDto(
        entity.getId(),
        entity.getNameEn(),
        entity.getNameHi(),
        entity.getCategory(),
        entity.getEnergyKcal(),
        entity.getProteinG(),
        entity.getCarbohydrateG(),
        entity.getSugarG(),
        entity.getFatG(),
        entity.getSaturatedFatG(),
        entity.getFibreG(),
        entity.getSodiumMg(),
        entity.getPotassiumMg(),
        entity.getCalciumMg(),
        entity.getIronMg(),
        allergens,
        dietFlags,
        entity.getSource(),
        entity.isVerified());
  }
}
