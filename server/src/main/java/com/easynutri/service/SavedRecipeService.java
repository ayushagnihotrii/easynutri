package com.easynutri.service;

import com.easynutri.dto.SavedRecipeDto;
import com.easynutri.entity.RecipeEntity;
import com.easynutri.entity.SavedRecipeEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.RecipeRepository;
import com.easynutri.repository.SavedRecipeRepository;
import com.easynutri.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SavedRecipeService {

  private final SavedRecipeRepository savedRecipeRepository;
  private final UserRepository userRepository;
  private final RecipeRepository recipeRepository;

  public SavedRecipeService(
      SavedRecipeRepository savedRecipeRepository,
      UserRepository userRepository,
      RecipeRepository recipeRepository) {
    this.savedRecipeRepository = savedRecipeRepository;
    this.userRepository = userRepository;
    this.recipeRepository = recipeRepository;
  }

  @Transactional(readOnly = true)
  public List<SavedRecipeDto> getSavedRecipes(Long userId) {
    return savedRecipeRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  public boolean saveRecipe(Long userId, String recipeId, String notes) {
    Optional<SavedRecipeEntity> existing =
        savedRecipeRepository.findByUserIdAndRecipeId(userId, recipeId);
    if (existing.isPresent()) {
      if (notes != null) {
        existing.get().setNotes(notes);
        savedRecipeRepository.save(existing.get());
      }
      return true;
    }

    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    RecipeEntity recipe =
        recipeRepository
            .findById(recipeId)
            .orElseThrow(() -> new IllegalArgumentException("Recipe not found: " + recipeId));

    SavedRecipeEntity entity = new SavedRecipeEntity(user, recipe, notes);
    savedRecipeRepository.save(entity);
    return true;
  }

  public boolean unsaveRecipe(Long userId, String recipeId) {
    if (savedRecipeRepository.existsByUserIdAndRecipeId(userId, recipeId)) {
      savedRecipeRepository.deleteByUserIdAndRecipeId(userId, recipeId);
      return true;
    }
    return false;
  }

  @Transactional(readOnly = true)
  public boolean isRecipeSaved(Long userId, String recipeId) {
    if (userId == null || recipeId == null) return false;
    return savedRecipeRepository.existsByUserIdAndRecipeId(userId, recipeId);
  }

  public void updateNotes(Long userId, String recipeId, String notes) {
    savedRecipeRepository
        .findByUserIdAndRecipeId(userId, recipeId)
        .ifPresent(
            entity -> {
              entity.setNotes(notes);
              savedRecipeRepository.save(entity);
            });
  }

  @Transactional(readOnly = true)
  public Optional<String> getNotes(Long userId, String recipeId) {
    return savedRecipeRepository
        .findByUserIdAndRecipeId(userId, recipeId)
        .map(SavedRecipeEntity::getNotes);
  }

  private SavedRecipeDto toDto(SavedRecipeEntity entity) {
    RecipeEntity r = entity.getRecipe();
    return new SavedRecipeDto(
        entity.getId(),
        r.getId(),
        r.getTitleEn(),
        r.getTitleHi(),
        r.getMealSlot(),
        r.getCookMinutes(),
        r.getBaseServings(),
        entity.getNotes());
  }
}
