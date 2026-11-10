package com.easynutri.repository;

import com.easynutri.entity.ContentReportEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentReportRepository extends JpaRepository<ContentReportEntity, Long> {

  List<ContentReportEntity> findByStatusOrderByCreatedAtDesc(String status);

  boolean existsByReporterIdAndTargetTypeAndTargetId(
      Long reporterId, String targetType, Long targetId);

  long countByStatus(String status);
}
