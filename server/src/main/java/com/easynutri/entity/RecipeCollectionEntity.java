package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/** User-defined recipe collection / bookmark category (Favorites, High Protein, etc.). */
@Entity
@Table(
    name = "recipe_collections",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_user_collection_name",
          columnNames = {"user_id", "name"})
    })
public class RecipeCollectionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(name = "display_order", nullable = false)
  private int displayOrder = 0;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "collection_recipes",
      joinColumns = @JoinColumn(name = "collection_id"),
      inverseJoinColumns = @JoinColumn(name = "recipe_id"))
  private Set<RecipeEntity> recipes = new HashSet<>();

  public RecipeCollectionEntity() {}

  public RecipeCollectionEntity(UserEntity user, String name, int displayOrder) {
    this.user = user;
    this.name = name;
    this.displayOrder = displayOrder;
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public void setDisplayOrder(int displayOrder) {
    this.displayOrder = displayOrder;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Set<RecipeEntity> getRecipes() {
    return recipes;
  }

  public void setRecipes(Set<RecipeEntity> recipes) {
    this.recipes = recipes;
  }

  public void addRecipe(RecipeEntity recipe) {
    this.recipes.add(recipe);
  }

  public void removeRecipe(RecipeEntity recipe) {
    this.recipes.remove(recipe);
  }
}
