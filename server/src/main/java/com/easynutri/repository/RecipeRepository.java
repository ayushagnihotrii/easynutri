package com.easynutri.repository;

import com.easynutri.entity.RecipeEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeRepository extends JpaRepository<RecipeEntity, String> {

  List<RecipeEntity> findByMealSlotIgnoreCase(String mealSlot);

  @Query(
      "SELECT r FROM RecipeEntity r WHERE LOWER(r.titleEn) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(r.titleHi) LIKE LOWER(CONCAT('%', :query, '%'))")
  List<RecipeEntity> searchRecipes(@Param("query") String query);
}
