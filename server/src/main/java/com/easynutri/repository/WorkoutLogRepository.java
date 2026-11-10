package com.easynutri.repository;

import com.easynutri.entity.WorkoutLogEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkoutLogRepository extends JpaRepository<WorkoutLogEntity, Long> {

  List<WorkoutLogEntity> findByUserIdAndRecordDate(Long userId, LocalDate recordDate);

  List<WorkoutLogEntity> findByUserIdAndRecordDateBetweenOrderByRecordDateAsc(
      Long userId, LocalDate start, LocalDate end);
}
