package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;

/** Persistent record of a badge awarded to a user. */
@Entity
@Table(
    name = "user_badges",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "badge_id"})})
public class UserBadgeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "badge_id", nullable = false)
  private BadgeDefinitionEntity badge;

  @Column(name = "awarded_at", nullable = false)
  private Instant awardedAt = Instant.now();

  public UserBadgeEntity() {}

  public UserBadgeEntity(UserEntity user, BadgeDefinitionEntity badge) {
    this.user = user;
    this.badge = badge;
    this.awardedAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public UserEntity getUser() {
    return user;
  }

  public BadgeDefinitionEntity getBadge() {
    return badge;
  }

  public Instant getAwardedAt() {
    return awardedAt;
  }
}
