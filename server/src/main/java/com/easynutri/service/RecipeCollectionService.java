package com.easynutri.service;

import com.easynutri.dto.RecipeCollectionDto;
import com.easynutri.entity.RecipeCollectionEntity;
import com.easynutri.entity.RecipeEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.RecipeCollectionRepository;
import com.easynutri.repository.RecipeRepository;
import com.easynutri.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RecipeCollectionService {

  private final RecipeCollectionRepository collectionRepository;
  private final UserRepository userRepository;
  private final RecipeRepository recipeRepository;

  public RecipeCollectionService(
      RecipeCollectionRepository collectionRepository,
      UserRepository userRepository,
      RecipeRepository recipeRepository) {
    this.collectionRepository = collectionRepository;
    this.userRepository = userRepository;
    this.recipeRepository = recipeRepository;
  }

  public void ensureDefaultCollections(Long userId) {
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    if (!collectionRepository.existsByUserIdAndName(userId, "Favorites")) {
      collectionRepository.save(new RecipeCollectionEntity(user, "Favorites", 0));
    }
  }

  @Transactional(readOnly = true)
  public List<RecipeCollectionDto> getUserCollections(Long userId) {
    ensureDefaultCollections(userId);
    return collectionRepository.findByUserIdOrderByDisplayOrderAsc(userId).stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  public RecipeCollectionDto createCollection(Long userId, String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Collection name cannot be empty");
    }
    String trimmed = name.trim();
    if (collectionRepository.existsByUserIdAndName(userId, trimmed)) {
      return toDto(collectionRepository.findByUserIdAndName(userId, trimmed).orElseThrow());
    }
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    int order = collectionRepository.findByUserIdOrderByDisplayOrderAsc(userId).size();
    RecipeCollectionEntity entity = new RecipeCollectionEntity(user, trimmed, order);
    return toDto(collectionRepository.save(entity));
  }

  public void deleteCollection(Long userId, Long collectionId) {
    collectionRepository
        .findByIdAndUserId(collectionId, userId)
        .ifPresent(
            col -> {
              if (!"Favorites".equalsIgnoreCase(col.getName())) {
                collectionRepository.delete(col);
              }
            });
  }

  public void addRecipeToCollection(Long userId, Long collectionId, String recipeId) {
    RecipeCollectionEntity col =
        collectionRepository
            .findByIdAndUserId(collectionId, userId)
            .orElseThrow(
                () -> new IllegalArgumentException("Collection not found: " + collectionId));
    RecipeEntity recipe =
        recipeRepository
            .findById(recipeId)
            .orElseThrow(() -> new IllegalArgumentException("Recipe not found: " + recipeId));

    col.addRecipe(recipe);
    collectionRepository.save(col);
  }

  public void removeRecipeFromCollection(Long userId, Long collectionId, String recipeId) {
    collectionRepository
        .findByIdAndUserId(collectionId, userId)
        .ifPresent(
            col -> {
              col.getRecipes().removeIf(r -> r.getId().equals(recipeId));
              collectionRepository.save(col);
            });
  }

  private RecipeCollectionDto toDto(RecipeCollectionEntity entity) {
    List<String> recipeIds =
        entity.getRecipes().stream().map(RecipeEntity::getId).collect(Collectors.toList());
    return new RecipeCollectionDto(
        entity.getId(),
        entity.getName(),
        entity.getDisplayOrder(),
        entity.getRecipes().size(),
        recipeIds);
  }
}
