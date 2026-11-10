package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Persistent entity representing a family household unit. */
@Entity
@Table(name = "households")
public class HouseholdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_user_id", nullable = false)
  private UserEntity owner;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @OneToMany(mappedBy = "household", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<HouseholdMemberEntity> members = new ArrayList<>();

  public HouseholdEntity() {}

  public HouseholdEntity(String name, UserEntity owner) {
    this.name = name;
    this.owner = owner;
    this.createdAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public UserEntity getOwner() {
    return owner;
  }

  public void setOwner(UserEntity owner) {
    this.owner = owner;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public List<HouseholdMemberEntity> getMembers() {
    return members;
  }

  public void addMember(HouseholdMemberEntity member) {
    members.add(member);
    member.setHousehold(this);
  }

  public void removeMember(HouseholdMemberEntity member) {
    members.remove(member);
    member.setHousehold(null);
  }
}
