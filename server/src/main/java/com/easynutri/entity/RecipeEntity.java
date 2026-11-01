package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Persistent recipe entity containing ingredients, instructions, and serving information. */
@Entity
@Table(name = "recipes")
public class RecipeEntity {

  @Id
  @Column(length = 100)
  private String id;

  @Column(name = "title_en", nullable = false, length = 200)
  private String titleEn;

  @Column(name = "title_hi", length = 200)
  private String titleHi;

  @Column(name = "description_en", columnDefinition = "TEXT")
  private String descriptionEn;

  @Column(name = "base_servings", nullable = false)
  private int baseServings = 2;

  @Column(name = "prep_minutes", nullable = false)
  private int prepMinutes = 15;

  @Column(name = "cook_minutes", nullable = false)
  private int cookMinutes = 20;

  @Column(name = "meal_slot", nullable = false, length = 30)
  private String mealSlot;

  @Column(name = "meal_role", nullable = false, length = 30)
  private String mealRole = "MAIN";

  @Column(name = "instructions_text", columnDefinition = "TEXT")
  private String instructionsText;

  @Column(length = 500)
  private String tags = "";

  @Column(nullable = false, length = 50)
  private String source = "SAMPLE";

  @Column(nullable = false)
  private boolean verified = false;

  @OneToMany(
      mappedBy = "recipe",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.EAGER)
  private List<RecipeIngredientEntity> ingredients = new ArrayList<>();

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  public RecipeEntity() {}

  public RecipeEntity(
      String id,
      String titleEn,
      String titleHi,
      String descriptionEn,
      int baseServings,
      int prepMinutes,
      int cookMinutes,
      String mealSlot,
      String mealRole) {
    this.id = id;
    this.titleEn = titleEn;
    this.titleHi = titleHi;
    this.descriptionEn = descriptionEn;
    this.baseServings = baseServings;
    this.prepMinutes = prepMinutes;
    this.cookMinutes = cookMinutes;
    this.mealSlot = mealSlot;
    this.mealRole = mealRole;
    this.createdAt = Instant.now();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitleEn() {
    return titleEn;
  }

  public void setTitleEn(String titleEn) {
    this.titleEn = titleEn;
  }

  public String getTitleHi() {
    return titleHi;
  }

  public void setTitleHi(String titleHi) {
    this.titleHi = titleHi;
  }

  public String getDescriptionEn() {
    return descriptionEn;
  }

  public void setDescriptionEn(String descriptionEn) {
    this.descriptionEn = descriptionEn;
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

  public String getMealRole() {
    return mealRole;
  }

  public void setMealRole(String mealRole) {
    this.mealRole = mealRole;
  }

  public String getInstructionsText() {
    return instructionsText;
  }

  public void setInstructionsText(String instructionsText) {
    this.instructionsText = instructionsText;
  }

  public String getTags() {
    return tags;
  }

  public void setTags(String tags) {
    this.tags = tags;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public boolean isVerified() {
    return verified;
  }

  public void setVerified(boolean verified) {
    this.verified = verified;
  }

  public List<RecipeIngredientEntity> getIngredients() {
    return ingredients;
  }

  public void setIngredients(List<RecipeIngredientEntity> ingredients) {
    this.ingredients = ingredients != null ? ingredients : new ArrayList<>();
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
