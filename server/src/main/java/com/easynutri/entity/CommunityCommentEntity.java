package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;

/** Persistent community recipe comment with moderation status. */
@Entity
@Table(name = "community_comments")
public class CommunityCommentEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "community_recipe_id", nullable = false)
  private CommunityRecipeEntity communityRecipe;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id", nullable = false)
  private UserEntity author;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(nullable = false, length = 30)
  private String status = "APPROVED"; // APPROVED, PENDING_REVIEW, REMOVED

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  public CommunityCommentEntity() {}

  public CommunityCommentEntity(
      CommunityRecipeEntity communityRecipe, UserEntity author, String content, String status) {
    this.communityRecipe = communityRecipe;
    this.author = author;
    this.content = content;
    this.status = status != null ? status : "APPROVED";
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public CommunityRecipeEntity getCommunityRecipe() {
    return communityRecipe;
  }

  public UserEntity getAuthor() {
    return author;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
    this.updatedAt = Instant.now();
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
    this.updatedAt = Instant.now();
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
