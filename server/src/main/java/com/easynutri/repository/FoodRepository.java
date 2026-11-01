package com.easynutri.repository;

import com.easynutri.entity.FoodEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodRepository extends JpaRepository<FoodEntity, String> {

  List<FoodEntity> findByCategoryIgnoreCase(String category);

  @Query(
      "SELECT f FROM FoodEntity f WHERE LOWER(f.nameEn) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(f.nameHi) LIKE LOWER(CONCAT('%', :query, '%'))")
  List<FoodEntity> searchFoods(@Param("query") String query);

  long countByVerified(boolean verified);
}
