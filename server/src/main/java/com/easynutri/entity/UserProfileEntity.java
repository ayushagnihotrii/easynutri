package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Persistent profile entity holding biometric measurements, diet, allergies, and clinical targets.
 */
@Entity
@Table(name = "user_profiles")
public class UserProfileEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private UserEntity user;

  @Column(nullable = false)
  private int age;

  @Column(name = "biological_sex", nullable = false, length = 20)
  private String biologicalSex;

  @Column(name = "height_cm", nullable = false)
  private double heightCm;

  @Column(name = "weight_kg", nullable = false)
  private double weightKg;

  @Column(name = "activity_level", nullable = false, length = 30)
  private String activityLevel = "MODERATE";

  @Column(name = "health_goal", nullable = false, length = 30)
  private String healthGoal = "MAINTAIN";

  @Column(name = "dietary_preference", nullable = false, length = 30)
  private String dietaryPreference = "VEGETARIAN";

  @Column(name = "is_pregnant", nullable = false)
  private boolean isPregnant = false;

  @Column(name = "sensitive_mode", nullable = false)
  private boolean sensitiveMode = false;

  @Column(name = "onboarding_completed", nullable = false)
  private boolean onboardingCompleted = false;

  @Column(name = "target_calories")
  private Integer targetCalories;

  @Column(name = "target_protein_g")
  private Integer targetProteinG;

  @Column(name = "target_carb_g")
  private Integer targetCarbG;

  @Column(name = "target_fat_g")
  private Integer targetFatG;

  @Column(name = "target_water_l")
  private Double targetWaterL;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
      name = "user_allergies",
      joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "user_id"))
  @Column(name = "allergy")
  private Set<String> allergies = new HashSet<>();

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
      name = "user_health_conditions",
      joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "user_id"))
  @Column(name = "health_condition")
  private Set<String> healthConditions = new HashSet<>();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  public UserProfileEntity() {}

  public UserProfileEntity(
      UserEntity user, int age, String biologicalSex, double heightCm, double weightKg) {
    this.user = user;
    this.age = age;
    this.biologicalSex = biologicalSex;
    this.heightCm = heightCm;
    this.weightKg = weightKg;
    this.updatedAt = Instant.now();
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

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public String getBiologicalSex() {
    return biologicalSex;
  }

  public void setBiologicalSex(String biologicalSex) {
    this.biologicalSex = biologicalSex;
  }

  public double getHeightCm() {
    return heightCm;
  }

  public void setHeightCm(double heightCm) {
    this.heightCm = heightCm;
  }

  public double getWeightKg() {
    return weightKg;
  }

  public void setWeightKg(double weightKg) {
    this.weightKg = weightKg;
  }

  public String getActivityLevel() {
    return activityLevel;
  }

  public void setActivityLevel(String activityLevel) {
    this.activityLevel = activityLevel;
  }

  public String getHealthGoal() {
    return healthGoal;
  }

  public void setHealthGoal(String healthGoal) {
    this.healthGoal = healthGoal;
  }

  public String getDietaryPreference() {
    return dietaryPreference;
  }

  public void setDietaryPreference(String dietaryPreference) {
    this.dietaryPreference = dietaryPreference;
  }

  public boolean isPregnant() {
    return isPregnant;
  }

  public void setPregnant(boolean pregnant) {
    isPregnant = pregnant;
  }

  public boolean isSensitiveMode() {
    return sensitiveMode;
  }

  public void setSensitiveMode(boolean sensitiveMode) {
    this.sensitiveMode = sensitiveMode;
  }

  public boolean isOnboardingCompleted() {
    return onboardingCompleted;
  }

  public void setOnboardingCompleted(boolean onboardingCompleted) {
    this.onboardingCompleted = onboardingCompleted;
  }

  public Integer getTargetCalories() {
    return targetCalories;
  }

  public void setTargetCalories(Integer targetCalories) {
    this.targetCalories = targetCalories;
  }

  public Integer getTargetProteinG() {
    return targetProteinG;
  }

  public void setTargetProteinG(Integer targetProteinG) {
    this.targetProteinG = targetProteinG;
  }

  public Integer getTargetCarbG() {
    return targetCarbG;
  }

  public void setTargetCarbG(Integer targetCarbG) {
    this.targetCarbG = targetCarbG;
  }

  public Integer getTargetFatG() {
    return targetFatG;
  }

  public void setTargetFatG(Integer targetFatG) {
    this.targetFatG = targetFatG;
  }

  public Double getTargetWaterL() {
    return targetWaterL;
  }

  public void setTargetWaterL(Double targetWaterL) {
    this.targetWaterL = targetWaterL;
  }

  public Set<String> getAllergies() {
    return allergies;
  }

  public void setAllergies(Set<String> allergies) {
    this.allergies = allergies != null ? allergies : new HashSet<>();
  }

  public Set<String> getHealthConditions() {
    return healthConditions;
  }

  public void setHealthConditions(Set<String> healthConditions) {
    this.healthConditions = healthConditions != null ? healthConditions : new HashSet<>();
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
