package com.easynutri.repository;

import com.easynutri.entity.WeightMeasurementEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeightMeasurementRepository extends JpaRepository<WeightMeasurementEntity, Long> {

  Optional<WeightMeasurementEntity> findFirstByUserIdOrderByMeasurementDateDesc(Long userId);

  List<WeightMeasurementEntity> findByUserIdAndMeasurementDateBetweenOrderByMeasurementDateAsc(
      Long userId, LocalDate start, LocalDate end);
}
