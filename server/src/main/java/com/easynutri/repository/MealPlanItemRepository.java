package com.easynutri.repository;

import com.easynutri.entity.MealPlanItemEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MealPlanItemRepository extends JpaRepository<MealPlanItemEntity, Long> {

  List<MealPlanItemEntity> findByUserIdAndPlannedDateOrderByMealSlotAsc(
      Long userId, LocalDate plannedDate);

  List<MealPlanItemEntity> findByUserIdAndPlannedDateBetweenOrderByPlannedDateAscMealSlotAsc(
      Long userId, LocalDate startDate, LocalDate endDate);
}
