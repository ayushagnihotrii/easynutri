package com.easynutri.repository;

import com.easynutri.entity.WaterLogEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WaterLogRepository extends JpaRepository<WaterLogEntity, Long> {

  List<WaterLogEntity> findByUserIdAndRecordDateOrderByLoggedAtAsc(
      Long userId, LocalDate recordDate);

  List<WaterLogEntity> findByUserIdAndRecordDateBetweenOrderByRecordDateAscLoggedAtAsc(
      Long userId, LocalDate start, LocalDate end);
}
