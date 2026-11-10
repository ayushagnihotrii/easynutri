package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;

/** Persistent record of a user like on a community recipe. */
@Entity
@Table(
    name = "community_likes",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "community_recipe_id"})})
public class CommunityLikeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "community_recipe_id", nullable = false)
  private CommunityRecipeEntity communityRecipe;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  public CommunityLikeEntity() {}

  public CommunityLikeEntity(UserEntity user, CommunityRecipeEntity communityRecipe) {
    this.user = user;
    this.communityRecipe = communityRecipe;
    this.createdAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public UserEntity getUser() {
    return user;
  }

  public CommunityRecipeEntity getCommunityRecipe() {
    return communityRecipe;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
