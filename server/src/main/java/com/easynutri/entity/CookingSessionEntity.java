package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;

/** Persistent interactive cooking session tracking progress, timers, and inventory deduction. */
@Entity
@Table(name = "cooking_sessions")
public class CookingSessionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "recipe_id", nullable = false)
  private RecipeEntity recipe;

  @Column(nullable = false)
  private int servings = 2;

  @Column(name = "started_at", nullable = false)
  private Instant startedAt = Instant.now();

  @Column(name = "completed_at")
  private Instant completedAt;

  @Column(name = "current_step", nullable = false)
  private int currentStep = 1;

  @Column(name = "pantry_deducted", nullable = false)
  private boolean pantryDeducted = false;

  @Column(name = "diary_logged", nullable = false)
  private boolean diaryLogged = false;

  @Column(name = "notes")
  private String notes;

  public CookingSessionEntity() {}

  public CookingSessionEntity(UserEntity user, RecipeEntity recipe, int servings) {
    this.user = user;
    this.recipe = recipe;
    this.servings = servings > 0 ? servings : recipe.getBaseServings();
    this.startedAt = Instant.now();
    this.currentStep = 1;
    this.pantryDeducted = false;
    this.diaryLogged = false;
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

  public int getServings() {
    return servings;
  }

  public void setServings(int servings) {
    this.servings = servings;
  }

  public Instant getStartedAt() {
    return startedAt;
  }

  public void setStartedAt(Instant startedAt) {
    this.startedAt = startedAt;
  }

  public Instant getCompletedAt() {
    return completedAt;
  }

  public void setCompletedAt(Instant completedAt) {
    this.completedAt = completedAt;
  }

  public int getCurrentStep() {
    return currentStep;
  }

  public void setCurrentStep(int currentStep) {
    this.currentStep = currentStep;
  }

  public boolean isPantryDeducted() {
    return pantryDeducted;
  }

  public void setPantryDeducted(boolean pantryDeducted) {
    this.pantryDeducted = pantryDeducted;
  }

  public boolean isDiaryLogged() {
    return diaryLogged;
  }

  public void setDiaryLogged(boolean diaryLogged) {
    this.diaryLogged = diaryLogged;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public boolean isCompleted() {
    return completedAt != null;
  }
}
