package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

/** Persistent record of user habit streaks calculated from database logs. */
@Entity
@Table(
    name = "user_streaks",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "streak_type"})})
public class UserStreakEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @Column(name = "streak_type", nullable = false, length = 50)
  private String streakType; // DIARY_LOGGING, HYDRATION, STEP_GOAL, COOKING

  @Column(name = "current_streak_days", nullable = false)
  private int currentStreakDays = 0;

  @Column(name = "longest_streak_days", nullable = false)
  private int longestStreakDays = 0;

  @Column(name = "last_activity_date", nullable = false)
  private LocalDate lastActivityDate;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  public UserStreakEntity() {}

  public UserStreakEntity(
      UserEntity user,
      String streakType,
      int currentStreakDays,
      int longestStreakDays,
      LocalDate lastActivityDate) {
    this.user = user;
    this.streakType = streakType;
    this.currentStreakDays = currentStreakDays;
    this.longestStreakDays = longestStreakDays;
    this.lastActivityDate = lastActivityDate;
    this.updatedAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public UserEntity getUser() {
    return user;
  }

  public String getStreakType() {
    return streakType;
  }

  public int getCurrentStreakDays() {
    return currentStreakDays;
  }

  public void setCurrentStreakDays(int currentStreakDays) {
    this.currentStreakDays = currentStreakDays;
  }

  public int getLongestStreakDays() {
    return longestStreakDays;
  }

  public void setLongestStreakDays(int longestStreakDays) {
    this.longestStreakDays = longestStreakDays;
  }

  public LocalDate getLastActivityDate() {
    return lastActivityDate;
  }

  public void setLastActivityDate(LocalDate lastActivityDate) {
    this.lastActivityDate = lastActivityDate;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
