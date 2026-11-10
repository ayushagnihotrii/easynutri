package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;

/** User hydration goals, clinical restrictions, and reminder preferences. */
@Entity
@Table(name = "hydration_preferences")
public class HydrationPreferenceEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private UserEntity user;

  @Column(name = "daily_target_ml", nullable = false)
  private int dailyTargetMl = 2500;

  @Column(name = "clinical_restriction", nullable = false)
  private boolean clinicalRestriction = false;

  @Column(name = "clinical_notes", length = 255)
  private String clinicalNotes;

  @Column(name = "reminder_enabled", nullable = false)
  private boolean reminderEnabled = true;

  @Column(name = "reminder_interval_hours", nullable = false)
  private int reminderIntervalHours = 2;

  @Column(name = "start_time", nullable = false, length = 10)
  private String startTime = "08:00";

  @Column(name = "end_time", nullable = false, length = 10)
  private String endTime = "22:00";

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  public HydrationPreferenceEntity() {}

  public HydrationPreferenceEntity(UserEntity user, int dailyTargetMl) {
    this.user = user;
    this.dailyTargetMl = dailyTargetMl;
    this.updatedAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public UserEntity getUser() {
    return user;
  }

  public void setUser(UserEntity user) {
    this.user = user;
  }

  public int getDailyTargetMl() {
    return dailyTargetMl;
  }

  public void setDailyTargetMl(int dailyTargetMl) {
    this.dailyTargetMl = dailyTargetMl;
    this.updatedAt = Instant.now();
  }

  public boolean isClinicalRestriction() {
    return clinicalRestriction;
  }

  public void setClinicalRestriction(boolean clinicalRestriction) {
    this.clinicalRestriction = clinicalRestriction;
    this.updatedAt = Instant.now();
  }

  public String getClinicalNotes() {
    return clinicalNotes;
  }

  public void setClinicalNotes(String clinicalNotes) {
    this.clinicalNotes = clinicalNotes;
  }

  public boolean isReminderEnabled() {
    return reminderEnabled;
  }

  public void setReminderEnabled(boolean reminderEnabled) {
    this.reminderEnabled = reminderEnabled;
    this.updatedAt = Instant.now();
  }

  public int getReminderIntervalHours() {
    return reminderIntervalHours;
  }

  public void setReminderIntervalHours(int reminderIntervalHours) {
    this.reminderIntervalHours = reminderIntervalHours;
    this.updatedAt = Instant.now();
  }

  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public String getEndTime() {
    return endTime;
  }

  public void setEndTime(String endTime) {
    this.endTime = endTime;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
