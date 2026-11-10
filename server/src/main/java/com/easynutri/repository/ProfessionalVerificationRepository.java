package com.easynutri.repository;

import com.easynutri.entity.ProfessionalVerificationEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfessionalVerificationRepository
    extends JpaRepository<ProfessionalVerificationEntity, Long> {

  List<ProfessionalVerificationEntity> findByStatusOrderByCreatedAtDesc(String status);

  Optional<ProfessionalVerificationEntity> findTopByUserIdOrderByCreatedAtDesc(Long userId);

  long countByStatus(String status);
}
