package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

/** Stored personalized weekly check-in insight and actionable recommendation. */
@Entity
@Table(name = "weekly_insights")
public class WeeklyInsightEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @Column(name = "week_start_date", nullable = false)
  private LocalDate weekStartDate;

  @Column(nullable = false, length = 50)
  private String category; // PROTEIN, FIBER, HYDRATION, ACTIVITY, PANTRY, CONSISTENCY

  @Column(name = "observation_text", nullable = false, columnDefinition = "TEXT")
  private String observationText;

  @Column(name = "suggested_action_type", length = 50)
  private String
      suggestedActionType; // RECIPE_SEARCH, ADAPT_RECIPE, GROCERY_SYNC, MEAL_PLAN, ASSISTANT_CHAT

  @Column(name = "suggested_action_payload", length = 255)
  private String suggestedActionPayload;

  @Column(name = "is_positive", nullable = false)
  private boolean positive = true;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  public WeeklyInsightEntity() {}

  public WeeklyInsightEntity(
      UserEntity user,
      LocalDate weekStartDate,
      String category,
      String observationText,
      String suggestedActionType,
      String suggestedActionPayload,
      boolean positive) {
    this.user = user;
    this.weekStartDate = weekStartDate;
    this.category = category;
    this.observationText = observationText;
    this.suggestedActionType = suggestedActionType;
    this.suggestedActionPayload = suggestedActionPayload;
    this.positive = positive;
    this.createdAt = Instant.now();
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

  public LocalDate getWeekStartDate() {
    return weekStartDate;
  }

  public void setWeekStartDate(LocalDate weekStartDate) {
    this.weekStartDate = weekStartDate;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getObservationText() {
    return observationText;
  }

  public void setObservationText(String observationText) {
    this.observationText = observationText;
  }

  public String getSuggestedActionType() {
    return suggestedActionType;
  }

  public void setSuggestedActionType(String suggestedActionType) {
    this.suggestedActionType = suggestedActionType;
  }

  public String getSuggestedActionPayload() {
    return suggestedActionPayload;
  }

  public void setSuggestedActionPayload(String suggestedActionPayload) {
    this.suggestedActionPayload = suggestedActionPayload;
  }

  public boolean isPositive() {
    return positive;
  }

  public void setPositive(boolean positive) {
    this.positive = positive;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
