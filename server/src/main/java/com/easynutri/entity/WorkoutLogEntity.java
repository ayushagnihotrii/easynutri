package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

/** Logged physical workout or exercise session. */
@Entity
@Table(name = "workout_logs")
public class WorkoutLogEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @Column(name = "record_date", nullable = false)
  private LocalDate recordDate;

  @Column(name = "activity_type", nullable = false, length = 50)
  private String activityType;

  @Column(name = "intensity", nullable = false, length = 30)
  private String intensity = "MODERATE";

  @Column(name = "duration_minutes", nullable = false)
  private int durationMinutes;

  @Column(name = "distance_km")
  private Double distanceKm;

  @Column(name = "calories_burned", nullable = false)
  private double caloriesBurned;

  @Column(name = "met_value", nullable = false)
  private double metValue;

  @Column(columnDefinition = "TEXT")
  private String notes;

  @Column(name = "source_type", nullable = false, length = 50)
  private String sourceType = "MANUAL";

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  public WorkoutLogEntity() {}

  public Long getId() {
    return id;
  }

  public UserEntity getUser() {
    return user;
  }

  public void setUser(UserEntity user) {
    this.user = user;
  }

  public LocalDate getRecordDate() {
    return recordDate;
  }

  public void setRecordDate(LocalDate recordDate) {
    this.recordDate = recordDate;
  }

  public String getActivityType() {
    return activityType;
  }

  public void setActivityType(String activityType) {
    this.activityType = activityType;
  }

  public String getIntensity() {
    return intensity;
  }

  public void setIntensity(String intensity) {
    this.intensity = intensity;
  }

  public int getDurationMinutes() {
    return durationMinutes;
  }

  public void setDurationMinutes(int durationMinutes) {
    this.durationMinutes = durationMinutes;
  }

  public Double getDistanceKm() {
    return distanceKm;
  }

  public void setDistanceKm(Double distanceKm) {
    this.distanceKm = distanceKm;
  }

  public double getCaloriesBurned() {
    return caloriesBurned;
  }

  public void setCaloriesBurned(double caloriesBurned) {
    this.caloriesBurned = caloriesBurned;
  }

  public double getMetValue() {
    return metValue;
  }

  public void setMetValue(double metValue) {
    this.metValue = metValue;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public String getSourceType() {
    return sourceType;
  }

  public void setSourceType(String sourceType) {
    this.sourceType = sourceType;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
