package com.easynutri.repository;

import com.easynutri.entity.RecipeAdaptationEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeAdaptationRepository extends JpaRepository<RecipeAdaptationEntity, Long> {

  List<RecipeAdaptationEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

  Optional<RecipeAdaptationEntity> findByIdAndUserId(Long id, Long userId);

  void deleteByUserId(Long userId);
}
