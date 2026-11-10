package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;

/** Persistent record of a creator follow relationship. */
@Entity
@Table(
    name = "community_follows",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"follower_id", "following_id"})})
public class CommunityFollowEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "follower_id", nullable = false)
  private UserEntity follower;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "following_id", nullable = false)
  private UserEntity following;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  public CommunityFollowEntity() {}

  public CommunityFollowEntity(UserEntity follower, UserEntity following) {
    this.follower = follower;
    this.following = following;
    this.createdAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public UserEntity getFollower() {
    return follower;
  }

  public UserEntity getFollowing() {
    return following;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
