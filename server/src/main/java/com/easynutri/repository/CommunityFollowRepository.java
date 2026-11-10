package com.easynutri.repository;

import com.easynutri.entity.CommunityFollowEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityFollowRepository extends JpaRepository<CommunityFollowEntity, Long> {

  Optional<CommunityFollowEntity> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

  boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

  List<CommunityFollowEntity> findByFollowerId(Long followerId);

  List<CommunityFollowEntity> findByFollowingId(Long followingId);

  long countByFollowerId(Long followerId);

  long countByFollowingId(Long followingId);

  void deleteByFollowerId(Long followerId);

  void deleteByFollowingId(Long followingId);
}
