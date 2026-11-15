package com.easynutri.data.repository;

import com.easynutri.core.db.AppDatabase;
import com.easynutri.core.db.sync.PendingSyncActionEntity;
import com.easynutri.core.network.ApiService;
import com.easynutri.core.network.SessionManager;
import com.easynutri.data.model.Models.*;
import com.google.gson.Gson;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Singleton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class AppRepository {

  private final ApiService apiService;
  private final SessionManager sessionManager;
  private final AppDatabase database;
  private final ExecutorService executor = Executors.newFixedThreadPool(4);
  private final Gson gson = new Gson();

  private final com.easynutri.data.demo.DemoDataManager demoDataManager;

  @Inject
  public AppRepository(
      ApiService apiService,
      SessionManager sessionManager,
      AppDatabase database,
      com.easynutri.data.demo.DemoDataManager demoDataManager) {
    this.apiService = apiService;
    this.sessionManager = sessionManager;
    this.database = database;
    this.demoDataManager = demoDataManager;
  }

  public com.easynutri.data.demo.DemoDataManager getDemoDataManager() {
    return demoDataManager;
  }

  public SessionManager getSessionManager() {
    return sessionManager;
  }

  public interface DataCallback<T> {
    void onSuccess(T data);

    void onError(String message);
  }

  // --- Auth ---
  public void login(String username, String password, DataCallback<AuthResponse> callback) {
    apiService
        .login(new LoginRequest(username, password))
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                  sessionManager.saveLoginSession(
                      username, username, "ROLE_USER", sessionManager.getSessionCookie());
                  callback.onSuccess(response.body());
                } else {
                  callback.onError(
                      response.body() != null ? response.body().message : "Invalid credentials");
                }
              }

              @Override
              public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError("Connection failed: " + t.getMessage());
              }
            });
  }

  public void register(
      String username,
      String email,
      String password,
      String fullName,
      DataCallback<AuthResponse> callback) {
    apiService
        .register(new RegisterRequest(username, email, password, fullName))
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                  sessionManager.saveLoginSession(
                      username, fullName, "ROLE_USER", sessionManager.getSessionCookie());
                  callback.onSuccess(response.body());
                } else {
                  callback.onError(
                      response.body() != null ? response.body().message : "Registration failed");
                }
              }

              @Override
              public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError("Connection error: " + t.getMessage());
              }
            });
  }

  public void logout(DataCallback<Boolean> callback) {
    apiService
        .logout()
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(
                  Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                sessionManager.clearSession();
                callback.onSuccess(true);
              }

              @Override
              public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                sessionManager.clearSession();
                callback.onSuccess(true);
              }
            });
  }

  // --- Profile ---
  public void getProfile(DataCallback<UserProfileDto> callback) {
    if (sessionManager.isDemoMode() || sessionManager.isGuestMode()) {
      apiService
          .getProfile()
          .enqueue(
              new Callback<>() {
                @Override
                public void onResponse(
                    Call<UserProfileDto> call, Response<UserProfileDto> response) {
                  if (response.isSuccessful() && response.body() != null) {
                    sessionManager.setSensitiveMode(response.body().sensitiveMode);
                    callback.onSuccess(response.body());
                  } else {
                    callback.onSuccess(demoDataManager.getProfile());
                  }
                }

                @Override
                public void onFailure(Call<UserProfileDto> call, Throwable t) {
                  callback.onSuccess(demoDataManager.getProfile());
                }
              });
      return;
    }

    apiService
        .getProfile()
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(Call<UserProfileDto> call, Response<UserProfileDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                  sessionManager.setSensitiveMode(response.body().sensitiveMode);
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Profile not found");
                }
              }

              @Override
              public void onFailure(Call<UserProfileDto> call, Throwable t) {
                callback.onError(t.getMessage());
              }
            });
  }

  public void submitOnboarding(OnboardingRequest request, DataCallback<UserProfileDto> callback) {
    apiService
        .submitOnboarding(request)
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(Call<UserProfileDto> call, Response<UserProfileDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Failed to save onboarding");
                }
              }

              @Override
              public void onFailure(Call<UserProfileDto> call, Throwable t) {
                callback.onError(t.getMessage());
              }
            });
  }

  public void updateProfile(ProfileUpdateRequest request, DataCallback<UserProfileDto> callback) {
    apiService
        .updateProfile(request)
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(Call<UserProfileDto> call, Response<UserProfileDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Failed to update profile");
                }
              }

              @Override
              public void onFailure(Call<UserProfileDto> call, Throwable t) {
                callback.onError(t.getMessage());
              }
            });
  }

  public void toggleSensitiveMode(Boolean enabled, DataCallback<Boolean> callback) {
    apiService
        .toggleSensitiveMode(enabled)
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(
                  Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                  Boolean state = (Boolean) response.body().get("sensitiveMode");
                  boolean active = state != null && state;
                  sessionManager.setSensitiveMode(active);
                  callback.onSuccess(active);
                } else {
                  callback.onError("Could not toggle sensitive mode");
                }
              }

              @Override
              public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                callback.onError(t.getMessage());
              }
            });
  }

  // --- Dashboard ---
  public void getDashboard(DataCallback<DashboardDto> callback) {
    if (sessionManager.isDemoMode() || sessionManager.isGuestMode()) {
      apiService
          .getDashboard()
          .enqueue(
              new Callback<>() {
                @Override
                public void onResponse(Call<DashboardDto> call, Response<DashboardDto> response) {
                  if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                  } else {
                    callback.onSuccess(demoDataManager.getDashboard());
                  }
                }

                @Override
                public void onFailure(Call<DashboardDto> call, Throwable t) {
                  callback.onSuccess(demoDataManager.getDashboard());
                }
              });
      return;
    }

    apiService
        .getDashboard()
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(Call<DashboardDto> call, Response<DashboardDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Failed to load dashboard: " + response.code());
                }
              }

              @Override
              public void onFailure(Call<DashboardDto> call, Throwable t) {
                callback.onError("Offline or server unreachable: " + t.getMessage());
              }
            });
  }

  // --- Foods & Recipes ---
  public void listFoods(String query, String category, DataCallback<List<FoodDto>> callback) {
    apiService
        .listFoods(query, category)
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(Call<List<FoodDto>> call, Response<List<FoodDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Could not fetch foods");
                }
              }

              @Override
              public void onFailure(Call<List<FoodDto>> call, Throwable t) {
                callback.onError(t.getMessage());
              }
            });
  }

  public void listRecipes(String query, String slot, DataCallback<List<RecipeDetailDto>> callback) {
    if (sessionManager.isDemoMode() || sessionManager.isGuestMode()) {
      apiService
          .listRecipes(query, slot)
          .enqueue(
              new Callback<>() {
                @Override
                public void onResponse(
                    Call<List<RecipeDetailDto>> call, Response<List<RecipeDetailDto>> response) {
                  if (response.isSuccessful()
                      && response.body() != null
                      && !response.body().isEmpty()) {
                    callback.onSuccess(response.body());
                  } else {
                    callback.onSuccess(demoDataManager.getAllRecipes());
                  }
                }

                @Override
                public void onFailure(Call<List<RecipeDetailDto>> call, Throwable t) {
                  callback.onSuccess(demoDataManager.getAllRecipes());
                }
              });
      return;
    }

    apiService
        .listRecipes(query, slot)
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(
                  Call<List<RecipeDetailDto>> call, Response<List<RecipeDetailDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Could not fetch recipes");
                }
              }

              @Override
              public void onFailure(Call<List<RecipeDetailDto>> call, Throwable t) {
                callback.onError(t.getMessage());
              }
            });
  }

  public void getRecipe(String id, Integer servings, DataCallback<RecipeDetailDto> callback) {
    if (sessionManager.isDemoMode() || sessionManager.isGuestMode()) {
      RecipeDetailDto local = demoDataManager.getRecipeById(id, servings != null ? servings : 2);
      if (local != null) {
        callback.onSuccess(local);
        return;
      }
    }

    apiService
        .getRecipe(id, servings)
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(
                  Call<RecipeDetailDto> call, Response<RecipeDetailDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  RecipeDetailDto fallback =
                      demoDataManager.getRecipeById(id, servings != null ? servings : 2);
                  if (fallback != null) {
                    callback.onSuccess(fallback);
                  } else {
                    callback.onError("Recipe not found");
                  }
                }
              }

              @Override
              public void onFailure(Call<RecipeDetailDto> call, Throwable t) {
                RecipeDetailDto fallback =
                    demoDataManager.getRecipeById(id, servings != null ? servings : 2);
                if (fallback != null) {
                  callback.onSuccess(fallback);
                } else {
                  callback.onError(t.getMessage());
                }
              }
            });
  }

  public void matchIngredients(
      List<String> ingredientIds,
      boolean leftoverMode,
      DataCallback<RecipeMatchResponseDto> callback) {
    matchIngredients(ingredientIds, 2, "ANY", "ANY", "VEGETARIAN", callback);
  }

  public void matchIngredients(
      List<String> ingredientIds,
      int servings,
      String mealSlot,
      String prepTime,
      String dietPref,
      DataCallback<RecipeMatchResponseDto> callback) {
    List<RecipeMatchItemDto> matches =
        demoDataManager.matchRecipes(ingredientIds, servings, mealSlot, prepTime, dietPref);
    RecipeMatchResponseDto response = new RecipeMatchResponseDto();
    response.matches = matches;
    callback.onSuccess(response);
  }

  // --- Meal Planner ---
  public void getMealPlan(String date, DataCallback<List<MealPlanItemDto>> callback) {
    if (sessionManager.isDemoMode() || sessionManager.isGuestMode()) {
      callback.onSuccess(demoDataManager.getMealPlan());
      return;
    }

    apiService
        .getMealPlan(date)
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(
                  Call<List<MealPlanItemDto>> call, Response<List<MealPlanItemDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onSuccess(demoDataManager.getMealPlan());
                }
              }

              @Override
              public void onFailure(Call<List<MealPlanItemDto>> call, Throwable t) {
                callback.onSuccess(demoDataManager.getMealPlan());
              }
            });
  }

  public void getWeeklyPlan(String weekStart, DataCallback<WeeklyMealPlanResponseDto> callback) {
    apiService
        .getWeeklyPlan(weekStart)
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(
                  Call<WeeklyMealPlanResponseDto> call,
                  Response<WeeklyMealPlanResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Could not load plan");
                }
              }

              @Override
              public void onFailure(Call<WeeklyMealPlanResponseDto> call, Throwable t) {
                callback.onError(t.getMessage());
              }
            });
  }

  public void generateWeeklyPlan(
      String weekStart, DataCallback<WeeklyMealPlanResponseDto> callback) {
    apiService
        .generateWeeklyPlan(weekStart)
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(
                  Call<WeeklyMealPlanResponseDto> call,
                  Response<WeeklyMealPlanResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Plan generation failed");
                }
              }

              @Override
              public void onFailure(Call<WeeklyMealPlanResponseDto> call, Throwable t) {
                callback.onError(t.getMessage());
              }
            });
  }

  public void swapMealPlanItem(
      Long id, String targetRecipeId, DataCallback<MealPlanItemDto> callback) {
    apiService
        .swapMealPlanItem(id, new ReplaceMealRequest(targetRecipeId))
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(
                  Call<MealPlanItemDto> call, Response<MealPlanItemDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Swap failed");
                }
              }

              @Override
              public void onFailure(Call<MealPlanItemDto> call, Throwable t) {
                callback.onError(t.getMessage());
              }
            });
  }

  public void toggleLock(Long id, DataCallback<MealPlanItemDto> callback) {
    apiService
        .toggleLock(id)
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(
                  Call<MealPlanItemDto> call, Response<MealPlanItemDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Could not lock slot");
                }
              }

              @Override
              public void onFailure(Call<MealPlanItemDto> call, Throwable t) {
                callback.onError(t.getMessage());
              }
            });
  }

  public void toggleComplete(Long id, DataCallback<MealPlanItemDto> callback) {
    apiService
        .toggleComplete(id)
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(
                  Call<MealPlanItemDto> call, Response<MealPlanItemDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Could not toggle meal completion");
                }
              }

              @Override
              public void onFailure(Call<MealPlanItemDto> call, Throwable t) {
                callback.onError(t.getMessage());
              }
            });
  }

  // --- Pantry ---
  public void getPantry(DataCallback<List<PantryItemDto>> callback) {
    if (sessionManager.isDemoMode() || sessionManager.isGuestMode()) {
      callback.onSuccess(demoDataManager.getPantryItems());
      return;
    }

    apiService
        .getPantry()
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(
                  Call<List<PantryItemDto>> call, Response<List<PantryItemDto>> response) {
                if (response.isSuccessful()
                    && response.body() != null
                    && !response.body().isEmpty()) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onSuccess(demoDataManager.getPantryItems());
                }
              }

              @Override
              public void onFailure(Call<List<PantryItemDto>> call, Throwable t) {
                callback.onSuccess(demoDataManager.getPantryItems());
              }
            });
  }

  public void addPantryItem(AddPantryItemRequest request, DataCallback<PantryItemDto> callback) {
    apiService
        .addPantryItem(request)
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(Call<PantryItemDto> call, Response<PantryItemDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Failed to add pantry item");
                }
              }

              @Override
              public void onFailure(Call<PantryItemDto> call, Throwable t) {
                // Queue offline
                queueOfflineAction("ADJUST_PANTRY", gson.toJson(request));
                callback.onError("Saved offline: Will sync when connected");
              }
            });
  }

  public void adjustPantryQuantity(Long id, double delta, DataCallback<Boolean> callback) {
    apiService
        .adjustPantryQuantity(id, delta)
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(
                  Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                callback.onSuccess(true);
              }

              @Override
              public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                callback.onError(t.getMessage());
              }
            });
  }

  // --- Grocery ---
  public void getGroceryItems(DataCallback<List<GroceryItemDto>> callback) {
    apiService
        .getGroceryItems()
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(
                  Call<List<GroceryItemDto>> call, Response<List<GroceryItemDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Could not load grocery list");
                }
              }

              @Override
              public void onFailure(Call<List<GroceryItemDto>> call, Throwable t) {
                callback.onError(t.getMessage());
              }
            });
  }

  public void toggleGroceryItem(Long id, DataCallback<GroceryItemDto> callback) {
    apiService
        .toggleGroceryItem(id)
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(Call<GroceryItemDto> call, Response<GroceryItemDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Toggle failed");
                }
              }

              @Override
              public void onFailure(Call<GroceryItemDto> call, Throwable t) {
                callback.onError(t.getMessage());
              }
            });
  }

  public void transferGroceryToPantry(DataCallback<Boolean> callback) {
    apiService
        .transferGroceryToPantry()
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(
                  Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                callback.onSuccess(true);
              }

              @Override
              public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                callback.onError(t.getMessage());
              }
            });
  }

  public void addPantryItem(
      String name,
      double quantity,
      String unit,
      int daysUntilExpiry,
      String storageArea,
      DataCallback<PantryItemDto> callback) {
    addPantryItem(
        new AddPantryItemRequest(name, quantity, unit, storageArea, "2026-12-31"), callback);
  }

  public void consumePantryItem(Long id, DataCallback<Boolean> callback) {
    apiService
        .consumePantryItem(id)
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(
                  Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                callback.onSuccess(true);
              }

              @Override
              public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                callback.onError(t.getMessage());
              }
            });
  }

  public void addGroceryItem(
      String name,
      double quantity,
      String unit,
      String category,
      DataCallback<GroceryItemDto> callback) {
    apiService
        .addGroceryItem(new AddGroceryItemRequest(name, quantity, unit, category))
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(Call<GroceryItemDto> call, Response<GroceryItemDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Failed to add grocery item");
                }
              }

              @Override
              public void onFailure(Call<GroceryItemDto> call, Throwable t) {
                callback.onError(t.getMessage());
              }
            });
  }

  public void deleteGroceryItem(Long id, DataCallback<Boolean> callback) {
    apiService
        .deleteGroceryItem(id)
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(
                  Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                callback.onSuccess(true);
              }

              @Override
              public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                callback.onError(t.getMessage());
              }
            });
  }

  // --- Diary ---
  public void getDayDiary(String date, DataCallback<DiaryDayDto> callback) {
    if (sessionManager.isDemoMode() || sessionManager.isGuestMode()) {
      callback.onSuccess(demoDataManager.getDayDiary());
      return;
    }

    apiService
        .getDayDiary(date)
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(Call<DiaryDayDto> call, Response<DiaryDayDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onSuccess(demoDataManager.getDayDiary());
                }
              }

              @Override
              public void onFailure(Call<DiaryDayDto> call, Throwable t) {
                callback.onSuccess(demoDataManager.getDayDiary());
              }
            });
  }

  public void logDiaryItem(LogFoodItemRequest request, DataCallback<DiaryItemDto> callback) {
    apiService
        .logDiaryItem(request)
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(Call<DiaryItemDto> call, Response<DiaryItemDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Failed to log food");
                }
              }

              @Override
              public void onFailure(Call<DiaryItemDto> call, Throwable t) {
                queueOfflineAction("LOG_DIARY", gson.toJson(request));
                callback.onError("Saved offline: queued for synchronization");
              }
            });
  }

  // --- Hydration ---
  public void getHydration(String date, DataCallback<HydrationSummaryDto> callback) {
    apiService
        .getHydration(date)
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(
                  Call<HydrationSummaryDto> call, Response<HydrationSummaryDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Could not load hydration");
                }
              }

              @Override
              public void onFailure(Call<HydrationSummaryDto> call, Throwable t) {
                callback.onError(t.getMessage());
              }
            });
  }

  public void logWater(LogWaterRequest request, DataCallback<WaterLogDto> callback) {
    apiService
        .logWater(request)
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(Call<WaterLogDto> call, Response<WaterLogDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Could not log water");
                }
              }

              @Override
              public void onFailure(Call<WaterLogDto> call, Throwable t) {
                queueOfflineAction("LOG_WATER", gson.toJson(request));
                callback.onError("Saved offline: queued for sync");
              }
            });
  }

  // --- Activity ---
  public void getActivitySummary(String date, DataCallback<ActivitySummaryDto> callback) {
    apiService
        .getActivitySummary(date)
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(
                  Call<ActivitySummaryDto> call, Response<ActivitySummaryDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Could not load activity");
                }
              }

              @Override
              public void onFailure(Call<ActivitySummaryDto> call, Throwable t) {
                callback.onError(t.getMessage());
              }
            });
  }

  public void logSteps(LogStepsRequest request, DataCallback<Boolean> callback) {
    apiService
        .logSteps(request)
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(
                  Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                callback.onSuccess(true);
              }

              @Override
              public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                queueOfflineAction("LOG_STEPS", gson.toJson(request));
                callback.onError("Saved offline: queued for sync");
              }
            });
  }

  public void logWorkout(LogWorkoutRequest request, DataCallback<WorkoutLogDto> callback) {
    apiService
        .logWorkout(request)
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(Call<WorkoutLogDto> call, Response<WorkoutLogDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Could not log workout");
                }
              }

              @Override
              public void onFailure(Call<WorkoutLogDto> call, Throwable t) {
                queueOfflineAction("LOG_WORKOUT", gson.toJson(request));
                callback.onError("Saved offline: queued for sync");
              }
            });
  }

  // --- Cooking ---
  public void startCookingSession(
      String recipeId, Integer servings, DataCallback<CookingSessionDto> callback) {
    apiService
        .startCookingSession(recipeId, servings)
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(
                  Call<CookingSessionDto> call, Response<CookingSessionDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Could not start cooking session");
                }
              }

              @Override
              public void onFailure(Call<CookingSessionDto> call, Throwable t) {
                callback.onError(t.getMessage());
              }
            });
  }

  public void updateCookingStep(
      Long sessionId, int step, DataCallback<CookingSessionDto> callback) {
    apiService
        .updateCookingStep(sessionId, step)
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(
                  Call<CookingSessionDto> call, Response<CookingSessionDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Could not update step");
                }
              }

              @Override
              public void onFailure(Call<CookingSessionDto> call, Throwable t) {
                callback.onError(t.getMessage());
              }
            });
  }

  public void finishCookingSession(
      Long sessionId, FinishCookingRequest request, DataCallback<CookingSessionDto> callback) {
    apiService
        .finishCookingSession(sessionId, request)
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(
                  Call<CookingSessionDto> call, Response<CookingSessionDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Could not finish cooking");
                }
              }

              @Override
              public void onFailure(Call<CookingSessionDto> call, Throwable t) {
                callback.onError(t.getMessage());
              }
            });
  }

  // --- AI Assistant ---
  public void chatAssistant(
      Long conversationId, String prompt, DataCallback<AiChatResponseDto> callback) {
    apiService
        .chatAssistant(new FullChatRequest(conversationId, prompt))
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(
                  Call<AiChatResponseDto> call, Response<AiChatResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("AI assistant unavailable");
                }
              }

              @Override
              public void onFailure(Call<AiChatResponseDto> call, Throwable t) {
                callback.onError(t.getMessage());
              }
            });
  }

  // --- Community ---
  public void getCommunityFeed(
      String feed, String query, DataCallback<List<CommunityRecipeDto>> callback) {
    apiService
        .getCommunityFeed(feed, query)
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(
                  Call<List<CommunityRecipeDto>> call,
                  Response<List<CommunityRecipeDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Could not load community feed");
                }
              }

              @Override
              public void onFailure(Call<List<CommunityRecipeDto>> call, Throwable t) {
                callback.onError(t.getMessage());
              }
            });
  }

  // --- Household ---
  public void getHousehold(DataCallback<HouseholdDto> callback) {
    apiService
        .getHousehold()
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(Call<HouseholdDto> call, Response<HouseholdDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Could not load household");
                }
              }

              @Override
              public void onFailure(Call<HouseholdDto> call, Throwable t) {
                callback.onError(t.getMessage());
              }
            });
  }

  // --- Gamification ---
  public void getStreaks(DataCallback<List<UserStreakDto>> callback) {
    apiService
        .getStreaks()
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(
                  Call<List<UserStreakDto>> call, Response<List<UserStreakDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Could not load streaks");
                }
              }

              @Override
              public void onFailure(Call<List<UserStreakDto>> call, Throwable t) {
                callback.onError(t.getMessage());
              }
            });
  }

  public void getBadges(DataCallback<List<BadgeDto>> callback) {
    apiService
        .getBadges()
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(Call<List<BadgeDto>> call, Response<List<BadgeDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Could not load badges");
                }
              }

              @Override
              public void onFailure(Call<List<BadgeDto>> call, Throwable t) {
                callback.onError(t.getMessage());
              }
            });
  }

  public void getChallenges(DataCallback<List<ChallengeDto>> callback) {
    apiService
        .getChallenges()
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(
                  Call<List<ChallengeDto>> call, Response<List<ChallengeDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Could not load challenges");
                }
              }

              @Override
              public void onFailure(Call<List<ChallengeDto>> call, Throwable t) {
                callback.onError(t.getMessage());
              }
            });
  }

  public void logWater(int amountMl, String date, String note, DataCallback<WaterLogDto> callback) {
    apiService
        .logWater(new LogWaterRequest(date, amountMl))
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(Call<WaterLogDto> call, Response<WaterLogDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Failed to log water");
                }
              }

              @Override
              public void onFailure(Call<WaterLogDto> call, Throwable t) {
                queueOfflineAction("LOG_WATER", gson.toJson(new LogWaterRequest(date, amountMl)));
                WaterLogDto fallback = new WaterLogDto();
                fallback.id = System.currentTimeMillis();
                fallback.amountMl = amountMl;
                fallback.loggedAt = date;
                callback.onSuccess(fallback);
              }
            });
  }

  public void logSteps(int steps, String date, DataCallback<Boolean> callback) {
    apiService
        .logSteps(new LogStepsRequest(date, steps))
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(
                  Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                  callback.onSuccess(true);
                } else {
                  callback.onError("Failed to log steps");
                }
              }

              @Override
              public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                queueOfflineAction("LOG_STEPS", gson.toJson(new LogStepsRequest(date, steps)));
                callback.onSuccess(true);
              }
            });
  }

  public void logWorkout(
      String type,
      int durationMinutes,
      String date,
      String note,
      DataCallback<WorkoutLogDto> callback) {
    LogWorkoutRequest req = new LogWorkoutRequest(date, type, durationMinutes, "MODERATE");
    apiService
        .logWorkout(req)
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(Call<WorkoutLogDto> call, Response<WorkoutLogDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Failed to log workout");
                }
              }

              @Override
              public void onFailure(Call<WorkoutLogDto> call, Throwable t) {
                queueOfflineAction("LOG_WORKOUT", gson.toJson(req));
                WorkoutLogDto fallback = new WorkoutLogDto();
                fallback.id = System.currentTimeMillis();
                fallback.activityType = type;
                fallback.durationMinutes = durationMinutes;
                fallback.caloriesBurned = durationMinutes * 7.0;
                callback.onSuccess(fallback);
              }
            });
  }

  public void askAssistant(String prompt, DataCallback<AssistantResponse> callback) {
    Map<String, String> body = Map.of("prompt", prompt, "message", prompt);
    apiService
        .askAssistant(body)
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(
                  Call<AssistantResponse> call, Response<AssistantResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Assistant unavailable");
                }
              }

              @Override
              public void onFailure(Call<AssistantResponse> call, Throwable t) {
                AssistantResponse resp = new AssistantResponse();
                resp.answer =
                    "Clinical Wellness Guidance: EasyNutri prioritizes balanced nutrition, whole grains, and proper hydration.";
                callback.onSuccess(resp);
              }
            });
  }

  public void toggleLikeCommunityRecipe(Long recipeId, DataCallback<Boolean> callback) {
    apiService
        .toggleLikeCommunityRecipe(recipeId)
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(
                  Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                callback.onSuccess(true);
              }

              @Override
              public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                callback.onError(t.getMessage());
              }
            });
  }

  public void createCommunityRecipe(
      String title,
      String description,
      int calories,
      List<String> tags,
      List<String> instructions,
      DataCallback<CommunityRecipeDto> callback) {
    CreateCommunityRecipeRequest req =
        new CreateCommunityRecipeRequest(
            title, description, 20, 2, Collections.emptyList(), instructions);
    apiService
        .createCommunityRecipe(req)
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(
                  Call<CommunityRecipeDto> call, Response<CommunityRecipeDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Failed to submit community recipe");
                }
              }

              @Override
              public void onFailure(Call<CommunityRecipeDto> call, Throwable t) {
                callback.onError(t.getMessage());
              }
            });
  }

  public void addHouseholdMember(
      String name,
      int targetCalories,
      List<String> allergies,
      List<String> dietaryPreferences,
      DataCallback<HouseholdMemberDto> callback) {
    String pref =
        dietaryPreferences != null && !dietaryPreferences.isEmpty()
            ? dietaryPreferences.get(0)
            : "BALANCED";
    AddHouseholdMemberRequest req =
        new AddHouseholdMemberRequest(
            name, "Family", 30, "BALANCED", 165.0, 60.0, pref, allergies, Collections.emptyList());
    apiService
        .addHouseholdMember(req)
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(
                  Call<HouseholdMemberDto> call, Response<HouseholdMemberDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Could not add household member");
                }
              }

              @Override
              public void onFailure(Call<HouseholdMemberDto> call, Throwable t) {
                callback.onError(t.getMessage());
              }
            });
  }

  public void finishCookingSession(
      Long sessionId, boolean deductPantry, DataCallback<CookingSessionDto> callback) {
    apiService
        .finishCookingSession(sessionId, new FinishCookingRequest(deductPantry, true))
        .enqueue(
            new Callback<CookingSessionDto>() {
              @Override
              public void onResponse(
                  Call<CookingSessionDto> call, Response<CookingSessionDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Failed to finish cooking session");
                }
              }

              @Override
              public void onFailure(Call<CookingSessionDto> call, Throwable t) {
                CookingSessionDto fallback = new CookingSessionDto();
                fallback.sessionId = sessionId;
                fallback.isCompleted = true;
                callback.onSuccess(fallback);
              }
            });
  }

  public void logDiaryItem(
      String slot,
      String foodName,
      double quantity,
      String unit,
      int calories,
      double protein,
      double carbs,
      double fat,
      String date,
      DataCallback<DiaryItemDto> callback) {
    LogFoodItemRequest req = new LogFoodItemRequest(date, slot, foodName, null, quantity);
    apiService
        .logDiaryItem(req)
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(Call<DiaryItemDto> call, Response<DiaryItemDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Failed to log food item");
                }
              }

              @Override
              public void onFailure(Call<DiaryItemDto> call, Throwable t) {
                queueOfflineAction("LOG_DIARY", gson.toJson(req));
                DiaryItemDto fallback = new DiaryItemDto();
                fallback.id = System.currentTimeMillis();
                fallback.name = foodName;
                fallback.calories = calories;
                callback.onSuccess(fallback);
              }
            });
  }

  public void deleteDiaryItem(Long id, DataCallback<Boolean> callback) {
    apiService
        .deleteDiaryItem(id)
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(
                  Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                callback.onSuccess(true);
              }

              @Override
              public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                callback.onError(t.getMessage());
              }
            });
  }

  public void matchIngredients(
      List<String> ingredientIds, DataCallback<RecipeMatchResponseDto> callback) {
    apiService
        .matchIngredients(new IngredientMatchRequest(ingredientIds, true))
        .enqueue(
            new Callback<>() {
              @Override
              public void onResponse(
                  Call<RecipeMatchResponseDto> call, Response<RecipeMatchResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                  callback.onSuccess(response.body());
                } else {
                  callback.onError("Could not match ingredients");
                }
              }

              @Override
              public void onFailure(Call<RecipeMatchResponseDto> call, Throwable t) {
                callback.onError(t.getMessage());
              }
            });
  }

  // --- Offline Queueing ---
  public void queueOfflineAction(String actionType, String payloadJson) {
    executor.execute(
        () -> {
          PendingSyncActionEntity action =
              new PendingSyncActionEntity(UUID.randomUUID().toString(), actionType, payloadJson);
          database.syncQueueDao().insert(action);
        });
  }
}
