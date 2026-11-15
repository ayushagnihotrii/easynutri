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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.password.PasswordEncoder;

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
      PantryItemRepository pantryRepository,
      PasswordEncoder passwordEncoder,
      Environment environment) {
    return args ->
        initializeData(
            userService,
            profileService,
            userRepository,
            foodRepository,
            recipeRepository,
            pantryRepository,
            passwordEncoder,
            environment);
  }

  public void initializeData(
      UserService userService,
      UserProfileService profileService,
      UserRepository userRepository,
      FoodRepository foodRepository,
      RecipeRepository recipeRepository,
      PantryItemRepository pantryRepository,
      PasswordEncoder passwordEncoder,
      Environment environment) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    boolean isDevOrTest = environment.acceptsProfiles(Profiles.of("dev", "test", "local"));
    boolean isProd = environment.acceptsProfiles(Profiles.of("prod"));

    // 1. Seed Canonical Foods Catalog First
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

    // 2. Seed Canonical Recipes Catalog
    File recipesFile = new File("data/recipes/indian_recipes.json");
    if (!recipesFile.exists()) {
      recipesFile = new File("../data/recipes/indian_recipes.json");
    }
    if (recipesFile.exists() && recipeRepository.count() == 0) {
      try {
        List<Map<String, Object>> recipeDataList =
            objectMapper.readValue(recipesFile, new TypeReference<List<Map<String, Object>>>() {});

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

    // 3. Audit & Remediate Legacy Compromised Administrator in Existing Databases
    userRepository
        .findByUsername("admin")
        .ifPresent(
            adminUser -> {
              if ("ROLE_ADMIN".equals(adminUser.getRole())
                  && passwordEncoder.matches("AdminSecure2026!", adminUser.getPasswordHash())) {
                String newAdminPass = environment.getProperty("EASYNUTRI_ADMIN_BOOTSTRAP_PASSWORD");
                if (newAdminPass != null && !newAdminPass.isBlank() && newAdminPass.length() >= 8) {
                  adminUser.setPasswordHash(passwordEncoder.encode(newAdminPass));
                  adminUser.setEnabled(true);
                  userRepository.save(adminUser);
                  log.warn(
                      "Compromised legacy administrator password detected for account '{}'. Automatically rotated to new credentials provided via EASYNUTRI_ADMIN_BOOTSTRAP_PASSWORD.",
                      adminUser.getUsername());
                } else {
                  adminUser.setEnabled(false);
                  userRepository.save(adminUser);
                  log.error(
                      "SECURITY ALERT: Administrator account '{}' was found with a known compromised default password. The account has been DISABLED automatically. Set EASYNUTRI_ADMIN_BOOTSTRAP_PASSWORD in environment to rotate and re-enable.",
                      adminUser.getUsername());
                }
              }
            });

    // 4. Production Administrator Bootstrap & Rotation
    String adminUsername = environment.getProperty("EASYNUTRI_ADMIN_BOOTSTRAP_USERNAME");
    String adminEmail = environment.getProperty("EASYNUTRI_ADMIN_BOOTSTRAP_EMAIL");
    String adminPassword = environment.getProperty("EASYNUTRI_ADMIN_BOOTSTRAP_PASSWORD");
    boolean forcePasswordReset =
        Boolean.parseBoolean(
            environment.getProperty("EASYNUTRI_ADMIN_FORCE_PASSWORD_RESET", "false"));

    if (adminUsername != null
        && !adminUsername.isBlank()
        && adminPassword != null
        && !adminPassword.isBlank()) {

      if (adminPassword.length() < 8) {
        log.error(
            "Administrator bootstrap/rotation failed: EASYNUTRI_ADMIN_BOOTSTRAP_PASSWORD must be at least 8 characters.");
      } else if (forcePasswordReset && userService.existsByUsername(adminUsername)) {
        UserEntity targetUser = userRepository.findByUsername(adminUsername).orElseThrow();
        targetUser.setPasswordHash(passwordEncoder.encode(adminPassword));
        targetUser.setRole("ROLE_ADMIN");
        targetUser.setEnabled(true);
        userRepository.save(targetUser);
        log.info(
            "Administrator password explicitly rotated via EASYNUTRI_ADMIN_FORCE_PASSWORD_RESET for username='{}'",
            adminUsername);
      } else if (userRepository.existsByRole("ROLE_ADMIN")) {
        log.info(
            "Administrator account already exists in database; skipping bootstrap provisioning.");
      } else if (userService.existsByUsername(adminUsername)) {
        log.warn(
            "Username '{}' already exists without ROLE_ADMIN; skipping bootstrap to prevent collision.",
            adminUsername);
      } else if (adminEmail != null && !adminEmail.isBlank()) {
        UserEntity adminUser =
            userService.registerUser(
                new RegisterRequest(
                    adminUsername, adminEmail, adminPassword, "System Administrator"));
        adminUser.setRole("ROLE_ADMIN");
        userRepository.save(adminUser);
        log.info(
            "Successfully provisioned initial administrator account for username='{}'",
            adminUsername);
      }
    } else if (isProd && !userRepository.existsByRole("ROLE_ADMIN")) {
      log.warn(
          "Production notice: No administrator bootstrap credentials configured (EASYNUTRI_ADMIN_BOOTSTRAP_*). System started with no default administrator.");
    }

    // 5. Inactivate Legacy Demo Account in Production
    if (isProd) {
      userRepository
          .findByUsername("demo")
          .ifPresent(
              demoUser -> {
                if (demoUser.isEnabled()) {
                  demoUser.setEnabled(false);
                  userRepository.save(demoUser);
                  log.warn(
                      "Production security: Disabled legacy development 'demo' account in production environment.");
                }
              });
    }

    // 6. Development Mock Account
    if (isDevOrTest) {
      String devDemoPassword =
          environment.getProperty("EASYNUTRI_DEMO_PASSWORD", "DevDemoPass2026!#");
      UserEntity demoUser;
      if (!userService.existsByUsername("demo")) {
        demoUser =
            userService.registerUser(
                new RegisterRequest(
                    "demo", "demo@easynutri.com", devDemoPassword, "EasyNutri Explorer"));
        log.info("Initialized local development mock user: username='demo' (profile: dev/test)");
      } else {
        demoUser = userRepository.findByUsername("demo").orElseThrow();
      }

      // Seed Demo User Profile if not present
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
        log.info("Initialized default profile for development demo user with calculated targets.");
      }

      // Seed Demo User Complete 14 Pantry Items
      if (pantryRepository.findByUserUsername("demo").size() < 10) {
        addPantryItem(pantryRepository, demoUser, foodRepository, "food_spinach_palak", 250.0, 1);
        addPantryItem(pantryRepository, demoUser, foodRepository, "food_curd_dahi", 400.0, 2);
        addPantryItem(pantryRepository, demoUser, foodRepository, "food_cow_milk", 1000.0, 2);
        addPantryItem(pantryRepository, demoUser, foodRepository, "food_tomato", 500.0, 3);
        addPantryItem(pantryRepository, demoUser, foodRepository, "food_banana", 6.0, 3);
        addPantryItem(pantryRepository, demoUser, foodRepository, "food_paneer", 200.0, 4);
        addPantryItem(pantryRepository, demoUser, foodRepository, "food_onion", 1000.0, 10);
        addPantryItem(pantryRepository, demoUser, foodRepository, "food_potato_aloo", 1000.0, 14);
        addPantryItem(pantryRepository, demoUser, foodRepository, "food_cooked_rice", 1000.0, 30);
        addPantryItem(pantryRepository, demoUser, foodRepository, "food_moong_dal", 500.0, 45);
        addPantryItem(pantryRepository, demoUser, foodRepository, "food_toor_dal", 500.0, 60);
        addPantryItem(pantryRepository, demoUser, foodRepository, "food_wheat_flour", 2000.0, 60);
        addPantryItem(pantryRepository, demoUser, foodRepository, "food_rolled_oats", 500.0, 90);
        addPantryItem(pantryRepository, demoUser, foodRepository, "food_cumin_seeds", 150.0, 180);
        log.info("Initialized 14 clinical Indian pantry inventory items for 'demo' user.");
      }
    }
  }

  private void addPantryItem(
      PantryItemRepository pantryRepository,
      UserEntity user,
      FoodRepository foodRepository,
      String foodId,
      double quantityG,
      int daysUntilExpiry) {
    foodRepository
        .findById(foodId)
        .ifPresent(
            f -> {
              boolean exists =
                  pantryRepository.findByUserUsername(user.getUsername()).stream()
                      .anyMatch(p -> p.getFood() != null && foodId.equals(p.getFood().getId()));
              if (!exists) {
                pantryRepository.save(
                    new PantryItemEntity(
                        user, f, quantityG, LocalDate.now().plusDays(daysUntilExpiry)));
              }
            });
  }
}
