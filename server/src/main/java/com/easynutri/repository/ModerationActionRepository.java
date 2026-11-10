package com.easynutri.repository;

import com.easynutri.entity.ModerationActionEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModerationActionRepository extends JpaRepository<ModerationActionEntity, Long> {

  List<ModerationActionEntity> findAllByOrderByCreatedAtDesc();

  List<ModerationActionEntity> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
      String targetType, Long targetId);
}
