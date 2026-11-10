package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Links a user account (or managed dependent profile) to a household with roles and permissions.
 */
@Entity
@Table(
    name = "household_members",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_household_member",
          columnNames = {"household_id", "user_id"})
    })
public class HouseholdMemberEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "household_id", nullable = false)
  private HouseholdEntity household;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @Column(name = "display_name", nullable = false, length = 100)
  private String displayName;

  @Column(nullable = false, length = 50)
  private String relationship = "OTHER"; // SELF, SPOUSE, FATHER, MOTHER, CHILD, SIBLING, OTHER

  @Column(nullable = false, length = 50)
  private String role = "MEMBER"; // HOUSEHOLD_OWNER, HOUSEHOLD_ADMIN, MEMBER, DEPENDENT

  @Column(name = "avatar_color", length = 30)
  private String avatarColor = "#237A57";

  @Column(name = "avatar_initials", length = 10)
  private String avatarInitials = "FM";

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "managed_by_user_id")
  private UserEntity managedBy;

  @Column(name = "can_manage_household", nullable = false)
  private boolean canManageHousehold = false;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  public HouseholdMemberEntity() {}

  public HouseholdMemberEntity(
      HouseholdEntity household,
      UserEntity user,
      String displayName,
      String relationship,
      String role,
      String avatarColor,
      String avatarInitials,
      UserEntity managedBy,
      boolean canManageHousehold) {
    this.household = household;
    this.user = user;
    this.displayName = displayName;
    this.relationship = relationship;
    this.role = role;
    this.avatarColor = avatarColor != null ? avatarColor : "#237A57";
    this.avatarInitials = avatarInitials != null ? avatarInitials : "FM";
    this.managedBy = managedBy;
    this.canManageHousehold = canManageHousehold;
    this.createdAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public HouseholdEntity getHousehold() {
    return household;
  }

  public void setHousehold(HouseholdEntity household) {
    this.household = household;
  }

  public UserEntity getUser() {
    return user;
  }

  public void setUser(UserEntity user) {
    this.user = user;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getRelationship() {
    return relationship;
  }

  public void setRelationship(String relationship) {
    this.relationship = relationship;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getAvatarColor() {
    return avatarColor;
  }

  public void setAvatarColor(String avatarColor) {
    this.avatarColor = avatarColor;
  }

  public String getAvatarInitials() {
    return avatarInitials;
  }

  public void setAvatarInitials(String avatarInitials) {
    this.avatarInitials = avatarInitials;
  }

  public UserEntity getManagedBy() {
    return managedBy;
  }

  public void setManagedBy(UserEntity managedBy) {
    this.managedBy = managedBy;
  }

  public boolean isCanManageHousehold() {
    return canManageHousehold;
  }

  public void setCanManageHousehold(boolean canManageHousehold) {
    this.canManageHousehold = canManageHousehold;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public boolean isDependent() {
    return "DEPENDENT".equalsIgnoreCase(role);
  }

  public boolean isOwnerOrAdmin() {
    return "HOUSEHOLD_OWNER".equalsIgnoreCase(role)
        || "HOUSEHOLD_ADMIN".equalsIgnoreCase(role)
        || canManageHousehold;
  }
}
