package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

/** Persistent pantry item owned by a user, referencing a canonical food item. */
@Entity
@Table(name = "pantry_items")
public class PantryItemEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "food_id", nullable = false)
  private FoodEntity food;

  @Column(name = "quantity_g", nullable = false)
  private double quantityG;

  @Column(name = "expiration_date")
  private LocalDate expirationDate;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  public PantryItemEntity() {}

  public PantryItemEntity(
      UserEntity user, FoodEntity food, double quantityG, LocalDate expirationDate) {
    this.user = user;
    this.food = food;
    this.quantityG = quantityG;
    this.expirationDate = expirationDate;
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

  public FoodEntity getFood() {
    return food;
  }

  public void setFood(FoodEntity food) {
    this.food = food;
  }

  public double getQuantityG() {
    return quantityG;
  }

  public void setQuantityG(double quantityG) {
    this.quantityG = quantityG;
  }

  public LocalDate getExpirationDate() {
    return expirationDate;
  }

  public void setExpirationDate(LocalDate expirationDate) {
    this.expirationDate = expirationDate;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
