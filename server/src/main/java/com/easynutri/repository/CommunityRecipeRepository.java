package com.easynutri.repository;

import com.easynutri.entity.CommunityRecipeEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityRecipeRepository extends JpaRepository<CommunityRecipeEntity, Long> {

  List<CommunityRecipeEntity> findByStatusOrderByCreatedAtDesc(String status);

  List<CommunityRecipeEntity> findByStatusOrderByLikeCountDescCreatedAtDesc(String status);

  List<CommunityRecipeEntity> findByAuthorIdAndStatusOrderByCreatedAtDesc(
      Long authorId, String status);

  List<CommunityRecipeEntity> findByAuthorIdOrderByCreatedAtDesc(Long authorId);

  @Query(
      "SELECT r FROM CommunityRecipeEntity r WHERE r.status = :status AND "
          + "(LOWER(r.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(r.description) LIKE LOWER(CONCAT('%', :query, '%')))")
  List<CommunityRecipeEntity> searchApprovedRecipes(
      @Param("status") String status, @Param("query") String query);

  @Query(
      "SELECT r FROM CommunityRecipeEntity r WHERE r.status = 'APPROVED' AND r.author.id IN "
          + "(SELECT f.following.id FROM CommunityFollowEntity f WHERE f.follower.id = :followerId) "
          + "ORDER BY r.createdAt DESC")
  List<CommunityRecipeEntity> findFollowingFeed(@Param("followerId") Long followerId);

  long countByAuthorIdAndStatus(Long authorId, String status);
}
