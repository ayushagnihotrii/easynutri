package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;

/** Persistent audit record of admin moderation actions. */
@Entity
@Table(name = "moderation_actions")
public class ModerationActionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "moderator_id", nullable = false)
  private UserEntity moderator;

  @Column(name = "target_type", nullable = false, length = 30)
  private String targetType; // RECIPE, COMMENT, USER

  @Column(name = "target_id", nullable = false)
  private Long targetId;

  @Column(name = "action_taken", nullable = false, length = 50)
  private String actionTaken; // APPROVE, REJECT, HIDE, RESTORE, REMOVE, SUSPEND_USER

  @Column(name = "moderator_notes", columnDefinition = "TEXT")
  private String moderatorNotes;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  public ModerationActionEntity() {}

  public ModerationActionEntity(
      UserEntity moderator,
      String targetType,
      Long targetId,
      String actionTaken,
      String moderatorNotes) {
    this.moderator = moderator;
    this.targetType = targetType;
    this.targetId = targetId;
    this.actionTaken = actionTaken;
    this.moderatorNotes = moderatorNotes;
    this.createdAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public UserEntity getModerator() {
    return moderator;
  }

  public String getTargetType() {
    return targetType;
  }

  public Long getTargetId() {
    return targetId;
  }

  public String getActionTaken() {
    return actionTaken;
  }

  public String getModeratorNotes() {
    return moderatorNotes;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
