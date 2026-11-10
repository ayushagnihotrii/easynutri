package com.easynutri.repository;

import com.easynutri.entity.UserStreakEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStreakRepository extends JpaRepository<UserStreakEntity, Long> {

  Optional<UserStreakEntity> findByUserIdAndStreakType(Long userId, String streakType);

  List<UserStreakEntity> findByUserId(Long userId);
}
