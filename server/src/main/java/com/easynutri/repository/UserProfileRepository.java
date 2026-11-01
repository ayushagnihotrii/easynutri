package com.easynutri.repository;

import com.easynutri.entity.UserProfileEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfileEntity, Long> {
  Optional<UserProfileEntity> findByUserId(Long userId);

  Optional<UserProfileEntity> findByUserUsername(String username);

  boolean existsByUserUsername(String username);
}
