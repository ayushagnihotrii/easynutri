package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

/** Daily step record tracking user steps against daily step goals. */
@Entity
@Table(
    name = "step_records",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_step_user_date",
          columnNames = {"user_id", "record_date"})
    })
public class StepRecordEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @Column(name = "record_date", nullable = false)
  private LocalDate recordDate;

  @Column(name = "step_count", nullable = false)
  private int stepCount = 0;

  @Column(name = "goal_steps", nullable = false)
  private int goalSteps = 10000;

  @Column(name = "source_type", nullable = false, length = 50)
  private String sourceType = "MANUAL";

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  public StepRecordEntity() {}

  public StepRecordEntity(
      UserEntity user, LocalDate recordDate, int stepCount, int goalSteps, String sourceType) {
    this.user = user;
    this.recordDate = recordDate;
    this.stepCount = stepCount;
    this.goalSteps = goalSteps;
    this.sourceType = sourceType != null ? sourceType : "MANUAL";
    this.createdAt = Instant.now();
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

  public LocalDate getRecordDate() {
    return recordDate;
  }

  public void setRecordDate(LocalDate recordDate) {
    this.recordDate = recordDate;
  }

  public int getStepCount() {
    return stepCount;
  }

  public void setStepCount(int stepCount) {
    this.stepCount = stepCount;
    this.updatedAt = Instant.now();
  }

  public int getGoalSteps() {
    return goalSteps;
  }

  public void setGoalSteps(int goalSteps) {
    this.goalSteps = goalSteps;
    this.updatedAt = Instant.now();
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

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
