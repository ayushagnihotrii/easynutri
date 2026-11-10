package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

/** Log of water / hydration intake portion. */
@Entity
@Table(name = "water_logs")
public class WaterLogEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @Column(name = "record_date", nullable = false)
  private LocalDate recordDate;

  @Column(name = "amount_ml", nullable = false)
  private int amountMl;

  @Column(length = 255)
  private String notes;

  @Column(name = "logged_at", nullable = false)
  private Instant loggedAt = Instant.now();

  public WaterLogEntity() {}

  public WaterLogEntity(UserEntity user, LocalDate recordDate, int amountMl, String notes) {
    this.user = user;
    this.recordDate = recordDate;
    this.amountMl = amountMl;
    this.notes = notes;
    this.loggedAt = Instant.now();
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

  public int getAmountMl() {
    return amountMl;
  }

  public void setAmountMl(int amountMl) {
    this.amountMl = amountMl;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public Instant getLoggedAt() {
    return loggedAt;
  }

  public void setLoggedAt(Instant loggedAt) {
    this.loggedAt = loggedAt;
  }
}
