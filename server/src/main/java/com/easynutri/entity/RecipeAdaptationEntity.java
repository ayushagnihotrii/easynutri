package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;

/** Persistent record of an AI-adapted recipe variant verified by the pure Java rules engine. */
@Entity
@Table(name = "recipe_adaptations")
public class RecipeAdaptationEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "base_recipe_id", nullable = false)
  private RecipeEntity baseRecipe;

  @Column(name = "adaptation_title", nullable = false, length = 200)
  private String adaptationTitle;

  @Column(name = "adaptation_type", nullable = false, length = 50)
  private String adaptationType =
      "CUSTOM"; // VEGAN, JAIN, DAIRY_FREE, LOW_SODIUM, HIGH_PROTEIN, CUSTOM

  @Column(name = "modifications_json", nullable = false, columnDefinition = "TEXT")
  private String modificationsJson;

  @Column(name = "instructions_text", columnDefinition = "TEXT")
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

  @Column(name = "safety_verdict", nullable = false, length = 30)
  private String safetyVerdict = "ALLOW";

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  public RecipeAdaptationEntity() {}

  public RecipeAdaptationEntity(
      UserEntity user,
      RecipeEntity baseRecipe,
      String adaptationTitle,
      String adaptationType,
      String modificationsJson,
      String instructionsText,
      double caloriesPerServing,
      double proteinG,
      double carbsG,
      double fatG,
      double fiberG,
      double sodiumMg,
      String safetyVerdict) {
    this.user = user;
    this.baseRecipe = baseRecipe;
    this.adaptationTitle = adaptationTitle;
    this.adaptationType = adaptationType != null ? adaptationType : "CUSTOM";
    this.modificationsJson = modificationsJson;
    this.instructionsText = instructionsText;
    this.caloriesPerServing = caloriesPerServing;
    this.proteinG = proteinG;
    this.carbsG = carbsG;
    this.fatG = fatG;
    this.fiberG = fiberG;
    this.sodiumMg = sodiumMg;
    this.safetyVerdict = safetyVerdict != null ? safetyVerdict : "ALLOW";
    this.createdAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public UserEntity getUser() {
    return user;
  }

  public void setUser(UserEntity user) {
    this.user = user;
  }

  public RecipeEntity getBaseRecipe() {
    return baseRecipe;
  }

  public void setBaseRecipe(RecipeEntity baseRecipe) {
    this.baseRecipe = baseRecipe;
  }

  public String getAdaptationTitle() {
    return adaptationTitle;
  }

  public void setAdaptationTitle(String adaptationTitle) {
    this.adaptationTitle = adaptationTitle;
  }

  public String getAdaptationType() {
    return adaptationType;
  }

  public void setAdaptationType(String adaptationType) {
    this.adaptationType = adaptationType;
  }

  public String getModificationsJson() {
    return modificationsJson;
  }

  public void setModificationsJson(String modificationsJson) {
    this.modificationsJson = modificationsJson;
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

  public String getSafetyVerdict() {
    return safetyVerdict;
  }

  public void setSafetyVerdict(String safetyVerdict) {
    this.safetyVerdict = safetyVerdict;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
