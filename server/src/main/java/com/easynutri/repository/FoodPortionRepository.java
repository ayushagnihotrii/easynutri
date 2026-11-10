package com.easynutri.repository;

import com.easynutri.entity.FoodPortionEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodPortionRepository extends JpaRepository<FoodPortionEntity, Long> {

  List<FoodPortionEntity> findByFoodId(String foodId);
}
