package com.easynutri.entity;

import jakarta.persistence.*;

/** Persistent definition of an achievable wellness badge. */
@Entity
@Table(name = "badge_definitions")
public class BadgeDefinitionEntity {

  @Id
  @Column(length = 50)
  private String id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false, length = 50)
  private String icon;

  @Column(nullable = false, length = 50)
  private String category; // NUTRITION, HYDRATION, ACTIVITY, COOKING, COMMUNITY

  @Column(name = "unlock_threshold", nullable = false)
  private int unlockThreshold = 1;

  public BadgeDefinitionEntity() {}

  public BadgeDefinitionEntity(
      String id,
      String name,
      String description,
      String icon,
      String category,
      int unlockThreshold) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.icon = icon;
    this.category = category;
    this.unlockThreshold = unlockThreshold;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getIcon() {
    return icon;
  }

  public String getCategory() {
    return category;
  }

  public int getUnlockThreshold() {
    return unlockThreshold;
  }
}
