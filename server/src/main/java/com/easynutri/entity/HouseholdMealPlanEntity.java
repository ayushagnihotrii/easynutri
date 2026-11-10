package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/** Persistent shared meal plan for household multi-member coordination. */
@Entity
@Table(name = "household_meal_plans")
public class HouseholdMealPlanEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "household_id", nullable = false)
  private HouseholdEntity household;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "recipe_id", nullable = false)
  private RecipeEntity recipe;

  @Column(name = "plan_date", nullable = false)
  private LocalDate planDate;

  @Column(name = "meal_slot", nullable = false, length = 30)
  private String mealSlot; // BREAKFAST, LUNCH, DINNER, SNACK

  @Column(name = "participating_member_ids", nullable = false)
  private String participatingMemberIds; // comma-separated user_ids

  @Column(name = "total_servings", nullable = false)
  private double totalServings = 2.0;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  public HouseholdMealPlanEntity() {}

  public HouseholdMealPlanEntity(
      HouseholdEntity household,
      RecipeEntity recipe,
      LocalDate planDate,
      String mealSlot,
      List<Long> participatingUserIds,
      double totalServings) {
    this.household = household;
    this.recipe = recipe;
    this.planDate = planDate;
    this.mealSlot = mealSlot;
    this.participatingMemberIds =
        participatingUserIds != null
            ? String.join(",", participatingUserIds.stream().map(String::valueOf).toList())
            : "";
    this.totalServings = totalServings > 0 ? totalServings : 2.0;
    this.createdAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public HouseholdEntity getHousehold() {
    return household;
  }

  public RecipeEntity getRecipe() {
    return recipe;
  }

  public LocalDate getPlanDate() {
    return planDate;
  }

  public String getMealSlot() {
    return mealSlot;
  }

  public String getParticipatingMemberIds() {
    return participatingMemberIds;
  }

  public List<Long> getParticipatingUserIdList() {
    if (participatingMemberIds == null || participatingMemberIds.isBlank()) {
      return List.of();
    }
    return Arrays.stream(participatingMemberIds.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .map(Long::parseLong)
        .toList();
  }

  public double getTotalServings() {
    return totalServings;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
