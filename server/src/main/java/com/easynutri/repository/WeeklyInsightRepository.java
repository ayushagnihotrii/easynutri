package com.easynutri.repository;

import com.easynutri.entity.WeeklyInsightEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeeklyInsightRepository extends JpaRepository<WeeklyInsightEntity, Long> {

  List<WeeklyInsightEntity> findByUserIdAndWeekStartDateOrderByCreatedAtDesc(
      Long userId, LocalDate weekStartDate);

  List<WeeklyInsightEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
}
