package com.easynutri.repository;

import com.easynutri.entity.FoodDiaryEntryEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodDiaryEntryRepository extends JpaRepository<FoodDiaryEntryEntity, Long> {

  List<FoodDiaryEntryEntity> findByUserId(Long userId);

  List<FoodDiaryEntryEntity> findByUserIdAndDiaryDate(Long userId, LocalDate diaryDate);

  Optional<FoodDiaryEntryEntity> findByUserIdAndDiaryDateAndMealSlot(
      Long userId, LocalDate diaryDate, String mealSlot);

  List<FoodDiaryEntryEntity> findByUserIdAndDiaryDateBetweenOrderByDiaryDateAsc(
      Long userId, LocalDate start, LocalDate end);

  @Query(
      "SELECT DISTINCT e.diaryDate FROM FoodDiaryEntryEntity e WHERE e.user.id = :userId ORDER BY e.diaryDate DESC")
  List<LocalDate> findDistinctLoggedDates(@Param("userId") Long userId);
}
