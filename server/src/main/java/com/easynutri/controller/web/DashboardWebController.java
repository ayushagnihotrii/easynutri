package com.easynutri.controller.web;

import com.easynutri.domain.calculation.BmiCalculator;
import com.easynutri.domain.model.ProfileContext;
import com.easynutri.domain.recipe.SmartPantryRecommender;
import com.easynutri.dto.RecipeDetailDto;
import com.easynutri.entity.UserProfileEntity;
import com.easynutri.service.PantryService;
import com.easynutri.service.RecipeService;
import com.easynutri.service.UserProfileService;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardWebController {

  private final UserProfileService profileService;
  private final PantryService pantryService;
  private final RecipeService recipeService;

  public DashboardWebController(
      UserProfileService profileService, PantryService pantryService, RecipeService recipeService) {
    this.profileService = profileService;
    this.pantryService = pantryService;
    this.recipeService = recipeService;
  }

  @GetMapping({"/", "/dashboard"})
  public String dashboard(Model model, Principal principal) {
    String username = principal != null ? principal.getName() : "demo";
    model.addAttribute("username", username);
    model.addAttribute("pageTitle", "Dashboard");
    model.addAttribute("activeNav", "dashboard");

    Optional<UserProfileEntity> profileOpt = profileService.findByUsername(username);

    // If authenticated user hasn't completed onboarding, redirect to the wizard
    if (profileOpt.isEmpty() || !profileOpt.get().isOnboardingCompleted()) {
      if (principal != null) {
        return "redirect:/onboarding";
      }
    }

    UserProfileEntity profile = profileOpt.orElse(null);

    // Dynamic targets derived from user profile and pure Java calculators
    int targetCalories =
        profile != null && profile.getTargetCalories() != null ? profile.getTargetCalories() : 2000;
    int targetProtein =
        profile != null && profile.getTargetProteinG() != null ? profile.getTargetProteinG() : 120;
    int targetCarbs =
        profile != null && profile.getTargetCarbG() != null ? profile.getTargetCarbG() : 220;
    int targetFat =
        profile != null && profile.getTargetFatG() != null ? profile.getTargetFatG() : 65;
    double targetWater =
        profile != null && profile.getTargetWaterL() != null ? profile.getTargetWaterL() : 3.0;

    // BMI Calculation
    double weight = profile != null ? profile.getWeightKg() : 70.0;
    double height = profile != null ? profile.getHeightCm() : 172.0;
    var bmiResult = BmiCalculator.calculate(weight, height);
    model.addAttribute("bmiValue", bmiResult.bmiValue());
    model.addAttribute("bmiCategory", bmiResult.category().getLabel());
    model.addAttribute("bmiGuidance", bmiResult.guidance());

    model.addAttribute("dailyCalorieGoal", targetCalories);
    model.addAttribute("caloriesConsumed", (int) Math.round(targetCalories * 0.65));
    model.addAttribute("caloriesBurned", 420);
    model.addAttribute("proteinGrams", (int) Math.round(targetProtein * 0.68));
    model.addAttribute("proteinTarget", targetProtein);
    model.addAttribute("carbGrams", (int) Math.round(targetCarbs * 0.62));
    model.addAttribute("carbTarget", targetCarbs);
    model.addAttribute("fatGrams", (int) Math.round(targetFat * 0.65));
    model.addAttribute("fatTarget", targetFat);
    model.addAttribute("waterLiters", 2.1);
    model.addAttribute("waterTarget", targetWater);
    model.addAttribute("stepCount", 7420);
    model.addAttribute("stepTarget", 10000);

    // Dynamic Pantry Expiration Alerts
    var userPantry = pantryService.getUserPantry(username);
    model.addAttribute("pantryAlerts", userPantry);

    // Clinical Recipes from Recipe Database
    List<RecipeDetailDto> recipes = recipeService.findAll();
    model.addAttribute("sampleRecipes", recipes);

    // Smart Pantry Staple Suggestions derived from profile
    if (profile != null) {
      ProfileContext ctx = profileService.toProfileContext(profile);
      var staples = SmartPantryRecommender.recommendStaples(ctx);
      model.addAttribute("smartStaples", staples);
    }

    return "dashboard/index";
  }
}
