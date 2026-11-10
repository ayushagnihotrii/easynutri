package com.easynutri.repository;

import com.easynutri.entity.ChallengeEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeRepository extends JpaRepository<ChallengeEntity, String> {

  List<ChallengeEntity> findByActiveTrue();

  List<ChallengeEntity> findByCategoryAndActiveTrue(String category);
}
