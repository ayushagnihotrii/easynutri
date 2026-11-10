package com.easynutri.repository;

import com.easynutri.entity.CommunityCommentEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityCommentRepository extends JpaRepository<CommunityCommentEntity, Long> {

  List<CommunityCommentEntity> findByCommunityRecipeIdAndStatusOrderByCreatedAtAsc(
      Long communityRecipeId, String status);

  long countByCommunityRecipeIdAndStatus(Long communityRecipeId, String status);

  List<CommunityCommentEntity> findByAuthorId(Long authorId);

  void deleteByAuthorId(Long authorId);

  void deleteByCommunityRecipeId(Long communityRecipeId);
}
