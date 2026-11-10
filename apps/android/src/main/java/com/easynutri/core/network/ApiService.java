package com.easynutri.core.network;

import com.easynutri.data.model.Models.*;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

  // Auth
  @POST("api/v1/auth/register")
  Call<AuthResponse> register(@Body RegisterRequest request);

  @POST("api/v1/auth/login")
  Call<AuthResponse> login(@Body LoginRequest request);

  @POST("api/v1/auth/logout")
  Call<Map<String, Object>> logout();

  @GET("api/v1/auth/me")
  Call<UserInfoDto> getCurrentUser();

  // Profile & Onboarding
  @GET("api/v1/profile")
  Call<UserProfileDto> getProfile();

  @POST("api/v1/profile/onboarding")
  Call<UserProfileDto> submitOnboarding(@Body OnboardingRequest request);

  @PUT("api/v1/profile")
  Call<UserProfileDto> updateProfile(@Body ProfileUpdateRequest request);

  @POST("api/v1/profile/sensitive-mode")
  Call<Map<String, Object>> toggleSensitiveMode(@Query("enabled") Boolean enabled);

  // Foods
  @GET("api/v1/foods")
  Call<List<FoodDto>> listFoods(@Query("q") String query, @Query("category") String category);

  @GET("api/v1/foods/{id}")
  Call<FoodDto> getFood(@Path("id") String id);

  @GET("api/v1/foods/categories")
  Call<List<String>> getFoodCategories();

  // Recipes
  @GET("api/v1/recipes")
  Call<List<RecipeDetailDto>> listRecipes(@Query("q") String query, @Query("slot") String slot);

  @GET("api/v1/recipes/{id}")
  Call<RecipeDetailDto> getRecipe(@Path("id") String id, @Query("servings") Integer servings);

  // Dashboard
  @GET("api/v1/dashboard")
  Call<DashboardDto> getDashboard();

  // Meal Plan
  @GET("api/v1/meal-plan")
  Call<List<MealPlanItemDto>> getMealPlan(@Query("date") String date);

  @GET("api/v1/meal-plan/weekly")
  Call<WeeklyMealPlanResponseDto> getWeeklyPlan(@Query("weekStart") String weekStart);

  @POST("api/v1/meal-plan/generate")
  Call<WeeklyMealPlanResponseDto> generateWeeklyPlan(@Query("weekStart") String weekStart);

  @POST("api/v1/meal-plan")
  Call<MealPlanItemDto> addMealPlanItem(@Body AddMealPlanRequest request);

  @POST("api/v1/meal-plan/{id}/swap")
  Call<MealPlanItemDto> swapMealPlanItem(@Path("id") Long id, @Body ReplaceMealRequest request);

  @POST("api/v1/meal-plan/{id}/lock")
  Call<MealPlanItemDto> toggleLock(@Path("id") Long id);

  @POST("api/v1/meal-plan/{id}/complete")
  Call<MealPlanItemDto> toggleComplete(@Path("id") Long id);

  // Pantry
  @GET("api/v1/pantry")
  Call<List<PantryItemDto>> getPantry();

  @POST("api/v1/pantry")
  Call<PantryItemDto> addPantryItem(@Body AddPantryItemRequest request);

  @PUT("api/v1/pantry/{id}")
  Call<PantryItemDto> updatePantryItem(@Path("id") Long id, @Body UpdatePantryItemRequest request);

  @POST("api/v1/pantry/{id}/adjust")
  Call<Map<String, Object>> adjustPantryQuantity(@Path("id") Long id, @Query("delta") double delta);

  @POST("api/v1/pantry/{id}/consume")
  Call<Map<String, Object>> consumePantryItem(@Path("id") Long id);

  @DELETE("api/v1/pantry/{id}")
  Call<Map<String, Object>> deletePantryItem(@Path("id") Long id);

  // Grocery
  @GET("api/v1/grocery")
  Call<List<GroceryItemDto>> getGroceryItems();

  @POST("api/v1/grocery/add")
  Call<GroceryItemDto> addGroceryItem(@Body AddGroceryItemRequest request);

  @POST("api/v1/grocery/{id}/toggle")
  Call<GroceryItemDto> toggleGroceryItem(@Path("id") Long id);

  @DELETE("api/v1/grocery/{id}")
  Call<Map<String, Object>> deleteGroceryItem(@Path("id") Long id);

  @POST("api/v1/grocery/transfer-to-pantry")
  Call<Map<String, Object>> transferGroceryToPantry();

  // Cooking
  @POST("api/v1/cook/{recipeId}/start")
  Call<CookingSessionDto> startCookingSession(
      @Path("recipeId") String recipeId, @Query("servings") Integer servings);

  @GET("api/v1/cook/{sessionId}")
  Call<CookingSessionDto> getCookingSession(@Path("sessionId") Long sessionId);

  @POST("api/v1/cook/{sessionId}/step")
  Call<CookingSessionDto> updateCookingStep(
      @Path("sessionId") Long sessionId, @Query("step") int step);

  @POST("api/v1/cook/{sessionId}/finish")
  Call<CookingSessionDto> finishCookingSession(
      @Path("sessionId") Long sessionId, @Body FinishCookingRequest request);

  // Diary
  @GET("api/v1/diary")
  Call<DiaryDayDto> getDayDiary(@Query("date") String date);

  @POST("api/v1/diary/items")
  Call<DiaryItemDto> logDiaryItem(@Body LogFoodItemRequest request);

  @DELETE("api/v1/diary/items/{id}")
  Call<Map<String, Object>> deleteDiaryItem(@Path("id") Long id);

  // Hydration
  @GET("api/v1/hydration")
  Call<HydrationSummaryDto> getHydration(@Query("date") String date);

  @POST("api/v1/hydration/logs")
  Call<WaterLogDto> logWater(@Body LogWaterRequest request);

  // Activity
  @GET("api/v1/activity")
  Call<ActivitySummaryDto> getActivitySummary(@Query("date") String date);

  @POST("api/v1/activity/steps")
  Call<Map<String, Object>> logSteps(@Body LogStepsRequest request);

  @POST("api/v1/activity/workouts")
  Call<WorkoutLogDto> logWorkout(@Body LogWorkoutRequest request);

  // AI Assistant
  @POST("api/v1/assistant/chat")
  Call<AssistantResponse> askAssistant(@Body Map<String, String> request);

  @POST("api/v1/assistant/conversations/chat")
  Call<AiChatResponseDto> chatAssistant(@Body FullChatRequest request);

  // Community
  @GET("api/v1/community/feed")
  Call<List<CommunityRecipeDto>> getCommunityFeed(
      @Query("feed") String feed, @Query("q") String query);

  @GET("api/v1/community/recipes/{id}")
  Call<CommunityRecipeDto> getCommunityRecipe(@Path("id") Long id);

  @POST("api/v1/community/recipes")
  Call<CommunityRecipeDto> createCommunityRecipe(@Body CreateCommunityRecipeRequest request);

  @POST("api/v1/community/recipes/{id}/like")
  Call<Map<String, Object>> toggleLikeCommunityRecipe(@Path("id") Long id);

  @POST("api/v1/community/recipes/{id}/comments")
  Call<CommunityCommentDto> addCommunityComment(
      @Path("id") Long id, @Body Map<String, String> request);

  // Household
  @GET("api/v1/household")
  Call<HouseholdDto> getHousehold();

  @POST("api/v1/household/members")
  Call<HouseholdMemberDto> addHouseholdMember(@Body AddHouseholdMemberRequest request);

  // Gamification
  @GET("api/v1/gamification/streaks")
  Call<List<UserStreakDto>> getStreaks();

  @GET("api/v1/gamification/badges")
  Call<List<BadgeDto>> getBadges();

  @GET("api/v1/gamification/challenges")
  Call<List<ChallengeDto>> getChallenges();

  // Ingredient Matcher
  @POST("api/v1/ingredients/match")
  Call<RecipeMatchResponseDto> matchIngredients(@Body IngredientMatchRequest request);
}
