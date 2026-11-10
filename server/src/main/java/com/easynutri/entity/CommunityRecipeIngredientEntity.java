package com.easynutri.entity;

import jakarta.persistence.*;

/** Join entity linking a community recipe to a canonical food item or text ingredient. */
@Entity
@Table(name = "community_recipe_ingredients")
public class CommunityRecipeIngredientEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "community_recipe_id", nullable = false)
  private CommunityRecipeEntity communityRecipe;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "food_id")
  private FoodEntity food;

  @Column(name = "ingredient_name", nullable = false, length = 200)
  private String ingredientName;

  @Column(name = "amount_g", nullable = false)
  private double amountG;

  @Column(nullable = false, length = 30)
  private String unit = "g";

  @Column(name = "is_verified_food", nullable = false)
  private boolean verifiedFood = true;

  public CommunityRecipeIngredientEntity() {}

  public CommunityRecipeIngredientEntity(
      CommunityRecipeEntity communityRecipe,
      FoodEntity food,
      String ingredientName,
      double amountG,
      String unit,
      boolean verifiedFood) {
    this.communityRecipe = communityRecipe;
    this.food = food;
    this.ingredientName = ingredientName;
    this.amountG = amountG;
    this.unit = unit != null ? unit : "g";
    this.verifiedFood = verifiedFood;
  }

  public Long getId() {
    return id;
  }

  public CommunityRecipeEntity getCommunityRecipe() {
    return communityRecipe;
  }

  public void setCommunityRecipe(CommunityRecipeEntity communityRecipe) {
    this.communityRecipe = communityRecipe;
  }

  public FoodEntity getFood() {
    return food;
  }

  public void setFood(FoodEntity food) {
    this.food = food;
  }

  public String getIngredientName() {
    return ingredientName;
  }

  public void setIngredientName(String ingredientName) {
    this.ingredientName = ingredientName;
  }

  public double getAmountG() {
    return amountG;
  }

  public void setAmountG(double amountG) {
    this.amountG = amountG;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public boolean isVerifiedFood() {
    return verifiedFood;
  }

  public void setVerifiedFood(boolean verifiedFood) {
    this.verifiedFood = verifiedFood;
  }
}
