package com.easynutri.entity;

import jakarta.persistence.*;

/** Join entity linking a recipe to a canonical food item with a specified gram amount. */
@Entity
@Table(name = "recipe_ingredients")
public class RecipeIngredientEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "recipe_id", nullable = false)
  private RecipeEntity recipe;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "food_id", nullable = false)
  private FoodEntity food;

  @Column(name = "amount_g", nullable = false)
  private double amountG;

  @Column(nullable = false)
  private boolean optional = false;

  public RecipeIngredientEntity() {}

  public RecipeIngredientEntity(
      RecipeEntity recipe, FoodEntity food, double amountG, boolean optional) {
    this.recipe = recipe;
    this.food = food;
    this.amountG = amountG;
    this.optional = optional;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public RecipeEntity getRecipe() {
    return recipe;
  }

  public void setRecipe(RecipeEntity recipe) {
    this.recipe = recipe;
  }

  public FoodEntity getFood() {
    return food;
  }

  public void setFood(FoodEntity food) {
    this.food = food;
  }

  public double getAmountG() {
    return amountG;
  }

  public void setAmountG(double amountG) {
    this.amountG = amountG;
  }

  public boolean isOptional() {
    return optional;
  }

  public void setOptional(boolean optional) {
    this.optional = optional;
  }
}
