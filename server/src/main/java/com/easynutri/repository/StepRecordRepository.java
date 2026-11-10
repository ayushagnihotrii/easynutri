package com.easynutri.repository;

import com.easynutri.entity.StepRecordEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StepRecordRepository extends JpaRepository<StepRecordEntity, Long> {

  Optional<StepRecordEntity> findByUserIdAndRecordDate(Long userId, LocalDate recordDate);

  List<StepRecordEntity> findByUserIdAndRecordDateBetweenOrderByRecordDateAsc(
      Long userId, LocalDate start, LocalDate end);
}
