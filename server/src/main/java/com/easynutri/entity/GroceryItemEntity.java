package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;

/** Persistent entity for items in a user's grocery / shopping checklist. */
@Entity
@Table(name = "grocery_items")
public class GroceryItemEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "food_id")
  private FoodEntity food;

  @Column(name = "item_name", nullable = false, length = 200)
  private String itemName;

  @Column(nullable = false)
  private double quantity = 1.0;

  @Column(nullable = false, length = 50)
  private String unit = "serving";

  @Column(nullable = false)
  private boolean checked = false;

  @Column(nullable = false, length = 50)
  private String category = "GENERAL";

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  public GroceryItemEntity() {}

  public GroceryItemEntity(
      UserEntity user,
      FoodEntity food,
      String itemName,
      double quantity,
      String unit,
      String category) {
    this.user = user;
    this.food = food;
    this.itemName = itemName;
    this.quantity = quantity;
    this.unit = unit != null ? unit : "serving";
    this.category = category != null ? category : "GENERAL";
    this.checked = false;
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

  public String getItemName() {
    return itemName;
  }

  public void setItemName(String itemName) {
    this.itemName = itemName;
  }

  public double getQuantity() {
    return quantity;
  }

  public void setQuantity(double quantity) {
    this.quantity = quantity;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public boolean isChecked() {
    return checked;
  }

  public void setChecked(boolean checked) {
    this.checked = checked;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
