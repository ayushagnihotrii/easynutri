package com.easynutri.repository;

import com.easynutri.entity.SavedRecipeEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavedRecipeRepository extends JpaRepository<SavedRecipeEntity, Long> {

  List<SavedRecipeEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

  Optional<SavedRecipeEntity> findByUserIdAndRecipeId(Long userId, String recipeId);

  boolean existsByUserIdAndRecipeId(Long userId, String recipeId);

  void deleteByUserIdAndRecipeId(Long userId, String recipeId);
}
