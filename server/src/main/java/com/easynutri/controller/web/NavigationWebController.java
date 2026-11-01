package com.easynutri.controller.web;

import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NavigationWebController {

  private void populateCommon(Model model, Principal principal, String title, String activeNav) {
    String username = principal != null ? principal.getName() : "Demo User";
    model.addAttribute("username", username);
    model.addAttribute("pageTitle", title);
    model.addAttribute("activeNav", activeNav);
  }

  @GetMapping("/planner")
  public String mealPlanner(Model model, Principal principal) {
    populateCommon(model, principal, "Meal Planner", "planner");
    return "planner/index";
  }

  @GetMapping("/diary")
  public String foodDiary(Model model, Principal principal) {
    populateCommon(model, principal, "Food Diary", "diary");
    return "diary/index";
  }

  @GetMapping("/pantry")
  public String myPantry(Model model, Principal principal) {
    populateCommon(model, principal, "My Pantry", "pantry");
    return "pantry/index";
  }

  @GetMapping("/grocery")
  public String groceryList(Model model, Principal principal) {
    populateCommon(model, principal, "Grocery List", "grocery");
    return "grocery/index";
  }

  @GetMapping({"/tracking", "/activity"})
  public String tracking(Model model, Principal principal) {
    populateCommon(model, principal, "Activity & Tracking", "tracking");
    return "tracking/index";
  }

  @GetMapping("/reports")
  public String reports(Model model, Principal principal) {
    populateCommon(model, principal, "Progress Reports", "reports");
    return "reports/index";
  }

  @GetMapping("/assistant")
  public String assistant(Model model, Principal principal) {
    populateCommon(model, principal, "AI Nutrition Assistant", "assistant");
    return "assistant/index";
  }

  @GetMapping("/community")
  public String community(Model model, Principal principal) {
    populateCommon(model, principal, "Community & Streaks", "community");
    return "community/index";
  }

  @GetMapping("/family")
  public String family(Model model, Principal principal) {
    populateCommon(model, principal, "Family Profiles", "family");
    return "family/index";
  }
}
