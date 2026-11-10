package com.easynutri.repository;

import com.easynutri.entity.UserBadgeEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadgeEntity, Long> {

  List<UserBadgeEntity> findByUserIdOrderByAwardedAtDesc(Long userId);

  boolean existsByUserIdAndBadgeId(Long userId, String badgeId);

  long countByUserId(Long userId);
}
