package com.easynutri.repository;

import com.easynutri.entity.UserNotificationEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotificationEntity, Long> {

  List<UserNotificationEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

  List<UserNotificationEntity> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);

  long countByUserIdAndReadFalse(Long userId);

  Optional<UserNotificationEntity> findByIdAndUserId(Long id, Long userId);

  void deleteByUserId(Long userId);
}
