package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

/** Persistent entity for planned meals scheduled into a user's calendar. */
@Entity
@Table(name = "meal_plan_items")
public class MealPlanItemEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "recipe_id", nullable = false)
  private RecipeEntity recipe;

  @Column(name = "planned_date", nullable = false)
  private LocalDate plannedDate;

  @Column(name = "meal_slot", nullable = false, length = 30)
  private String mealSlot = "LUNCH";

  @Column(nullable = false)
  private int servings = 2;

  @Column(nullable = false)
  private boolean locked = false;

  @Column(nullable = false)
  private boolean completed = false;

  @Column(name = "selection_reason", length = 255)
  private String selectionReason;

  @Column(columnDefinition = "TEXT")
  private String notes;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  public MealPlanItemEntity() {}

  public MealPlanItemEntity(
      UserEntity user, RecipeEntity recipe, LocalDate plannedDate, String mealSlot, int servings) {
    this.user = user;
    this.recipe = recipe;
    this.plannedDate = plannedDate;
    this.mealSlot = mealSlot != null ? mealSlot : "LUNCH";
    this.servings = Math.max(1, servings);
    this.locked = false;
    this.completed = false;
    this.createdAt = Instant.now();
  }

  public MealPlanItemEntity(
      UserEntity user,
      RecipeEntity recipe,
      LocalDate plannedDate,
      String mealSlot,
      int servings,
      boolean locked,
      String selectionReason) {
    this.user = user;
    this.recipe = recipe;
    this.plannedDate = plannedDate;
    this.mealSlot = mealSlot != null ? mealSlot : "LUNCH";
    this.servings = Math.max(1, servings);
    this.locked = locked;
    this.completed = false;
    this.selectionReason = selectionReason;
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

  public RecipeEntity getRecipe() {
    return recipe;
  }

  public void setRecipe(RecipeEntity recipe) {
    this.recipe = recipe;
  }

  public LocalDate getPlannedDate() {
    return plannedDate;
  }

  public void setPlannedDate(LocalDate plannedDate) {
    this.plannedDate = plannedDate;
  }

  public String getMealSlot() {
    return mealSlot;
  }

  public void setMealSlot(String mealSlot) {
    this.mealSlot = mealSlot;
  }

  public int getServings() {
    return servings;
  }

  public void setServings(int servings) {
    this.servings = servings;
  }

  public boolean isLocked() {
    return locked;
  }

  public void setLocked(boolean locked) {
    this.locked = locked;
  }

  public boolean isCompleted() {
    return completed;
  }

  public void setCompleted(boolean completed) {
    this.completed = completed;
  }

  public String getSelectionReason() {
    return selectionReason;
  }

  public void setSelectionReason(String selectionReason) {
    this.selectionReason = selectionReason;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
