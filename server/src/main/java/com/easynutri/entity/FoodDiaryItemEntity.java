package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * An individual food item or portion within a food diary entry. Retains immutable nutrition
 * snapshot values taken at log time.
 */
@Entity
@Table(name = "food_diary_items")
public class FoodDiaryItemEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "entry_id", nullable = false)
  private FoodDiaryEntryEntity entry;

  @Column(name = "food_id", length = 100)
  private String foodId;

  @Column(name = "recipe_id", length = 100)
  private String recipeId;

  @Column(name = "item_name", nullable = false)
  private String itemName;

  @Column(name = "amount_g", nullable = false)
  private double amountG = 100.0;

  @Column(name = "servings", nullable = false)
  private double servings = 1.0;

  @Column(name = "calories", nullable = false)
  private double calories = 0.0;

  @Column(name = "protein_g", nullable = false)
  private double proteinG = 0.0;

  @Column(name = "carbs_g", nullable = false)
  private double carbsG = 0.0;

  @Column(name = "fat_g", nullable = false)
  private double fatG = 0.0;

  @Column(name = "fiber_g", nullable = false)
  private double fiberG = 0.0;

  @Column(name = "sodium_mg", nullable = false)
  private double sodiumMg = 0.0;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  public FoodDiaryItemEntity() {}

  public Long getId() {
    return id;
  }

  public FoodDiaryEntryEntity getEntry() {
    return entry;
  }

  public void setEntry(FoodDiaryEntryEntity entry) {
    this.entry = entry;
  }

  public String getFoodId() {
    return foodId;
  }

  public void setFoodId(String foodId) {
    this.foodId = foodId;
  }

  public String getRecipeId() {
    return recipeId;
  }

  public void setRecipeId(String recipeId) {
    this.recipeId = recipeId;
  }

  public String getItemName() {
    return itemName;
  }

  public void setItemName(String itemName) {
    this.itemName = itemName;
  }

  public double getAmountG() {
    return amountG;
  }

  public void setAmountG(double amountG) {
    this.amountG = amountG;
  }

  public double getServings() {
    return servings;
  }

  public void setServings(double servings) {
    this.servings = servings;
  }

  public double getCalories() {
    return calories;
  }

  public void setCalories(double calories) {
    this.calories = calories;
  }

  public double getProteinG() {
    return proteinG;
  }

  public void setProteinG(double proteinG) {
    this.proteinG = proteinG;
  }

  public double getCarbsG() {
    return carbsG;
  }

  public void setCarbsG(double carbsG) {
    this.carbsG = carbsG;
  }

  public double getFatG() {
    return fatG;
  }

  public void setFatG(double fatG) {
    this.fatG = fatG;
  }

  public double getFiberG() {
    return fiberG;
  }

  public void setFiberG(double fiberG) {
    this.fiberG = fiberG;
  }

  public double getSodiumMg() {
    return sodiumMg;
  }

  public void setSodiumMg(double sodiumMg) {
    this.sodiumMg = sodiumMg;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
