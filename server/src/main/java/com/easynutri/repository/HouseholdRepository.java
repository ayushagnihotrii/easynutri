package com.easynutri.repository;

import com.easynutri.entity.HouseholdEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HouseholdRepository extends JpaRepository<HouseholdEntity, Long> {

  List<HouseholdEntity> findByOwnerId(Long ownerId);

  Optional<HouseholdEntity> findFirstByOwnerId(Long ownerId);
}
