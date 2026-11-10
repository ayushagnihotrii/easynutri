package com.easynutri.repository;

import com.easynutri.entity.CommunityLikeEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityLikeRepository extends JpaRepository<CommunityLikeEntity, Long> {

  Optional<CommunityLikeEntity> findByUserIdAndCommunityRecipeId(
      Long userId, Long communityRecipeId);

  boolean existsByUserIdAndCommunityRecipeId(Long userId, Long communityRecipeId);

  long countByCommunityRecipeId(Long communityRecipeId);
}
