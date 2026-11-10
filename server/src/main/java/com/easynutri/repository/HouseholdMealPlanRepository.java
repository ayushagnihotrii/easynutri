package com.easynutri.repository;

import com.easynutri.entity.HouseholdMealPlanEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HouseholdMealPlanRepository extends JpaRepository<HouseholdMealPlanEntity, Long> {

  List<HouseholdMealPlanEntity> findByHouseholdIdAndPlanDateOrderByMealSlotAsc(
      Long householdId, LocalDate planDate);

  List<HouseholdMealPlanEntity> findByHouseholdIdAndPlanDateBetweenOrderByPlanDateAscMealSlotAsc(
      Long householdId, LocalDate startDate, LocalDate endDate);
}
