package com.easynutri.repository;

import com.easynutri.entity.AiToolExecutionEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiToolExecutionRepository extends JpaRepository<AiToolExecutionEntity, Long> {

  List<AiToolExecutionEntity> findByMessageId(Long messageId);
}
