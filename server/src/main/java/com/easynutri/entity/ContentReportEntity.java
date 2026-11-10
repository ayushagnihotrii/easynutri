package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;

/** Persistent record of user content reports for moderation. */
@Entity
@Table(name = "content_reports")
public class ContentReportEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reporter_id", nullable = false)
  private UserEntity reporter;

  @Column(name = "target_type", nullable = false, length = 30)
  private String targetType; // RECIPE, COMMENT, USER

  @Column(name = "target_id", nullable = false)
  private Long targetId;

  @Column(name = "reason_category", nullable = false, length = 50)
  private String reasonCategory;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "ai_screening_verdict", length = 30)
  private String aiScreeningVerdict;

  @Column(name = "ai_screening_reason", columnDefinition = "TEXT")
  private String aiScreeningReason;

  @Column(nullable = false, length = 30)
  private String status = "PENDING"; // PENDING, RESOLVED_ACTIONED, RESOLVED_DISMISSED

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  public ContentReportEntity() {}

  public ContentReportEntity(
      UserEntity reporter,
      String targetType,
      Long targetId,
      String reasonCategory,
      String description,
      String aiScreeningVerdict,
      String aiScreeningReason) {
    this.reporter = reporter;
    this.targetType = targetType;
    this.targetId = targetId;
    this.reasonCategory = reasonCategory;
    this.description = description;
    this.aiScreeningVerdict = aiScreeningVerdict;
    this.aiScreeningReason = aiScreeningReason;
    this.status = "PENDING";
    this.createdAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public UserEntity getReporter() {
    return reporter;
  }

  public String getTargetType() {
    return targetType;
  }

  public Long getTargetId() {
    return targetId;
  }

  public String getReasonCategory() {
    return reasonCategory;
  }

  public String getDescription() {
    return description;
  }

  public String getAiScreeningVerdict() {
    return aiScreeningVerdict;
  }

  public void setAiScreeningVerdict(String aiScreeningVerdict) {
    this.aiScreeningVerdict = aiScreeningVerdict;
  }

  public String getAiScreeningReason() {
    return aiScreeningReason;
  }

  public void setAiScreeningReason(String aiScreeningReason) {
    this.aiScreeningReason = aiScreeningReason;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
