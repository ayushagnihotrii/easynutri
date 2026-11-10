package com.easynutri.repository;

import com.easynutri.entity.SyncOperationEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SyncOperationRepository extends JpaRepository<SyncOperationEntity, Long> {

  Optional<SyncOperationEntity> findByIdempotencyKey(String idempotencyKey);

  boolean existsByIdempotencyKey(String idempotencyKey);

  List<SyncOperationEntity> findByUserIdOrderByProcessedAtDesc(Long userId);
}
