package com.easynutri.repository;

import com.easynutri.entity.RecipeCollectionEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeCollectionRepository extends JpaRepository<RecipeCollectionEntity, Long> {

  List<RecipeCollectionEntity> findByUserIdOrderByDisplayOrderAsc(Long userId);

  Optional<RecipeCollectionEntity> findByIdAndUserId(Long id, Long userId);

  Optional<RecipeCollectionEntity> findByUserIdAndName(Long userId, String name);

  boolean existsByUserIdAndName(Long userId, String name);
}
