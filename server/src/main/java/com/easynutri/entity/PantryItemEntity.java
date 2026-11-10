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

  @Column(name = "storage_location", nullable = false, length = 30)
  private String storageLocation = "CUPBOARD";

  @Column(name = "unit", nullable = false, length = 30)
  private String unit = "g";

  @Column(name = "purchase_date")
  private LocalDate purchaseDate;

  @Column(name = "notes")
  private String notes;

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
    this.storageLocation = "CUPBOARD";
    this.unit = "g";
    this.purchaseDate = LocalDate.now();
    this.createdAt = Instant.now();
  }

  public PantryItemEntity(
      UserEntity user,
      FoodEntity food,
      double quantityG,
      String unit,
      String storageLocation,
      LocalDate expirationDate,
      LocalDate purchaseDate,
      String notes) {
    this.user = user;
    this.food = food;
    this.quantityG = quantityG;
    this.unit = unit != null ? unit : "g";
    this.storageLocation = storageLocation != null ? storageLocation : "CUPBOARD";
    this.expirationDate = expirationDate;
    this.purchaseDate = purchaseDate != null ? purchaseDate : LocalDate.now();
    this.notes = notes;
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

  public String getStorageLocation() {
    return storageLocation;
  }

  public void setStorageLocation(String storageLocation) {
    this.storageLocation = storageLocation;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public LocalDate getPurchaseDate() {
    return purchaseDate;
  }

  public void setPurchaseDate(LocalDate purchaseDate) {
    this.purchaseDate = purchaseDate;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
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
