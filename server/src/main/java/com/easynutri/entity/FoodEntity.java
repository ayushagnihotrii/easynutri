package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;

/** Persistent canonical food record holding nutritional data per 100 grams. */
@Entity
@Table(name = "foods")
public class FoodEntity {

  @Id
  @Column(length = 100)
  private String id;

  @Column(name = "name_en", nullable = false, length = 200)
  private String nameEn;

  @Column(name = "name_hi", length = 200)
  private String nameHi;

  @Column(nullable = false, length = 50)
  private String category;

  @Column(name = "energy_kcal", nullable = false)
  private double energyKcal;

  @Column(name = "protein_g", nullable = false)
  private double proteinG;

  @Column(name = "carbohydrate_g", nullable = false)
  private double carbohydrateG;

  @Column(name = "sugar_g", nullable = false)
  private double sugarG = 0.0;

  @Column(name = "fat_g", nullable = false)
  private double fatG;

  @Column(name = "saturated_fat_g", nullable = false)
  private double saturatedFatG = 0.0;

  @Column(name = "fibre_g", nullable = false)
  private double fibreG = 0.0;

  @Column(name = "sodium_mg", nullable = false)
  private double sodiumMg = 0.0;

  @Column(name = "potassium_mg", nullable = false)
  private double potassiumMg = 0.0;

  @Column(name = "calcium_mg", nullable = false)
  private double calciumMg = 0.0;

  @Column(name = "iron_mg", nullable = false)
  private double ironMg = 0.0;

  @Column(name = "allergen_tags", length = 500)
  private String allergenTags = "";

  @Column(name = "diet_flags", length = 500)
  private String dietFlags = "";

  @Column(nullable = false, length = 50)
  private String source = "SAMPLE";

  @Column(nullable = false)
  private boolean verified = false;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  public FoodEntity() {}

  public FoodEntity(
      String id,
      String nameEn,
      String nameHi,
      String category,
      double energyKcal,
      double proteinG,
      double carbohydrateG,
      double fatG,
      String source,
      boolean verified) {
    this.id = id;
    this.nameEn = nameEn;
    this.nameHi = nameHi;
    this.category = category;
    this.energyKcal = energyKcal;
    this.proteinG = proteinG;
    this.carbohydrateG = carbohydrateG;
    this.fatG = fatG;
    this.source = source;
    this.verified = verified;
    this.createdAt = Instant.now();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getNameEn() {
    return nameEn;
  }

  public void setNameEn(String nameEn) {
    this.nameEn = nameEn;
  }

  public String getNameHi() {
    return nameHi;
  }

  public void setNameHi(String nameHi) {
    this.nameHi = nameHi;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public double getEnergyKcal() {
    return energyKcal;
  }

  public void setEnergyKcal(double energyKcal) {
    this.energyKcal = energyKcal;
  }

  public double getProteinG() {
    return proteinG;
  }

  public void setProteinG(double proteinG) {
    this.proteinG = proteinG;
  }

  public double getCarbohydrateG() {
    return carbohydrateG;
  }

  public void setCarbohydrateG(double carbohydrateG) {
    this.carbohydrateG = carbohydrateG;
  }

  public double getSugarG() {
    return sugarG;
  }

  public void setSugarG(double sugarG) {
    this.sugarG = sugarG;
  }

  public double getFatG() {
    return fatG;
  }

  public void setFatG(double fatG) {
    this.fatG = fatG;
  }

  public double getSaturatedFatG() {
    return saturatedFatG;
  }

  public void setSaturatedFatG(double saturatedFatG) {
    this.saturatedFatG = saturatedFatG;
  }

  public double getFibreG() {
    return fibreG;
  }

  public void setFibreG(double fibreG) {
    this.fibreG = fibreG;
  }

  public double getSodiumMg() {
    return sodiumMg;
  }

  public void setSodiumMg(double sodiumMg) {
    this.sodiumMg = sodiumMg;
  }

  public double getPotassiumMg() {
    return potassiumMg;
  }

  public void setPotassiumMg(double potassiumMg) {
    this.potassiumMg = potassiumMg;
  }

  public double getCalciumMg() {
    return calciumMg;
  }

  public void setCalciumMg(double calciumMg) {
    this.calciumMg = calciumMg;
  }

  public double getIronMg() {
    return ironMg;
  }

  public void setIronMg(double ironMg) {
    this.ironMg = ironMg;
  }

  public String getAllergenTags() {
    return allergenTags;
  }

  public void setAllergenTags(String allergenTags) {
    this.allergenTags = allergenTags;
  }

  public String getDietFlags() {
    return dietFlags;
  }

  public void setDietFlags(String dietFlags) {
    this.dietFlags = dietFlags;
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

  public Instant getCreatedAt() {
    return createdAt;
  }
}
