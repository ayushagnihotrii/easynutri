package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

/** Log of periodic body weight measurements and calculated BMI. */
@Entity
@Table(name = "weight_measurements")
public class WeightMeasurementEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @Column(name = "measurement_date", nullable = false)
  private LocalDate measurementDate;

  @Column(name = "weight_kg", nullable = false)
  private double weightKg;

  @Column(name = "bmi")
  private Double bmi;

  @Column(length = 255)
  private String notes;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  public WeightMeasurementEntity() {}

  public WeightMeasurementEntity(
      UserEntity user, LocalDate measurementDate, double weightKg, Double bmi, String notes) {
    this.user = user;
    this.measurementDate = measurementDate;
    this.weightKg = weightKg;
    this.bmi = bmi;
    this.notes = notes;
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

  public LocalDate getMeasurementDate() {
    return measurementDate;
  }

  public void setMeasurementDate(LocalDate measurementDate) {
    this.measurementDate = measurementDate;
  }

  public double getWeightKg() {
    return weightKg;
  }

  public void setWeightKg(double weightKg) {
    this.weightKg = weightKg;
  }

  public Double getBmi() {
    return bmi;
  }

  public void setBmi(Double bmi) {
    this.bmi = bmi;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
