package com.easynutri.repository;

import com.easynutri.entity.CookingSessionEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CookingSessionRepository extends JpaRepository<CookingSessionEntity, Long> {

  List<CookingSessionEntity> findByUserIdOrderByStartedAtDesc(Long userId);

  Optional<CookingSessionEntity> findByIdAndUserId(Long id, Long userId);

  Optional<CookingSessionEntity> findTopByUserIdAndRecipeIdAndCompletedAtIsNullOrderByStartedAtDesc(
      Long userId, String recipeId);
}
