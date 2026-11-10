package com.easynutri.repository;

import com.easynutri.entity.ChallengeParticipationEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeParticipationRepository
    extends JpaRepository<ChallengeParticipationEntity, Long> {

  List<ChallengeParticipationEntity> findByUserIdOrderByJoinedAtDesc(Long userId);

  Optional<ChallengeParticipationEntity> findByUserIdAndChallengeId(
      Long userId, String challengeId);

  boolean existsByUserIdAndChallengeId(Long userId, String challengeId);

  long countByChallengeId(String challengeId);
}
