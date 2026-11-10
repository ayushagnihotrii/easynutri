package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;

/** Persistent in-app user notifications for expiry alerts, plan suggestions, and warnings. */
@Entity
@Table(name = "user_notifications")
public class UserNotificationEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String message;

  @Column(nullable = false, length = 50)
  private String type = "INFO";

  @Column(name = "read", nullable = false)
  private boolean read = false;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  public UserNotificationEntity() {}

  public UserNotificationEntity(UserEntity user, String title, String message, String type) {
    this.user = user;
    this.title = title;
    this.message = message;
    this.type = type != null ? type : "INFO";
    this.read = false;
    this.createdAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public UserEntity getUser() {
    return user;
  }

  public void setUser(UserEntity user) {
    this.user = user;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public boolean isRead() {
    return read;
  }

  public void setRead(boolean read) {
    this.read = read;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
