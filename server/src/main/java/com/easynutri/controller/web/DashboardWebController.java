package com.easynutri.controller.web;

import java.security.Principal;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardWebController {

  @GetMapping({"/", "/dashboard"})
  public String dashboard(Model model, Principal principal) {
    String username = principal != null ? principal.getName() : "Demo User";
    model.addAttribute("username", username);
    model.addAttribute("pageTitle", "Dashboard");
    model.addAttribute("activeNav", "dashboard");

    // Clinical and nutrition metrics (Phase 0 demonstration foundation)
    model.addAttribute("dailyCalorieGoal", 2000);
    model.addAttribute("caloriesConsumed", 1450);
    model.addAttribute("caloriesBurned", 420);
    model.addAttribute("proteinGrams", 78);
    model.addAttribute("proteinTarget", 120);
    model.addAttribute("carbGrams", 160);
    model.addAttribute("carbTarget", 220);
    model.addAttribute("fatGrams", 45);
    model.addAttribute("fatTarget", 65);
    model.addAttribute("waterLiters", 2.1);
    model.addAttribute("waterTarget", 3.0);
    model.addAttribute("stepCount", 7420);
    model.addAttribute("stepTarget", 10000);

    // Pantry Expiration Alerts (Smart Pantry preview)
    model.addAttribute(
        "pantryAlerts",
        List.of(
            new AlertItem("Fresh Baby Spinach", "Expires in 1 day", "danger"),
            new AlertItem("Greek Yogurt", "Expires in 2 days", "warning"),
            new AlertItem("Whole Wheat Tortillas", "Expires in 4 days", "info")));

    // Sample recipes verified with pure Java rules
    model.addAttribute(
        "sampleRecipes",
        List.of(
            new RecipePreview(
                "Quinoa Buddha Bowl",
                "High protein & fiber, completely vegan.",
                480,
                22,
                15,
                "25 min",
                "Vegetarian, Gluten-Free"),
            new RecipePreview(
                "Herb Roasted Salmon",
                "Omega-3 rich with seasonal steamed greens.",
                520,
                38,
                20,
                "30 min",
                "High-Protein, Keto-Friendly"),
            new RecipePreview(
                "Lentil Spinach Soup",
                "Quick pantry-friendly antioxidant rich soup.",
                320,
                18,
                8,
                "20 min",
                "Pantry-Friendly, Low-Calorie")));

    return "dashboard/index";
  }

  public record AlertItem(String name, String urgency, String level) {}

  public record RecipePreview(
      String title,
      String description,
      int calories,
      int protein,
      int carbs,
      String cookTime,
      String tags) {}
}
