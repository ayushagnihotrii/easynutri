package com.easynutri.entity;

import jakarta.persistence.*;

/** Persistent definition of a community wellness challenge. */
@Entity
@Table(name = "challenges")
public class ChallengeEntity {

  @Id
  @Column(length = 50)
  private String id;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false, length = 50)
  private String icon;

  @Column(nullable = false, length = 50)
  private String category; // NUTRITION, COOKING, PANTRY, HABIT

  @Column(name = "target_count", nullable = false)
  private int targetCount = 7;

  @Column(name = "duration_days", nullable = false)
  private int durationDays = 7;

  @Column(nullable = false)
  private boolean active = true;

  public ChallengeEntity() {}

  public ChallengeEntity(
      String id,
      String title,
      String description,
      String icon,
      String category,
      int targetCount,
      int durationDays,
      boolean active) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.icon = icon;
    this.category = category;
    this.targetCount = targetCount;
    this.durationDays = durationDays;
    this.active = active;
  }

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public String getIcon() {
    return icon;
  }

  public String getCategory() {
    return category;
  }

  public int getTargetCount() {
    return targetCount;
  }

  public int getDurationDays() {
    return durationDays;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }
}
