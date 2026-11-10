package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;

/** Persistent record of professional credential verification requests and admin decisions. */
@Entity
@Table(name = "professional_verifications")
public class ProfessionalVerificationEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @Column(name = "professional_category", nullable = false, length = 50)
  private String professionalCategory; // REGISTERED_DIETITIAN, NUTRITIONIST, MEDICAL_DOCTOR

  @Column(name = "credentials_summary", nullable = false, columnDefinition = "TEXT")
  private String credentialsSummary;

  @Column(name = "license_number", length = 100)
  private String licenseNumber;

  @Column(name = "document_filename", length = 255)
  private String documentFilename;

  @Column(nullable = false, length = 30)
  private String status = "PENDING"; // PENDING, APPROVED, REJECTED

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reviewed_by")
  private UserEntity reviewedBy;

  @Column(name = "reviewer_notes", columnDefinition = "TEXT")
  private String reviewerNotes;

  @Column(name = "reviewed_at")
  private Instant reviewedAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  public ProfessionalVerificationEntity() {}

  public ProfessionalVerificationEntity(
      UserEntity user,
      String professionalCategory,
      String credentialsSummary,
      String licenseNumber,
      String documentFilename) {
    this.user = user;
    this.professionalCategory = professionalCategory;
    this.credentialsSummary = credentialsSummary;
    this.licenseNumber = licenseNumber;
    this.documentFilename = documentFilename;
    this.status = "PENDING";
    this.createdAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public UserEntity getUser() {
    return user;
  }

  public String getProfessionalCategory() {
    return professionalCategory;
  }

  public String getCredentialsSummary() {
    return credentialsSummary;
  }

  public String getLicenseNumber() {
    return licenseNumber;
  }

  public String getDocumentFilename() {
    return documentFilename;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public UserEntity getReviewedBy() {
    return reviewedBy;
  }

  public void setReviewedBy(UserEntity reviewedBy) {
    this.reviewedBy = reviewedBy;
  }

  public String getReviewerNotes() {
    return reviewerNotes;
  }

  public void setReviewerNotes(String reviewerNotes) {
    this.reviewerNotes = reviewerNotes;
  }

  public Instant getReviewedAt() {
    return reviewedAt;
  }

  public void setReviewedAt(Instant reviewedAt) {
    this.reviewedAt = reviewedAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
