package com.easynutri.entity;

import jakarta.persistence.*;

/** Persistent entity defining named household measures (e.g. katori, roti, tbsp) for foods. */
@Entity
@Table(name = "food_portions")
public class FoodPortionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "food_id", nullable = false)
  private FoodEntity food;

  @Column(name = "portion_name", nullable = false, length = 100)
  private String portionName;

  @Column(nullable = false)
  private double grams;

  public FoodPortionEntity() {}

  public FoodPortionEntity(FoodEntity food, String portionName, double grams) {
    this.food = food;
    this.portionName = portionName;
    this.grams = grams;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public FoodEntity getFood() {
    return food;
  }

  public void setFood(FoodEntity food) {
    this.food = food;
  }

  public String getPortionName() {
    return portionName;
  }

  public void setPortionName(String portionName) {
    this.portionName = portionName;
  }

  public double getGrams() {
    return grams;
  }

  public void setGrams(double grams) {
    this.grams = grams;
  }
}
