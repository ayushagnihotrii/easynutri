package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Persistent community-submitted recipe with verified nutritional calculations. */
@Entity
@Table(name = "community_recipes")
public class CommunityRecipeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id", nullable = false)
  private UserEntity author;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "cover_image_url", length = 500)
  private String coverImageUrl;

  @Column(name = "base_servings", nullable = false)
  private int baseServings = 2;

  @Column(name = "prep_minutes", nullable = false)
  private int prepMinutes = 15;

  @Column(name = "cook_minutes", nullable = false)
  private int cookMinutes = 20;

  @Column(name = "meal_slot", nullable = false, length = 30)
  private String mealSlot = "LUNCH";

  @Column(name = "dietary_tags", length = 500)
  private String dietaryTags = "";

  @Column(name = "instructions_text", nullable = false, columnDefinition = "TEXT")
  private String instructionsText;

  @Column(name = "calories_per_serving", nullable = false)
  private double caloriesPerServing = 0.0;

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

  @Column(name = "iron_mg", nullable = false)
  private double ironMg = 0.0;

  @Column(name = "calcium_mg", nullable = false)
  private double calciumMg = 0.0;

  @Column(nullable = false, length = 30)
  private String status = "APPROVED"; // APPROVED, PENDING_REVIEW, REJECTED, HIDDEN

  @Column(name = "like_count", nullable = false)
  private int likeCount = 0;

  @Column(name = "comment_count", nullable = false)
  private int commentCount = 0;

  @Column(name = "bookmark_count", nullable = false)
  private int bookmarkCount = 0;

  @OneToMany(
      mappedBy = "communityRecipe",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.EAGER)
  private List<CommunityRecipeIngredientEntity> ingredients = new ArrayList<>();

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  public CommunityRecipeEntity() {}

  public CommunityRecipeEntity(
      UserEntity author,
      String title,
      String description,
      String coverImageUrl,
      int baseServings,
      int prepMinutes,
      int cookMinutes,
      String mealSlot,
      String dietaryTags,
      String instructionsText,
      double caloriesPerServing,
      double proteinG,
      double carbsG,
      double fatG,
      double fiberG,
      double sodiumMg,
      double ironMg,
      double calciumMg,
      String status) {
    this.author = author;
    this.title = title;
    this.description = description;
    this.coverImageUrl = coverImageUrl;
    this.baseServings = baseServings > 0 ? baseServings : 2;
    this.prepMinutes = prepMinutes;
    this.cookMinutes = cookMinutes;
    this.mealSlot = mealSlot != null ? mealSlot : "LUNCH";
    this.dietaryTags = dietaryTags != null ? dietaryTags : "";
    this.instructionsText = instructionsText;
    this.caloriesPerServing = caloriesPerServing;
    this.proteinG = proteinG;
    this.carbsG = carbsG;
    this.fatG = fatG;
    this.fiberG = fiberG;
    this.sodiumMg = sodiumMg;
    this.ironMg = ironMg;
    this.calciumMg = calciumMg;
    this.status = status != null ? status : "APPROVED";
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  public void addIngredient(CommunityRecipeIngredientEntity ing) {
    ingredients.add(ing);
    ing.setCommunityRecipe(this);
  }

  public Long getId() {
    return id;
  }

  public UserEntity getAuthor() {
    return author;
  }

  public void setAuthor(UserEntity author) {
    this.author = author;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getCoverImageUrl() {
    return coverImageUrl;
  }

  public void setCoverImageUrl(String coverImageUrl) {
    this.coverImageUrl = coverImageUrl;
  }

  public int getBaseServings() {
    return baseServings;
  }

  public void setBaseServings(int baseServings) {
    this.baseServings = baseServings;
  }

  public int getPrepMinutes() {
    return prepMinutes;
  }

  public void setPrepMinutes(int prepMinutes) {
    this.prepMinutes = prepMinutes;
  }

  public int getCookMinutes() {
    return cookMinutes;
  }

  public void setCookMinutes(int cookMinutes) {
    this.cookMinutes = cookMinutes;
  }

  public String getMealSlot() {
    return mealSlot;
  }

  public void setMealSlot(String mealSlot) {
    this.mealSlot = mealSlot;
  }

  public String getDietaryTags() {
    return dietaryTags;
  }

  public void setDietaryTags(String dietaryTags) {
    this.dietaryTags = dietaryTags;
  }

  public String getInstructionsText() {
    return instructionsText;
  }

  public void setInstructionsText(String instructionsText) {
    this.instructionsText = instructionsText;
  }

  public double getCaloriesPerServing() {
    return caloriesPerServing;
  }

  public void setCaloriesPerServing(double caloriesPerServing) {
    this.caloriesPerServing = caloriesPerServing;
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

  public double getIronMg() {
    return ironMg;
  }

  public void setIronMg(double ironMg) {
    this.ironMg = ironMg;
  }

  public double getCalciumMg() {
    return calciumMg;
  }

  public void setCalciumMg(double calciumMg) {
    this.calciumMg = calciumMg;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public int getLikeCount() {
    return likeCount;
  }

  public void setLikeCount(int likeCount) {
    this.likeCount = likeCount;
  }

  public int getCommentCount() {
    return commentCount;
  }

  public void setCommentCount(int commentCount) {
    this.commentCount = commentCount;
  }

  public int getBookmarkCount() {
    return bookmarkCount;
  }

  public void setBookmarkCount(int bookmarkCount) {
    this.bookmarkCount = bookmarkCount;
  }

  public List<CommunityRecipeIngredientEntity> getIngredients() {
    return ingredients;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
