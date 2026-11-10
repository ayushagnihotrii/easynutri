package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;

/** Persistent entity for user-bookmarked clinical recipes. */
@Entity
@Table(
    name = "saved_recipes",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_user_saved_recipe",
            columnNames = {"user_id", "recipe_id"}))
public class SavedRecipeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "recipe_id", nullable = false)
  private RecipeEntity recipe;

  @Column(columnDefinition = "TEXT")
  private String notes;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  public SavedRecipeEntity() {}

  public SavedRecipeEntity(UserEntity user, RecipeEntity recipe, String notes) {
    this.user = user;
    this.recipe = recipe;
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

  public RecipeEntity getRecipe() {
    return recipe;
  }

  public void setRecipe(RecipeEntity recipe) {
    this.recipe = recipe;
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
