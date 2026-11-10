package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;

/** Persistent record of a user's participation in a community challenge. */
@Entity
@Table(
    name = "challenge_participations",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "challenge_id"})})
public class ChallengeParticipationEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "challenge_id", nullable = false)
  private ChallengeEntity challenge;

  @Column(nullable = false)
  private int progress = 0;

  @Column(nullable = false)
  private boolean completed = false;

  @Column(name = "completed_at")
  private Instant completedAt;

  @Column(name = "joined_at", nullable = false)
  private Instant joinedAt = Instant.now();

  public ChallengeParticipationEntity() {}

  public ChallengeParticipationEntity(UserEntity user, ChallengeEntity challenge) {
    this.user = user;
    this.challenge = challenge;
    this.progress = 0;
    this.completed = false;
    this.joinedAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public UserEntity getUser() {
    return user;
  }

  public ChallengeEntity getChallenge() {
    return challenge;
  }

  public int getProgress() {
    return progress;
  }

  public void setProgress(int progress) {
    this.progress = progress;
  }

  public boolean isCompleted() {
    return completed;
  }

  public void setCompleted(boolean completed) {
    this.completed = completed;
    if (completed && this.completedAt == null) {
      this.completedAt = Instant.now();
    }
  }

  public Instant getCompletedAt() {
    return completedAt;
  }

  public Instant getJoinedAt() {
    return joinedAt;
  }
}
