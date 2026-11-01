package com.easynutri.config;

import com.easynutri.dto.OnboardingRequest;
import com.easynutri.dto.RegisterRequest;
import com.easynutri.entity.FoodEntity;
import com.easynutri.entity.PantryItemEntity;
import com.easynutri.entity.RecipeEntity;
import com.easynutri.entity.RecipeIngredientEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.FoodRepository;
import com.easynutri.repository.PantryItemRepository;
import com.easynutri.repository.RecipeRepository;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.UserProfileService;
import com.easynutri.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

  private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

  @Bean
  public CommandLineRunner seedDatabase(
      UserService userService,
      UserProfileService profileService,
      UserRepository userRepository,
      FoodRepository foodRepository,
      RecipeRepository recipeRepository,
      PantryItemRepository pantryRepository) {
    return args -> {
      ObjectMapper objectMapper = new ObjectMapper();

      // 1. Seed Demo User
      UserEntity demoUser;
      if (!userService.existsByUsername("demo")) {
        demoUser =
            userService.registerUser(
                new RegisterRequest(
                    "demo", "demo@easynutri.com", "Password123!", "EasyNutri Explorer"));
        log.info("Initialized default demo user: username='demo', password='Password123!'");
      } else {
        demoUser = userRepository.findByUsername("demo").orElseThrow();
      }

      // 2. Seed Foods Catalog
      File foodsFile = new File("data/foods/indian_foods.json");
      if (!foodsFile.exists()) {
        foodsFile = new File("../data/foods/indian_foods.json");
      }
      if (foodsFile.exists() && foodRepository.count() == 0) {
        try {
          List<FoodEntity> foods =
              objectMapper.readValue(foodsFile, new TypeReference<List<FoodEntity>>() {});
          foodRepository.saveAll(foods);
          log.info(
              "Successfully ingested {} Indian food items from {}",
              foods.size(),
              foodsFile.getPath());
        } catch (Exception e) {
          log.error("Failed to parse foods file: {}", e.getMessage(), e);
        }
      }

      // 3. Seed Recipes Catalog
      File recipesFile = new File("data/recipes/indian_recipes.json");
      if (!recipesFile.exists()) {
        recipesFile = new File("../data/recipes/indian_recipes.json");
      }
      if (recipesFile.exists() && recipeRepository.count() == 0) {
        try {
          List<Map<String, Object>> recipeDataList =
              objectMapper.readValue(
                  recipesFile, new TypeReference<List<Map<String, Object>>>() {});

          for (Map<String, Object> map : recipeDataList) {
            String id = (String) map.get("id");
            String titleEn = (String) map.get("title_en");
            String titleHi = (String) map.get("title_hi");
            String desc = (String) map.get("description_en");
            int baseServings = ((Number) map.get("base_servings")).intValue();
            int prep = ((Number) map.get("prep_minutes")).intValue();
            int cook = ((Number) map.get("cook_minutes")).intValue();
            String mealSlot = (String) map.get("meal_slot");
            String mealRole = (String) map.get("meal_role");
            String instructions = (String) map.get("instructions_text");
            String tags = (String) map.get("tags");
            String source = (String) map.get("source");
            boolean verified = Boolean.TRUE.equals(map.get("verified"));

            RecipeEntity recipe =
                new RecipeEntity(
                    id, titleEn, titleHi, desc, baseServings, prep, cook, mealSlot, mealRole);
            recipe.setInstructionsText(instructions);
            recipe.setTags(tags);
            recipe.setSource(source != null ? source : "SAMPLE");
            recipe.setVerified(verified);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> ingredients =
                (List<Map<String, Object>>) map.get("ingredients");
            if (ingredients != null) {
              for (Map<String, Object> ingMap : ingredients) {
                String foodId = (String) ingMap.get("food_id");
                double amountG = ((Number) ingMap.get("amount_g")).doubleValue();
                boolean optional = Boolean.TRUE.equals(ingMap.get("optional"));

                foodRepository
                    .findById(foodId)
                    .ifPresent(
                        food -> {
                          RecipeIngredientEntity ingEntity =
                              new RecipeIngredientEntity(recipe, food, amountG, optional);
                          recipe.getIngredients().add(ingEntity);
                        });
              }
            }
            recipeRepository.save(recipe);
          }
          log.info(
              "Successfully ingested {} Indian recipes from {}",
              recipeDataList.size(),
              recipesFile.getPath());
        } catch (Exception e) {
          log.error("Failed to parse recipes file: {}", e.getMessage(), e);
        }
      }

      // 4. Seed Demo User Profile if not present
      if (!profileService.hasCompletedOnboarding("demo")) {
        profileService.saveOnboardingProfile(
            "demo",
            new OnboardingRequest(
                28,
                "MALE",
                175.0,
                72.0,
                "MODERATE",
                "MAINTAIN",
                "VEGETARIAN",
                false,
                false,
                List.of(),
                List.of()));
        log.info("Initialized default profile for demo user with calculated targets.");
      }

      // 5. Seed Demo User Pantry Items
      if (pantryRepository.findByUserUsername("demo").isEmpty()) {
        foodRepository
            .findById("food_spinach_palak")
            .ifPresent(
                f ->
                    pantryRepository.save(
                        new PantryItemEntity(demoUser, f, 250.0, LocalDate.now().plusDays(1))));
        foodRepository
            .findById("food_curd_dahi")
            .ifPresent(
                f ->
                    pantryRepository.save(
                        new PantryItemEntity(demoUser, f, 400.0, LocalDate.now().plusDays(2))));
        foodRepository
            .findById("food_moong_dal")
            .ifPresent(
                f ->
                    pantryRepository.save(
                        new PantryItemEntity(demoUser, f, 1000.0, LocalDate.now().plusDays(45))));
        foodRepository
            .findById("food_cooked_rice")
            .ifPresent(
                f ->
                    pantryRepository.save(
                        new PantryItemEntity(demoUser, f, 1200.0, LocalDate.now().plusDays(30))));
        log.info("Initialized sample pantry stock for demo user.");
      }
    };
  }
}
