package com.easynutri.data.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Models {

  // Auth
  public static class LoginRequest {
    public String username;
    public String password;

    public LoginRequest(String username, String password) {
      this.username = username;
      this.password = password;
    }
  }

  public static class RegisterRequest {
    public String username;
    public String email;
    public String password;
    public String fullName;

    public RegisterRequest(String username, String email, String password, String fullName) {
      this.username = username;
      this.email = email;
      this.password = password;
      this.fullName = fullName;
    }
  }

  public static class AuthResponse {
    public boolean success;
    public String message;
    public String username;
    public String redirectUrl;
  }

  public static class UserInfoDto {
    public String username;
    public String email;
    public String fullName;
    public String role;
  }

  // Profile & Onboarding
  public static class UserProfileDto {
    public Long id;
    public String username;
    public int age;
    public String biologicalSex;
    public double heightCm;
    public double weightKg;
    public String activityLevel;
    public String healthGoal;
    public String dietaryPreference;
    public boolean isPregnant;
    public boolean sensitiveMode;
    public boolean onboardingCompleted;
    public Integer targetCalories;
    public Integer targetProteinG;
    public Integer targetCarbG;
    public Integer targetFatG;
    public Double targetWaterL;
    public Set<String> allergies;
    public Set<String> healthConditions;
  }

  public static class OnboardingRequest {
    public int age;
    public String biologicalSex;
    public double heightCm;
    public double weightKg;
    public String activityLevel;
    public String healthGoal;
    public String dietaryPreference;
    public boolean isPregnant;
    public boolean sensitiveMode;
    public List<String> allergies = new ArrayList<>();
    public List<String> healthConditions = new ArrayList<>();

    public OnboardingRequest(
        int age,
        String biologicalSex,
        double heightCm,
        double weightKg,
        String activityLevel,
        String healthGoal,
        String dietaryPreference,
        boolean isPregnant,
        boolean sensitiveMode,
        List<String> allergies,
        List<String> healthConditions) {
      this.age = age;
      this.biologicalSex = biologicalSex;
      this.heightCm = heightCm;
      this.weightKg = weightKg;
      this.activityLevel = activityLevel;
      this.healthGoal = healthGoal;
      this.dietaryPreference = dietaryPreference;
      this.isPregnant = isPregnant;
      this.sensitiveMode = sensitiveMode;
      this.allergies = allergies != null ? allergies : new ArrayList<>();
      this.healthConditions = healthConditions != null ? healthConditions : new ArrayList<>();
    }
  }

  public static class ProfileUpdateRequest {
    public int age;
    public String biologicalSex;
    public double heightCm;
    public double weightKg;
    public String activityLevel;
    public String healthGoal;
    public String dietaryPreference;
    public boolean isPregnant;
    public boolean sensitiveMode;
    public List<String> allergies = new ArrayList<>();
    public List<String> healthConditions = new ArrayList<>();

    public ProfileUpdateRequest(
        int age,
        String biologicalSex,
        double heightCm,
        double weightKg,
        String activityLevel,
        String healthGoal,
        String dietaryPreference,
        boolean isPregnant,
        boolean sensitiveMode,
        List<String> allergies,
        List<String> healthConditions) {
      this.age = age;
      this.biologicalSex = biologicalSex;
      this.heightCm = heightCm;
      this.weightKg = weightKg;
      this.activityLevel = activityLevel;
      this.healthGoal = healthGoal;
      this.dietaryPreference = dietaryPreference;
      this.isPregnant = isPregnant;
      this.sensitiveMode = sensitiveMode;
      this.allergies = allergies != null ? allergies : new ArrayList<>();
      this.healthConditions = healthConditions != null ? healthConditions : new ArrayList<>();
    }
  }

  // Food & Recipe
  public static class FoodDto {
    public String id;
    public String nameEn;
    public String nameHi;
    public String category;
    public double caloriesKcal;
    public double proteinG;
    public double carbsG;
    public double fatG;
    public double fiberG;
    public double sodiumMg;
  }

  public static class RecipeDetailDto {
    public String id;
    public String titleEn;
    public String titleHi;
    public String description;
    public String mealSlot;
    public int cookTimeMinutes;
    public int prepTimeMinutes;
    public int defaultServings;
    public List<ScaledIngredientDto> ingredients;
    public List<String> instructions;
    public List<String> dietaryTags;
    public double caloriesKcal;
    public double proteinG;
    public double carbsG;
    public double fatG;
    public double fiberG;
  }

  public static class ScaledIngredientDto {
    public String foodId;
    public String nameEn;
    public String nameHi;
    public double amountG;
    public String displayAmount;
    public boolean optional;
  }

  // Dashboard
  public static class DashboardDto {
    public UserProfileDto profile;
    public Integer targetCalories;
    public Integer consumedCalories;
    public Integer burnedCalories;
    public Integer netCalories;
    public Integer targetProteinG;
    public Integer consumedProteinG;
    public Integer targetCarbG;
    public Integer consumedCarbG;
    public Integer targetFatG;
    public Integer consumedFatG;
    public Double targetWaterL;
    public Double consumedWaterL;
    public Integer targetSteps;
    public Integer currentSteps;
    public int activeStreakDays;
    public int expiringPantryCount;
    public List<MealPlanItemDto> todayMeals;
    public List<RecipeDetailDto> suggestedRecipes;
  }

  // Meal Plan
  public static class MealPlanItemDto {
    public Long id;
    public String mealSlot;
    public RecipeDetailDto recipe;
    public int servings;
    public boolean locked;
    public boolean completed;
    public String plannedDate;
  }

  public static class WeeklyMealPlanResponseDto {
    public String weekStartDate;
    public String weekEndDate;
    public List<MealPlanItemDto> items;
  }

  public static class AddMealPlanRequest {
    public String date;
    public String slot;
    public String recipeId;
    public int servings;

    public AddMealPlanRequest(String date, String slot, String recipeId, int servings) {
      this.date = date;
      this.slot = slot;
      this.recipeId = recipeId;
      this.servings = servings;
    }
  }

  public static class ReplaceMealRequest {
    public String targetRecipeId;

    public ReplaceMealRequest(String targetRecipeId) {
      this.targetRecipeId = targetRecipeId;
    }
  }

  // Pantry & Grocery
  public static class PantryItemDto {
    public Long id;
    public String foodId;
    public String foodNameEn;
    public String foodNameHi;
    public double quantityG;
    public String unit;
    public String storageLocation;
    public String expirationDate;
    public long daysUntilExpiry;
    public String expiryUrgency;
  }

  public static class AddPantryItemRequest {
    public String foodId;
    public double quantityG;
    public String unit;
    public String storageLocation;
    public String expirationDate;

    public AddPantryItemRequest(
        String foodId,
        double quantityG,
        String unit,
        String storageLocation,
        String expirationDate) {
      this.foodId = foodId;
      this.quantityG = quantityG;
      this.unit = unit;
      this.storageLocation = storageLocation;
      this.expirationDate = expirationDate;
    }
  }

  public static class UpdatePantryItemRequest {
    public double quantityG;
    public String unit;
    public String storageLocation;
    public String expirationDate;

    public UpdatePantryItemRequest(
        double quantityG, String unit, String storageLocation, String expirationDate) {
      this.quantityG = quantityG;
      this.unit = unit;
      this.storageLocation = storageLocation;
      this.expirationDate = expirationDate;
    }
  }

  public static class GroceryItemDto {
    public Long id;
    public String foodId;
    public String foodNameEn;
    public String foodNameHi;
    public double quantityG;
    public String unit;
    public String category;
    public boolean checked;
    public boolean autoGenerated;
  }

  public static class AddGroceryItemRequest {
    public String foodId;
    public double quantityG;
    public String unit;
    public String category;

    public AddGroceryItemRequest(String foodId, double quantityG, String unit, String category) {
      this.foodId = foodId;
      this.quantityG = quantityG;
      this.unit = unit;
      this.category = category;
    }
  }

  // Cooking
  public static class CookingSessionDto {
    public Long sessionId;
    public String recipeId;
    public String recipeTitle;
    public int currentStepIndex;
    public int totalSteps;
    public boolean isCompleted;
    public boolean pantryDeducted;
    public boolean diaryLogged;
    public List<ScaledIngredientDto> ingredients;
    public List<String> instructions;
  }

  public static class FinishCookingRequest {
    public boolean deductPantry;
    public boolean logToDiary;

    public FinishCookingRequest(boolean deductPantry, boolean logToDiary) {
      this.deductPantry = deductPantry;
      this.logToDiary = logToDiary;
    }
  }

  // Diary
  public static class DiaryDayDto {
    public String date;
    public List<DiarySlotDto> slots;
    public double totalCalories;
    public double totalProteinG;
    public double totalCarbsG;
    public double totalFatG;
    public double totalFiberG;
    public Integer targetCalories;
    public boolean sensitiveMode;
    public String formattedCalories;
  }

  public static class DiarySlotDto {
    public String mealSlot;
    public List<DiaryItemDto> items;
    public double slotCalories;
    public double slotProteinG;
    public double slotCarbsG;
    public double slotFatG;
  }

  public static class DiaryItemDto {
    public Long id;
    public String foodId;
    public String recipeId;
    public String name;
    public double amountG;
    public double calories;
    public double proteinG;
    public double carbsG;
    public double fatG;
  }

  public static class LogFoodItemRequest {
    public String date;
    public String mealSlot;
    public String foodId;
    public String recipeId;
    public double amountG;

    public LogFoodItemRequest(
        String date, String mealSlot, String foodId, String recipeId, double amountG) {
      this.date = date;
      this.mealSlot = mealSlot;
      this.foodId = foodId;
      this.recipeId = recipeId;
      this.amountG = amountG;
    }
  }

  // Hydration
  public static class HydrationSummaryDto {
    public String date;
    public int currentIntakeMl;
    public int dailyTargetMl;
    public int progressPercent;
    public boolean isClinicallyRestricted;
    public String clinicalNotice;
    public List<WaterLogDto> logs;
  }

  public static class WaterLogDto {
    public Long id;
    public int amountMl;
    public String loggedAt;
  }

  public static class LogWaterRequest {
    public String date;
    public int amountMl;

    public LogWaterRequest(String date, int amountMl) {
      this.date = date;
      this.amountMl = amountMl;
    }
  }

  // Activity
  public static class ActivitySummaryDto {
    public String date;
    public int steps;
    public int goalSteps;
    public double totalExpenditureKcal;
    public double netEnergyBalanceKcal;
    public List<WorkoutLogDto> workouts;
  }

  public static class WorkoutLogDto {
    public Long id;
    public String activityType;
    public int durationMinutes;
    public String intensity;
    public double caloriesBurned;
  }

  public static class LogStepsRequest {
    public String date;
    public int stepCount;

    public LogStepsRequest(String date, int stepCount) {
      this.date = date;
      this.stepCount = stepCount;
    }
  }

  public static class LogWorkoutRequest {
    public String date;
    public String activityType;
    public int durationMinutes;
    public String intensity;

    public LogWorkoutRequest(
        String date, String activityType, int durationMinutes, String intensity) {
      this.date = date;
      this.activityType = activityType;
      this.durationMinutes = durationMinutes;
      this.intensity = intensity;
    }
  }

  // AI Assistant
  public static class AssistantResponse {
    public String answer;
    public String tone;
    public boolean emergencyDetected;
    public String emergencyAdvice;
  }

  public static class AiChatResponseDto {
    public Long conversationId;
    public String reply;
    public String modelName;
    public List<String> toolNames;
    public String safetyVerdict;
  }

  public static class FullChatRequest {
    public Long conversationId;
    public String prompt;

    public FullChatRequest(Long conversationId, String prompt) {
      this.conversationId = conversationId;
      this.prompt = prompt;
    }
  }

  // Community
  public static class CommunityRecipeDto {
    public Long id;
    public String title;
    public String description;
    public String authorUsername;
    public int likeCount;
    public boolean userLiked;
    public int commentCount;
    public double caloriesKcal;
    public double proteinG;
  }

  public static class CommunityCommentDto {
    public Long id;
    public String username;
    public String content;
    public String createdAt;
  }

  public static class CreateCommunityRecipeRequest {
    public String title;
    public String description;
    public int cookTimeMinutes;
    public int servings;
    public List<CommunityIngredientInput> ingredients;
    public List<String> instructions;

    public CreateCommunityRecipeRequest(
        String title,
        String description,
        int cookTimeMinutes,
        int servings,
        List<CommunityIngredientInput> ingredients,
        List<String> instructions) {
      this.title = title;
      this.description = description;
      this.cookTimeMinutes = cookTimeMinutes;
      this.servings = servings;
      this.ingredients = ingredients;
      this.instructions = instructions;
    }
  }

  public static class CommunityIngredientInput {
    public String foodId;
    public double amountG;

    public CommunityIngredientInput(String foodId, double amountG) {
      this.foodId = foodId;
      this.amountG = amountG;
    }
  }

  // Household / Family
  public static class HouseholdDto {
    public Long id;
    public String name;
    public List<HouseholdMemberDto> members;
  }

  public static class HouseholdMemberDto {
    public Long id;
    public String displayName;
    public String relationship;
    public int age;
    public Set<String> allergies;
  }

  public static class AddHouseholdMemberRequest {
    public String displayName;
    public String relationship;
    public int age;
    public String biologicalSex;
    public double heightCm;
    public double weightKg;
    public String dietaryPreference;
    public List<String> allergies;
    public List<String> healthConditions;

    public AddHouseholdMemberRequest(
        String displayName,
        String relationship,
        int age,
        String biologicalSex,
        double heightCm,
        double weightKg,
        String dietaryPreference,
        List<String> allergies,
        List<String> healthConditions) {
      this.displayName = displayName;
      this.relationship = relationship;
      this.age = age;
      this.biologicalSex = biologicalSex;
      this.heightCm = heightCm;
      this.weightKg = weightKg;
      this.dietaryPreference = dietaryPreference;
      this.allergies = allergies;
      this.healthConditions = healthConditions;
    }
  }

  // Gamification
  public static class UserStreakDto {
    public String streakType;
    public int currentStreakDays;
    public int longestStreakDays;
    public boolean graceUsed;
  }

  public static class BadgeDto {
    public String badgeCode;
    public String title;
    public String description;
    public boolean unlocked;
    public String unlockedAt;
  }

  public static class ChallengeDto {
    public Long id;
    public String title;
    public String description;
    public int durationDays;
    public boolean userJoined;
    public int progressPercent;
  }

  // Ingredient match request
  public static class IngredientMatchRequest {
    public List<String> ingredientIds;
    public boolean leftoverMode;

    public IngredientMatchRequest(List<String> ingredientIds, boolean leftoverMode) {
      this.ingredientIds = ingredientIds;
      this.leftoverMode = leftoverMode;
    }
  }

  public static class RecipeMatchResponseDto {
    public List<RecipeMatchItemDto> matches;
  }

  public static class RecipeMatchItemDto {
    public RecipeDetailDto recipe;
    public int matchPercentage;
    public String readinessBadge;
    public List<String> missingIngredients;
  }
}
