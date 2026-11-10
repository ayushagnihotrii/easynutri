package com.easynutri.repository;

import com.easynutri.entity.HouseholdMemberEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HouseholdMemberRepository extends JpaRepository<HouseholdMemberEntity, Long> {

  List<HouseholdMemberEntity> findByHouseholdId(Long householdId);

  List<HouseholdMemberEntity> findByUserId(Long userId);

  Optional<HouseholdMemberEntity> findByHouseholdIdAndUserId(Long householdId, Long userId);

  boolean existsByHouseholdIdAndUserId(Long householdId, Long userId);

  List<HouseholdMemberEntity> findByManagedById(Long managedById);
}
