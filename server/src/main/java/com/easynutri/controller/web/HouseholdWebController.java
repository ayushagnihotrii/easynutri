package com.easynutri.controller.web;

import com.easynutri.dto.*;
import com.easynutri.service.ActiveProfileContextService;
import com.easynutri.service.HouseholdMealPlanService;
import com.easynutri.service.HouseholdService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/family")
public class HouseholdWebController {

  private final HouseholdService householdService;
  private final HouseholdMealPlanService householdMealPlanService;
  private final ActiveProfileContextService activeProfileContextService;

  public HouseholdWebController(
      HouseholdService householdService,
      HouseholdMealPlanService householdMealPlanService,
      ActiveProfileContextService activeProfileContextService) {
    this.householdService = householdService;
    this.householdMealPlanService = householdMealPlanService;
    this.activeProfileContextService = activeProfileContextService;
  }

  @GetMapping
  public String familyDashboard(Model model, Principal principal, HttpSession session) {
    Long authUserId = activeProfileContextService.getAuthenticatedUserId(principal);
    Long activeProfileId = activeProfileContextService.getActiveProfileId(principal, session);

    HouseholdDto household =
        householdService
            .getHouseholdForUser(authUserId)
            .orElseGet(() -> householdService.createHousehold(authUserId, null));

    LocalDate today = LocalDate.now();
    List<HouseholdMealPlanDto> todayMeals =
        householdMealPlanService.getHouseholdMealPlans(household.id(), today);

    model.addAttribute("household", household);
    model.addAttribute("members", household.members());
    model.addAttribute("activeProfileId", activeProfileId);
    model.addAttribute("todayMeals", todayMeals);
    model.addAttribute("username", principal != null ? principal.getName() : "demo");
    model.addAttribute("activeNav", "family");
    model.addAttribute("pageTitle", household.name() + " — EasyNutri Family");

    return "family/index";
  }

  @PostMapping("/create")
  public String createHousehold(
      @RequestParam("name") String name,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    Long authUserId = activeProfileContextService.getAuthenticatedUserId(principal);
    try {
      HouseholdDto household = householdService.createHousehold(authUserId, name);
      redirectAttributes.addFlashAttribute(
          "flashMessage", "Household '" + household.name() + "' created successfully!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "Failed to create household: " + e.getMessage());
    }

    return "redirect:/family";
  }

  @PostMapping("/members/add")
  public String addMember(
      @RequestParam("householdId") Long householdId,
      @RequestParam("displayName") String displayName,
      @RequestParam("relationship") String relationship,
      @RequestParam(value = "role", defaultValue = "MEMBER") String role,
      @RequestParam(value = "age", defaultValue = "25") int age,
      @RequestParam(value = "biologicalSex", defaultValue = "FEMALE") String biologicalSex,
      @RequestParam(value = "heightCm", required = false) Double heightCm,
      @RequestParam(value = "weightKg", required = false) Double weightKg,
      @RequestParam(value = "activityLevel", defaultValue = "MODERATE") String activityLevel,
      @RequestParam(value = "healthGoal", defaultValue = "MAINTAIN") String healthGoal,
      @RequestParam(value = "dietaryPreference", defaultValue = "VEGETARIAN")
          String dietaryPreference,
      @RequestParam(value = "allergies", required = false) String allergiesRaw,
      @RequestParam(value = "healthConditions", required = false) String conditionsRaw,
      @RequestParam(value = "sensitiveMode", defaultValue = "false") boolean sensitiveMode,
      @RequestParam(value = "avatarColor", defaultValue = "#237A57") String avatarColor,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    Long authUserId = activeProfileContextService.getAuthenticatedUserId(principal);

    List<String> allergies =
        allergiesRaw != null && !allergiesRaw.isBlank()
            ? Arrays.stream(allergiesRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList()
            : List.of();

    List<String> conditions =
        conditionsRaw != null && !conditionsRaw.isBlank()
            ? Arrays.stream(conditionsRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList()
            : List.of();

    AddHouseholdMemberRequest req =
        new AddHouseholdMemberRequest(
            displayName,
            relationship,
            role,
            age,
            biologicalSex,
            heightCm,
            weightKg,
            activityLevel,
            healthGoal,
            dietaryPreference,
            allergies,
            conditions,
            sensitiveMode,
            avatarColor);

    try {
      HouseholdMemberDto added = householdService.addMember(authUserId, householdId, req);
      redirectAttributes.addFlashAttribute(
          "flashMessage", "Added " + added.displayName() + " to family household!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "Failed to add family member: " + e.getMessage());
    }

    return "redirect:/family";
  }

  @PostMapping("/switch/{profileId}")
  public String switchProfile(
      @PathVariable("profileId") Long profileId,
      Principal principal,
      HttpSession session,
      HttpServletRequest request,
      RedirectAttributes redirectAttributes) {

    try {
      activeProfileContextService.switchActiveProfile(principal, session, profileId);
      redirectAttributes.addFlashAttribute("flashMessage", "Active profile switched successfully.");
    } catch (SecurityException e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "Unauthorized profile switch: " + e.getMessage());
    }

    String referer = request.getHeader("Referer");
    if (referer != null && !referer.isBlank()) {
      return "redirect:" + referer;
    }
    return "redirect:/family";
  }

  @GetMapping("/meal-planner")
  public String householdMealPlanner(
      @RequestParam(value = "memberIds", required = false) List<Long> memberIds,
      @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate date,
      Model model,
      Principal principal,
      HttpSession session) {

    Long authUserId = activeProfileContextService.getAuthenticatedUserId(principal);
    Long activeProfileId = activeProfileContextService.getActiveProfileId(principal, session);

    HouseholdDto household =
        householdService
            .getHouseholdForUser(authUserId)
            .orElseGet(() -> householdService.createHousehold(authUserId, null));

    LocalDate targetDate = date != null ? date : LocalDate.now();

    List<Long> selectedMemberIds =
        (memberIds != null && !memberIds.isEmpty())
            ? memberIds
            : household.members().stream().map(HouseholdMemberDto::userId).toList();

    List<HouseholdRecipeRecommendationDto> recommendations =
        householdMealPlanService.recommendHouseholdMeals(household.id(), selectedMemberIds);

    List<HouseholdMealPlanDto> scheduledMeals =
        householdMealPlanService.getHouseholdMealPlans(household.id(), targetDate);

    model.addAttribute("household", household);
    model.addAttribute("members", household.members());
    model.addAttribute("selectedMemberIds", selectedMemberIds);
    model.addAttribute("recommendations", recommendations);
    model.addAttribute("scheduledMeals", scheduledMeals);
    model.addAttribute("targetDate", targetDate);
    model.addAttribute("activeProfileId", activeProfileId);
    model.addAttribute("username", principal != null ? principal.getName() : "demo");
    model.addAttribute("activeNav", "family");
    model.addAttribute("pageTitle", "Household Meal Planning — EasyNutri");

    return "family/meal-planner";
  }

  @PostMapping("/meal-planner/schedule")
  public String scheduleHouseholdMeal(
      @RequestParam("householdId") Long householdId,
      @RequestParam("recipeId") String recipeId,
      @RequestParam("planDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate planDate,
      @RequestParam(value = "mealSlot", defaultValue = "DINNER") String mealSlot,
      @RequestParam("memberIds") List<Long> memberIds,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    Long authUserId = activeProfileContextService.getAuthenticatedUserId(principal);

    try {
      HouseholdMealPlanRequest req =
          new HouseholdMealPlanRequest(householdId, recipeId, planDate, mealSlot, memberIds);
      householdMealPlanService.scheduleHouseholdMeal(authUserId, req);
      redirectAttributes.addFlashAttribute(
          "flashMessage",
          "Household meal scheduled! Scaled portions synchronized to individual member plans.");
    } catch (IllegalArgumentException e) {
      redirectAttributes.addFlashAttribute("flashError", e.getMessage());
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute(
          "flashError", "Failed to schedule household meal: " + e.getMessage());
    }

    return "redirect:/family/meal-planner?date=" + planDate;
  }
}
