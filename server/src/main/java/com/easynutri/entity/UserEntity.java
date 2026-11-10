package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;

/** Persistent database entity for authenticated EasyNutri users. */
@Entity
@Table(name = "users")
public class UserEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false, length = 50)
  private String username;

  @Column(unique = true, nullable = false, length = 100)
  private String email;

  @Column(nullable = false, length = 255)
  private String passwordHash;

  @Column(length = 100)
  private String fullName;

  @Column(nullable = false)
  private String role = "ROLE_USER";

  @Column(nullable = false)
  private boolean enabled = true;

  @Column(length = 500)
  private String bio = "";

  @Column(name = "avatar_url", length = 500)
  private String avatarUrl = "";

  @Column(name = "is_verified_pro", nullable = false)
  private boolean verifiedPro = false;

  @Column(name = "pro_category", length = 50)
  private String proCategory;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  public UserEntity() {}

  public UserEntity(String username, String email, String passwordHash, String fullName) {
    this.username = username;
    this.email = email;
    this.passwordHash = passwordHash;
    this.fullName = fullName;
    this.role = "ROLE_USER";
    this.enabled = true;
    this.bio = "";
    this.avatarUrl = "";
    this.verifiedPro = false;
    this.createdAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getBio() {
    return bio != null ? bio : "";
  }

  public void setBio(String bio) {
    this.bio = bio;
  }

  public String getAvatarUrl() {
    return avatarUrl != null ? avatarUrl : "";
  }

  public void setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
  }

  public boolean isVerifiedPro() {
    return verifiedPro;
  }

  public void setVerifiedPro(boolean verifiedPro) {
    this.verifiedPro = verifiedPro;
  }

  public String getProCategory() {
    return proCategory;
  }

  public void setProCategory(String proCategory) {
    this.proCategory = proCategory;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
