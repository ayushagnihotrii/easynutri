package com.easynutri.repository;

import com.easynutri.entity.HydrationPreferenceEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HydrationPreferenceRepository
    extends JpaRepository<HydrationPreferenceEntity, Long> {

  Optional<HydrationPreferenceEntity> findByUserId(Long userId);
}
