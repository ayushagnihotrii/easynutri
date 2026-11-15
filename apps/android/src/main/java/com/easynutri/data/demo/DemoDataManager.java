package com.easynutri.data.demo;

import android.content.Context;
import android.content.SharedPreferences;
import com.easynutri.data.model.Models.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

public class DemoDataManager {

  private static final String PREF_NAME = "easynutri_demo_presentation_state";
  private static final String KEY_PANTRY = "demo_pantry_items";
  private static final String KEY_DIARY = "demo_diary_items";
  private static final String KEY_WATER = "demo_water_ml";
  private static final String KEY_STEPS = "demo_steps";
  private static final String KEY_MEAL_PLAN = "demo_meal_plan";
  private static final String KEY_PROFILE = "demo_user_profile";
  private static final String KEY_INITIALIZED = "demo_initialized";

  private static DemoDataManager instance;
  private final SharedPreferences prefs;
  private final Gson gson = new Gson();

  private List<PantryItemDto> pantryList = new ArrayList<>();
  private List<RecipeDetailDto> recipeCatalog = new ArrayList<>();
  private List<MealPlanItemDto> mealPlanList = new ArrayList<>();
  private List<DiaryItemDto> diaryItemList = new ArrayList<>();
  private UserProfileDto profileDto;
  private int currentWaterMl = 1500;
  private int currentSteps = 6420;

  private DemoDataManager(Context context) {
    this.prefs =
        context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    initRecipes();
    if (!prefs.getBoolean(KEY_INITIALIZED, false)) {
      resetToBaseline();
    } else {
      loadFromStorage();
    }
  }

  public static synchronized DemoDataManager getInstance(Context context) {
    if (instance == null) {
      instance = new DemoDataManager(context);
    }
    return instance;
  }

  public synchronized void resetToBaseline() {
    pantryList.clear();
    pantryList.add(
        createPantry(
            1L,
            "food_cooked_rice",
            "White Rice",
            "सफेद चावल",
            1000.0,
            "g",
            "Pantry",
            30,
            "NORMAL"));
    pantryList.add(
        createPantry(
            2L,
            "food_moong_dal",
            "Yellow Moong Dal",
            "मूंग दाल",
            500.0,
            "g",
            "Pantry",
            45,
            "NORMAL"));
    pantryList.add(
        createPantry(
            3L, "food_toor_dal", "Toor Dal", "तूर दाल", 500.0, "g", "Pantry", 60, "NORMAL"));
    pantryList.add(
        createPantry(
            4L,
            "food_spinach_palak",
            "Fresh Spinach (Palak)",
            "ताजा पालक",
            250.0,
            "g",
            "Refrigerator",
            2,
            "URGENT"));
    pantryList.add(
        createPantry(
            5L,
            "food_tomato",
            "Fresh Tomatoes",
            "टमाटर",
            500.0,
            "g",
            "Vegetable Basket",
            3,
            "EXPIRING_SOON"));
    pantryList.add(
        createPantry(6L, "food_onion", "Onions", "प्याज", 1000.0, "g", "Pantry", 10, "NORMAL"));
    pantryList.add(
        createPantry(
            7L, "food_potato_aloo", "Potatoes (Aloo)", "आलू", 1000.0, "g", "Pantry", 14, "NORMAL"));
    pantryList.add(
        createPantry(
            8L,
            "food_paneer",
            "Fresh Paneer",
            "ताजा पनीर",
            200.0,
            "g",
            "Refrigerator",
            4,
            "NORMAL"));
    pantryList.add(
        createPantry(
            9L,
            "food_cow_milk",
            "Fresh Cow Milk",
            "गाय का दूध",
            1000.0,
            "ml",
            "Refrigerator",
            2,
            "URGENT"));
    pantryList.add(
        createPantry(
            10L,
            "food_rolled_oats",
            "Rolled Oats",
            "रोल्ड ओट्स",
            500.0,
            "g",
            "Pantry",
            90,
            "NORMAL"));
    pantryList.add(
        createPantry(
            11L,
            "food_banana",
            "Fresh Bananas",
            "केला",
            6.0,
            "pcs",
            "Fruit Bowl",
            3,
            "EXPIRING_SOON"));
    pantryList.add(
        createPantry(
            12L,
            "food_wheat_flour",
            "Whole Wheat Flour (Atta)",
            "गेहूं का आटा",
            2000.0,
            "g",
            "Pantry",
            60,
            "NORMAL"));
    pantryList.add(
        createPantry(
            13L,
            "food_curd_dahi",
            "Fresh Curd (Dahi)",
            "ताजा दही",
            400.0,
            "g",
            "Refrigerator",
            2,
            "URGENT"));
    pantryList.add(
        createPantry(
            14L,
            "food_cumin_seeds",
            "Cumin & Spices (Jeera, Haldi)",
            "जीरा और भारतीय मसाले",
            150.0,
            "g",
            "Spice Box",
            180,
            "NORMAL"));

    profileDto = new UserProfileDto();
    profileDto.id = 101L;
    profileDto.username = "demo";
    profileDto.age = 28;
    profileDto.biologicalSex = "MALE";
    profileDto.heightCm = 175.0;
    profileDto.weightKg = 70.0;
    profileDto.activityLevel = "MODERATELY_ACTIVE";
    profileDto.healthGoal = "MAINTAIN";
    profileDto.dietaryPreference = "VEGETARIAN";
    profileDto.sensitiveMode = false;
    profileDto.onboardingCompleted = true;
    profileDto.targetCalories = 2000;
    profileDto.targetProteinG = 75;
    profileDto.targetCarbG = 250;
    profileDto.targetFatG = 55;
    profileDto.targetWaterL = 2.5;
    profileDto.allergies = new HashSet<>();
    profileDto.healthConditions = new HashSet<>();

    currentWaterMl = 1500;
    currentSteps = 6420;

    diaryItemList.clear();
    DiaryItemDto breakfast = new DiaryItemDto();
    breakfast.id = 1L;
    breakfast.recipeId = "recipe_veg_poha";
    breakfast.name = "Vegetable Poha (Breakfast)";
    breakfast.amountG = 250.0;
    breakfast.calories = 340.0;
    breakfast.proteinG = 8.5;
    breakfast.carbsG = 58.0;
    breakfast.fatG = 7.0;
    diaryItemList.add(breakfast);

    mealPlanList.clear();
    mealPlanList.add(
        createMealPlan(1L, "BREAKFAST", getRecipeByIdInternal("recipe_veg_poha"), 2, true, true));
    mealPlanList.add(
        createMealPlan(
            2L, "LUNCH", getRecipeByIdInternal("recipe_moong_khichdi"), 2, false, false));
    mealPlanList.add(
        createMealPlan(
            3L, "SNACK", getRecipeByIdInternal("recipe_oats_porridge"), 1, false, false));
    mealPlanList.add(
        createMealPlan(
            4L, "DINNER", getRecipeByIdInternal("recipe_palak_dal_rice"), 2, false, false));

    saveToStorage();
    prefs.edit().putBoolean(KEY_INITIALIZED, true).apply();
  }

  private void saveToStorage() {
    prefs
        .edit()
        .putString(KEY_PANTRY, gson.toJson(pantryList))
        .putString(KEY_DIARY, gson.toJson(diaryItemList))
        .putString(KEY_MEAL_PLAN, gson.toJson(mealPlanList))
        .putString(KEY_PROFILE, gson.toJson(profileDto))
        .putInt(KEY_WATER, currentWaterMl)
        .putInt(KEY_STEPS, currentSteps)
        .apply();
  }

  private void loadFromStorage() {
    try {
      Type pantryType = new TypeToken<List<PantryItemDto>>() {}.getType();
      String pJson = prefs.getString(KEY_PANTRY, null);
      if (pJson != null) pantryList = gson.fromJson(pJson, pantryType);

      Type diaryType = new TypeToken<List<DiaryItemDto>>() {}.getType();
      String dJson = prefs.getString(KEY_DIARY, null);
      if (dJson != null) diaryItemList = gson.fromJson(dJson, diaryType);

      Type planType = new TypeToken<List<MealPlanItemDto>>() {}.getType();
      String mpJson = prefs.getString(KEY_MEAL_PLAN, null);
      if (mpJson != null) mealPlanList = gson.fromJson(mpJson, planType);

      String profJson = prefs.getString(KEY_PROFILE, null);
      if (profJson != null) profileDto = gson.fromJson(profJson, UserProfileDto.class);

      currentWaterMl = prefs.getInt(KEY_WATER, 1500);
      currentSteps = prefs.getInt(KEY_STEPS, 6420);
    } catch (Exception e) {
      resetToBaseline();
    }
  }

  private PantryItemDto createPantry(
      Long id,
      String foodId,
      String nameEn,
      String nameHi,
      double qty,
      String unit,
      String loc,
      int days,
      String urgency) {
    PantryItemDto item = new PantryItemDto();
    item.id = id;
    item.foodId = foodId;
    item.foodNameEn = nameEn;
    item.foodNameHi = nameHi;
    item.quantityG = qty;
    item.unit = unit;
    item.storageLocation = loc;
    item.daysUntilExpiry = days;
    item.expiryUrgency = urgency;
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DAY_OF_YEAR, days);
    item.expirationDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.getTime());
    return item;
  }

  private MealPlanItemDto createMealPlan(
      Long id,
      String slot,
      RecipeDetailDto recipe,
      int servings,
      boolean locked,
      boolean completed) {
    MealPlanItemDto mp = new MealPlanItemDto();
    mp.id = id;
    mp.mealSlot = slot;
    mp.recipe = recipe;
    mp.servings = servings;
    mp.locked = locked;
    mp.completed = completed;
    mp.plannedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    return mp;
  }

  private void initRecipes() {
    recipeCatalog.clear();

    // 1. Moong Dal Khichdi
    recipeCatalog.add(
        buildRecipe(
            "recipe_moong_khichdi",
            "Moong Dal Khichdi",
            "सादी मूंग दाल खिचड़ी",
            "Soothing, comforting one-pot Indian meal combining protein-rich yellow lentils, rice, and aromatic cumin.",
            "DINNER",
            2,
            10,
            20,
            340.0,
            14.0,
            62.0,
            4.5,
            6.0,
            List.of("high_protein", "easy_digest", "comfort_food", "vegetarian"),
            List.of(
                "1. Wash yellow moong dal and white rice together until water runs clear; drain.",
                "2. Heat 1 tsp oil/ghee in a pressure cooker; splutter cumin seeds and add a pinch of turmeric and salt.",
                "3. Add the washed dal and rice, pour 3 cups of water, and stir well.",
                "4. Close cooker lid and pressure cook on medium heat for 3 whistles.",
                "5. Release pressure naturally, fluff gently, and serve steaming hot with fresh homemade curd."),
            List.of(
                ing("food_cooked_rice", "White Rice", "चावल", 120.0, false),
                ing("food_moong_dal", "Yellow Moong Dal", "मूंग दाल", 100.0, false),
                ing("food_cumin_seeds", "Cumin Seeds (Jeera)", "जीरा", 4.0, true),
                ing("food_turmeric_powder", "Turmeric Powder (Haldi)", "हल्दी", 2.0, true),
                ing("food_curd_dahi", "Fresh Curd (Dahi)", "दही", 60.0, true))));

    // 2. Palak Toor Dal with Steamed Rice
    recipeCatalog.add(
        buildRecipe(
            "recipe_palak_dal_rice",
            "Palak Dal with Steamed Rice",
            "पालक दाल और उबले चावल",
            "Iron-dense everyday yellow lentil stew enriched with freshly chopped spinach leaves and served with rice.",
            "LUNCH",
            2,
            15,
            25,
            380.0,
            16.5,
            68.0,
            5.0,
            8.5,
            List.of("high_protein", "iron_rich", "high_fiber", "vegetarian"),
            List.of(
                "1. Rinse toor dal and pressure cook with 3 cups water until tender (approx 3 whistles).",
                "2. In a pan, heat 1 tsp oil, add cumin seeds, chopped onions, and sauté until golden.",
                "3. Add chopped fresh tomatoes and simmer until soft and pulpy.",
                "4. Stir in finely chopped fresh spinach (palak) leaves and cook for 2 minutes.",
                "5. Pour cooked toor dal into the pan, season with salt, and simmer for 5 minutes.",
                "6. Serve hot over a bed of warm steamed white rice."),
            List.of(
                ing("food_toor_dal", "Toor Dal", "तूर दाल", 120.0, false),
                ing("food_spinach_palak", "Fresh Spinach", "पालक", 100.0, false),
                ing("food_cooked_rice", "Steamed Rice", "चावल", 120.0, false),
                ing("food_tomato", "Fresh Tomato", "टमाटर", 60.0, false),
                ing("food_onion", "Onion", "प्याज", 50.0, true),
                ing("food_cumin_seeds", "Cumin Seeds", "जीरा", 3.0, true))));

    // 3. Vegetable Pulao
    recipeCatalog.add(
        buildRecipe(
            "recipe_veg_pulao",
            "Vegetable Pulao",
            "सब्जी पुलाव",
            "Fragrant basmati rice gently cooked with potatoes, onions, tomatoes, and whole cumin spices.",
            "LUNCH",
            2,
            10,
            20,
            310.0,
            7.5,
            64.0,
            4.0,
            5.2,
            List.of("vegetarian", "gluten_free", "comfort_food", "balanced"),
            List.of(
                "1. Rinse and soak rice for 10 minutes; drain.",
                "2. In a heavy pot, heat 1 tsp oil, add cumin seeds, sliced onions, and sauté until translucent.",
                "3. Add diced potatoes and tomatoes with a pinch of turmeric and salt; sauté 2 minutes.",
                "4. Add soaked rice with 2 cups of water. Cover and simmer on low heat for 15 minutes.",
                "5. Let rest covered for 5 minutes, fluff with a fork, and serve warm."),
            List.of(
                ing("food_cooked_rice", "White Rice", "चावल", 150.0, false),
                ing("food_potato_aloo", "Potatoes (Aloo)", "आलू", 100.0, false),
                ing("food_onion", "Onion", "प्याज", 60.0, false),
                ing("food_tomato", "Tomato", "टमाटर", 60.0, false),
                ing("food_cumin_seeds", "Cumin Seeds", "जीरा", 4.0, true))));

    // 4. Paneer Bhurji with Roti
    recipeCatalog.add(
        buildRecipe(
            "recipe_paneer_bhurji_roti",
            "Paneer Bhurji with Roti",
            "पनीर भुर्जी और रोटी",
            "High-protein scrambled fresh paneer tossed with turmeric, spinach, tomatoes, and warm wheat rotis.",
            "LUNCH",
            2,
            15,
            15,
            420.0,
            22.0,
            48.0,
            16.0,
            7.0,
            List.of("high_protein", "vegetarian", "quick_cook"),
            List.of(
                "1. Crumble fresh cottage cheese (paneer) lightly by hand.",
                "2. Heat 1 tsp oil in a non-stick pan, sauté chopped onions and tomatoes until tender.",
                "3. Add shredded spinach, turmeric powder, and salt; stir for 1 minute.",
                "4. Fold in crumbled paneer and warm through on low flame for 3 minutes.",
                "5. Serve immediately with warm freshly made whole wheat rotis."),
            List.of(
                ing("food_paneer", "Fresh Paneer", "पनीर", 160.0, false),
                ing("food_wheat_flour", "Whole Wheat Flour (Roti)", "गेहूं का आटा", 100.0, false),
                ing("food_onion", "Onion", "प्याज", 50.0, false),
                ing("food_tomato", "Tomato", "टमाटर", 50.0, false),
                ing("food_spinach_palak", "Fresh Spinach", "पालक", 40.0, true))));

    // 5. Vegetable Poha
    recipeCatalog.add(
        buildRecipe(
            "recipe_veg_poha",
            "Vegetable Poha",
            "कांदा बटाटा पोहा",
            "Light, wholesome flattened rice stir-fried with diced potatoes, onions, mustard seeds, and turmeric.",
            "BREAKFAST",
            2,
            10,
            12,
            280.0,
            6.8,
            54.0,
            4.2,
            4.0,
            List.of("quick_cook", "breakfast", "vegetarian", "low_calorie"),
            List.of(
                "1. Gently rinse flattened rice (poha) in a colander until softened; drain well.",
                "2. Heat 1 tsp oil in a pan, add mustard seeds and let crackle.",
                "3. Add finely diced onions and potatoes; cook covered on medium until potatoes are tender.",
                "4. Stir in turmeric powder and salt, then gently fold in rinsed poha for 3 minutes.",
                "5. Garnish with a squeeze of fresh lemon and serve warm."),
            List.of(
                ing("food_poha_flattened_rice", "Flattened Rice (Poha)", "पोहा", 120.0, false),
                ing("food_potato_aloo", "Potatoes (Aloo)", "आलू", 80.0, false),
                ing("food_onion", "Onion", "प्याज", 50.0, false),
                ing("food_mustard_seeds", "Mustard Seeds", "राई", 3.0, true),
                ing("food_turmeric_powder", "Turmeric Powder", "हल्दी", 2.0, true))));

    // 6. Warm Oats Porridge with Banana
    recipeCatalog.add(
        buildRecipe(
            "recipe_oats_porridge",
            "Oats Porridge with Milk & Banana",
            "दूध और केले का ओट्स दलिया",
            "Heart-healthy creamy rolled oats simmered in warm cow milk and naturally sweetened with ripe banana.",
            "BREAKFAST",
            1,
            5,
            8,
            310.0,
            11.5,
            52.0,
            6.8,
            6.5,
            List.of("high_fiber", "calcium_rich", "quick_cook", "vegetarian"),
            List.of(
                "1. Add rolled oats and fresh milk to a small saucepan.",
                "2. Bring to a gentle simmer on medium heat, stirring continuously.",
                "3. Cook for 4-5 minutes until thick, creamy, and fragrant.",
                "4. Pour into a breakfast bowl and top with freshly sliced banana rounds."),
            List.of(
                ing("food_rolled_oats", "Rolled Oats", "ओट्स", 50.0, false),
                ing("food_cow_milk", "Fresh Milk", "दूध", 200.0, false),
                ing("food_banana", "Fresh Banana", "केला", 80.0, false))));

    // 7. Homestyle Dal Tadka with Steamed Rice
    recipeCatalog.add(
        buildRecipe(
            "recipe_dal_tadka",
            "Homestyle Dal Tadka with Rice",
            "दाल तड़का और चावल",
            "Creamy yellow toor lentil soup tempered with cumin seeds, onions, tomatoes, and golden turmeric.",
            "LUNCH",
            2,
            10,
            20,
            360.0,
            15.0,
            65.0,
            5.0,
            7.8,
            List.of("high_protein", "comfort_food", "vegetarian", "gluten_free"),
            List.of(
                "1. Pressure cook toor dal with turmeric and 3 cups of water until completely soft.",
                "2. In a small pan, prepare the tadka: heat oil, add cumin seeds, chopped onions, and sauté until golden.",
                "3. Add chopped tomatoes and cook until soft and aromatic.",
                "4. Pour the sizzling tadka into the cooked dal and simmer for 2 minutes.",
                "5. Serve piping hot with freshly steamed white rice."),
            List.of(
                ing("food_toor_dal", "Toor Dal", "तूर दाल", 120.0, false),
                ing("food_tomato", "Tomato", "टमाटर", 60.0, false),
                ing("food_onion", "Onion", "प्याज", 50.0, false),
                ing("food_cooked_rice", "White Rice", "चावल", 120.0, false),
                ing("food_cumin_seeds", "Cumin Seeds", "जीरा", 4.0, true))));

    // 8. Nutritious Spinach (Palak) Khichdi
    recipeCatalog.add(
        buildRecipe(
            "recipe_spinach_khichdi",
            "Spinach Khichdi",
            "पौष्टिक पालक खिचड़ी",
            "Energizing one-pot blend of fragrant rice, yellow moong lentils, and fresh tender spinach leaves.",
            "DINNER",
            2,
            10,
            20,
            330.0,
            14.5,
            60.0,
            4.0,
            7.5,
            List.of("high_protein", "iron_rich", "comfort_food", "easy_digest"),
            List.of(
                "1. Wash rice and yellow moong dal together; drain thoroughly.",
                "2. In a pressure cooker, heat 1 tsp oil, splutter cumin seeds, and sauté sliced onions.",
                "3. Add freshly chopped spinach leaves and sauté 1 minute until wilted.",
                "4. Add rice, moong dal, salt, turmeric, and 3.5 cups of water.",
                "5. Pressure cook for 3 whistles. Allow steam to release naturally and serve warm with curd."),
            List.of(
                ing("food_cooked_rice", "White Rice", "चावल", 120.0, false),
                ing("food_moong_dal", "Yellow Moong Dal", "मूंग दाल", 100.0, false),
                ing("food_spinach_palak", "Fresh Spinach", "पालक", 80.0, false),
                ing("food_onion", "Onion", "प्याज", 40.0, true),
                ing("food_curd_dahi", "Fresh Curd", "दही", 50.0, true))));
  }

  private RecipeDetailDto buildRecipe(
      String id,
      String titleEn,
      String titleHi,
      String desc,
      String slot,
      int baseServings,
      int prepMin,
      int cookMin,
      double cal,
      double prot,
      double carbs,
      double fat,
      double fiber,
      List<String> tags,
      List<String> steps,
      List<ScaledIngredientDto> ings) {
    RecipeDetailDto r = new RecipeDetailDto();
    r.id = id;
    r.titleEn = titleEn;
    r.titleHi = titleHi;
    r.description = desc;
    r.mealSlot = slot;
    r.defaultServings = baseServings;
    r.prepTimeMinutes = prepMin;
    r.cookTimeMinutes = cookMin;
    r.caloriesKcal = cal;
    r.proteinG = prot;
    r.carbsG = carbs;
    r.fatG = fat;
    r.fiberG = fiber;
    r.dietaryTags = tags;
    r.instructions = steps;
    r.ingredients = ings;
    return r;
  }

  private ScaledIngredientDto ing(
      String foodId, String nameEn, String nameHi, double amountG, boolean optional) {
    ScaledIngredientDto dto = new ScaledIngredientDto();
    dto.foodId = foodId;
    dto.nameEn = nameEn;
    dto.nameHi = nameHi;
    dto.amountG = amountG;
    dto.displayAmount = (int) amountG + "g";
    dto.optional = optional;
    return dto;
  }

  // --- Public APIs for UI & ViewModels ---

  public synchronized List<PantryItemDto> getPantryItems() {
    return new ArrayList<>(pantryList);
  }

  public synchronized void addPantryItem(PantryItemDto item) {
    if (item.id == null) {
      item.id = System.currentTimeMillis();
    }
    pantryList.add(0, item);
    saveToStorage();
  }

  public synchronized void removePantryItem(Long id) {
    pantryList.removeIf(item -> Objects.equals(item.id, id));
    saveToStorage();
  }

  public synchronized void updatePantryQuantity(Long id, double deltaG) {
    for (PantryItemDto item : pantryList) {
      if (Objects.equals(item.id, id)) {
        item.quantityG = Math.max(0.0, item.quantityG + deltaG);
        break;
      }
    }
    saveToStorage();
  }

  public List<String> getSampleIngredientIds() {
    return List.of(
        "food_cooked_rice", "food_moong_dal", "food_spinach_palak", "food_tomato", "food_onion");
  }

  public List<RecipeDetailDto> getAllRecipes() {
    return new ArrayList<>(recipeCatalog);
  }

  public RecipeDetailDto getRecipeById(String recipeId, int servings) {
    RecipeDetailDto base = getRecipeByIdInternal(recipeId);
    if (base == null) return null;
    return scaleRecipe(base, servings);
  }

  private RecipeDetailDto getRecipeByIdInternal(String recipeId) {
    for (RecipeDetailDto r : recipeCatalog) {
      if (r.id.equalsIgnoreCase(recipeId)) return r;
    }
    return recipeCatalog.isEmpty() ? null : recipeCatalog.get(0);
  }

  /**
   * Java-based Serving Scaler & Clinical Recalculator. Scales ingredient gram weights linearly.
   * Total recipe calories scale by ratio; per-serving nutritional density remains exact.
   */
  public RecipeDetailDto scaleRecipe(RecipeDetailDto base, int targetServings) {
    if (targetServings <= 0) targetServings = base.defaultServings;
    double ratio = (double) targetServings / (double) base.defaultServings;

    RecipeDetailDto scaled = new RecipeDetailDto();
    scaled.id = base.id;
    scaled.titleEn = base.titleEn;
    scaled.titleHi = base.titleHi;
    scaled.description = base.description;
    scaled.mealSlot = base.mealSlot;
    scaled.defaultServings = targetServings;
    scaled.prepTimeMinutes = base.prepTimeMinutes;
    scaled.cookTimeMinutes = base.cookTimeMinutes;
    scaled.dietaryTags = base.dietaryTags;
    scaled.instructions = base.instructions;

    // Per-serving values remain clinically consistent
    scaled.caloriesKcal = base.caloriesKcal;
    scaled.proteinG = base.proteinG;
    scaled.carbsG = base.carbsG;
    scaled.fatG = base.fatG;
    scaled.fiberG = base.fiberG;

    // Scale ingredient gram amounts linearly
    List<ScaledIngredientDto> scaledIngs = new ArrayList<>();
    if (base.ingredients != null) {
      for (ScaledIngredientDto orig : base.ingredients) {
        ScaledIngredientDto s = new ScaledIngredientDto();
        s.foodId = orig.foodId;
        s.nameEn = orig.nameEn;
        s.nameHi = orig.nameHi;
        s.amountG = Math.round(orig.amountG * ratio);
        s.displayAmount = (int) s.amountG + "g";
        s.optional = orig.optional;
        scaledIngs.add(s);
      }
    }
    scaled.ingredients = scaledIngs;
    return scaled;
  }

  /**
   * Pure Java Recipe Matcher: Matches selected kitchen ingredients against the canonical recipe
   * catalog. Calculates availability, missing ingredients, match scores, and presentation labels.
   */
  public List<RecipeMatchItemDto> matchRecipes(
      List<String> selectedFoodIds,
      int servings,
      String mealSlot,
      String prepTimeFilter,
      String dietFilter) {
    Set<String> selectedSet = new HashSet<>();
    if (selectedFoodIds != null) {
      for (String id : selectedFoodIds) {
        selectedSet.add(id.trim().toLowerCase(Locale.ROOT));
      }
    }

    List<RecipeMatchItemDto> results = new ArrayList<>();

    for (RecipeDetailDto recipe : recipeCatalog) {
      // 1. Dietary Preference Filter
      if (dietFilter != null && !dietFilter.isEmpty() && !"any".equalsIgnoreCase(dietFilter)) {
        if ("VEGAN".equalsIgnoreCase(dietFilter)) {
          boolean hasDairy =
              recipe.ingredients.stream()
                  .anyMatch(
                      i ->
                          i.foodId.contains("milk")
                              || i.foodId.contains("paneer")
                              || i.foodId.contains("curd"));
          if (hasDairy) continue;
        } else if ("JAIN".equalsIgnoreCase(dietFilter)) {
          boolean hasRoot =
              recipe.ingredients.stream()
                  .anyMatch(i -> i.foodId.contains("onion") || i.foodId.contains("potato"));
          if (hasRoot) continue;
        }
      }

      // 2. Prep Time Filter
      if (prepTimeFilter != null && !prepTimeFilter.isEmpty()) {
        int totalTime = recipe.prepTimeMinutes + recipe.cookTimeMinutes;
        if ("< 15 min".equalsIgnoreCase(prepTimeFilter) && totalTime > 15) continue;
        if ("< 30 min".equalsIgnoreCase(prepTimeFilter) && totalTime > 30) continue;
        if ("< 45 min".equalsIgnoreCase(prepTimeFilter) && totalTime > 45) continue;
      }

      // 3. Match Ingredients
      int totalRequired = 0;
      int matchedCount = 0;
      List<String> missing = new ArrayList<>();

      for (ScaledIngredientDto ing : recipe.ingredients) {
        if (!ing.optional) {
          totalRequired++;
          String norm = ing.foodId.toLowerCase(Locale.ROOT);
          if (selectedSet.contains(norm) || containsLooseMatch(selectedSet, norm)) {
            matchedCount++;
          } else {
            missing.add(ing.nameEn != null ? ing.nameEn : ing.foodId);
          }
        }
      }

      int scorePercent =
          totalRequired > 0
              ? (int) Math.round(((double) matchedCount / totalRequired) * 100.0)
              : 100;
      if (scorePercent > 100) scorePercent = 100;

      RecipeMatchItemDto item = new RecipeMatchItemDto();
      item.recipe = scaleRecipe(recipe, servings);
      item.matchPercentage = scorePercent;
      item.missingIngredients = missing;

      if (missing.isEmpty()) {
        item.readinessBadge = "Exact Match • Pantry Ready";
      } else if (missing.size() == 1) {
        item.readinessBadge = "Made mostly from your pantry";
      } else {
        item.readinessBadge = missing.size() + " Ingredients Missing";
      }

      results.add(item);
    }

    // Sort by match percentage descending, then missing ingredients ascending
    results.sort(
        (a, b) -> {
          int cmp = Integer.compare(b.matchPercentage, a.matchPercentage);
          if (cmp != 0) return cmp;
          return Integer.compare(a.missingIngredients.size(), b.missingIngredients.size());
        });

    return results;
  }

  private boolean containsLooseMatch(Set<String> selectedSet, String targetId) {
    for (String s : selectedSet) {
      if (targetId.contains("rice") && s.contains("rice")) return true;
      if (targetId.contains("dal") && s.contains("dal")) return true;
      if (targetId.contains("spinach") && s.contains("spinach")) return true;
      if (targetId.contains("tomato") && s.contains("tomato")) return true;
      if (targetId.contains("onion") && s.contains("onion")) return true;
      if (targetId.contains("potato") && s.contains("potato")) return true;
      if (targetId.contains("paneer") && s.contains("paneer")) return true;
      if (targetId.contains("milk") && s.contains("milk")) return true;
      if (targetId.contains("oats") && s.contains("oats")) return true;
      if (targetId.contains("poha") && s.contains("poha")) return true;
      if (targetId.contains("curd") && s.contains("curd")) return true;
    }
    return false;
  }

  public synchronized DashboardDto getDashboard() {
    DashboardDto d = new DashboardDto();
    d.profile = profileDto;
    d.targetCalories = profileDto.targetCalories;
    d.consumedCalories = calculateTotalDiaryCalories();
    d.burnedCalories = 320;
    d.netCalories = d.consumedCalories - d.burnedCalories;
    d.targetProteinG = profileDto.targetProteinG;
    d.consumedProteinG = calculateTotalDiaryProtein();
    d.targetCarbG = profileDto.targetCarbG;
    d.consumedCarbG = calculateTotalDiaryCarbs();
    d.targetFatG = profileDto.targetFatG;
    d.consumedFatG = calculateTotalDiaryFat();
    d.targetWaterL = profileDto.targetWaterL;
    d.consumedWaterL = currentWaterMl / 1000.0;
    d.targetSteps = 8000;
    d.currentSteps = currentSteps;
    d.activeStreakDays = 3;
    d.expiringPantryCount = 3;
    d.todayMeals = new ArrayList<>(mealPlanList);
    d.suggestedRecipes =
        new ArrayList<>(recipeCatalog.subList(0, Math.min(3, recipeCatalog.size())));
    return d;
  }

  public synchronized UserProfileDto getProfile() {
    return profileDto;
  }

  public synchronized void setSensitiveMode(boolean enabled) {
    if (profileDto != null) {
      profileDto.sensitiveMode = enabled;
      saveToStorage();
    }
  }

  public synchronized List<MealPlanItemDto> getMealPlan() {
    return new ArrayList<>(mealPlanList);
  }

  public synchronized DiaryDayDto getDayDiary() {
    DiaryDayDto day = new DiaryDayDto();
    day.date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    day.targetCalories = profileDto != null ? profileDto.targetCalories : 2000;
    day.sensitiveMode = profileDto != null && profileDto.sensitiveMode;

    DiarySlotDto breakfastSlot = new DiarySlotDto();
    breakfastSlot.mealSlot = "BREAKFAST";
    breakfastSlot.items = new ArrayList<>();

    DiarySlotDto lunchSlot = new DiarySlotDto();
    lunchSlot.mealSlot = "LUNCH";
    lunchSlot.items = new ArrayList<>();

    DiarySlotDto dinnerSlot = new DiarySlotDto();
    dinnerSlot.mealSlot = "DINNER";
    dinnerSlot.items = new ArrayList<>();

    double totCal = 0, totProt = 0, totCarb = 0, totFat = 0;

    for (DiaryItemDto item : diaryItemList) {
      totCal += item.calories;
      totProt += item.proteinG;
      totCarb += item.carbsG;
      totFat += item.fatG;
      if (item.name.toLowerCase(Locale.ROOT).contains("breakfast")
          || item.name.toLowerCase(Locale.ROOT).contains("poha")) {
        breakfastSlot.items.add(item);
        breakfastSlot.slotCalories += item.calories;
      } else if (item.name.toLowerCase(Locale.ROOT).contains("dinner")) {
        dinnerSlot.items.add(item);
        dinnerSlot.slotCalories += item.calories;
      } else {
        lunchSlot.items.add(item);
        lunchSlot.slotCalories += item.calories;
      }
    }

    day.totalCalories = totCal;
    day.totalProteinG = totProt;
    day.totalCarbsG = totCarb;
    day.totalFatG = totFat;
    day.formattedCalories = (int) totCal + " kcal";
    day.slots = List.of(breakfastSlot, lunchSlot, dinnerSlot);
    return day;
  }

  public synchronized void logMealToDiary(String recipeId, int servings, String slotName) {
    RecipeDetailDto recipe = getRecipeById(recipeId, servings);
    if (recipe == null) return;

    DiaryItemDto item = new DiaryItemDto();
    item.id = System.currentTimeMillis();
    item.recipeId = recipe.id;
    item.name = recipe.titleEn + " (" + (slotName != null ? slotName : "Meal") + ")";
    item.amountG = 250.0 * (servings > 0 ? servings : 2);
    item.calories =
        recipe.caloriesKcal
            * (servings > 0 ? servings : 2)
            / (recipe.defaultServings > 0 ? recipe.defaultServings : 2);
    item.proteinG = recipe.proteinG;
    item.carbsG = recipe.carbsG;
    item.fatG = recipe.fatG;

    diaryItemList.add(item);
    saveToStorage();
  }

  public synchronized void logWater(int amountMl) {
    currentWaterMl += amountMl;
    saveToStorage();
  }

  public synchronized void logSteps(int steps) {
    currentSteps += steps;
    saveToStorage();
  }

  private int calculateTotalDiaryCalories() {
    int sum = 0;
    for (DiaryItemDto d : diaryItemList) sum += (int) d.calories;
    return sum;
  }

  private int calculateTotalDiaryProtein() {
    int sum = 0;
    for (DiaryItemDto d : diaryItemList) sum += (int) d.proteinG;
    return sum;
  }

  private int calculateTotalDiaryCarbs() {
    int sum = 0;
    for (DiaryItemDto d : diaryItemList) sum += (int) d.carbsG;
    return sum;
  }

  private int calculateTotalDiaryFat() {
    int sum = 0;
    for (DiaryItemDto d : diaryItemList) sum += (int) d.fatG;
    return sum;
  }
}
