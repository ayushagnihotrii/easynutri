package com.easynutri.controller.web;

import com.easynutri.dto.RecipeDetailDto;
import com.easynutri.dto.WeeklyMealPlanResponseDto;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.MealPlanService;
import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/planner")
public class MealPlannerWebController {

  private final MealPlanService mealPlanService;
  private final UserRepository userRepository;

  public MealPlannerWebController(MealPlanService mealPlanService, UserRepository userRepository) {
    this.mealPlanService = mealPlanService;
    this.userRepository = userRepository;
  }

  @GetMapping
  public String plannerPage(
      @RequestParam(value = "weekStart", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate weekStart,
      Model model,
      Principal principal) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    LocalDate currentMonday =
        LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    LocalDate effectiveStart =
        weekStart != null
            ? weekStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            : currentMonday;

    WeeklyMealPlanResponseDto plan = mealPlanService.getWeeklyPlan(userId, effectiveStart);

    model.addAttribute("username", username);
    model.addAttribute("pageTitle", "Clinical Weekly Meal Planner");
    model.addAttribute("activeNav", "planner");
    model.addAttribute("plan", plan);
    model.addAttribute("weekStart", effectiveStart);
    model.addAttribute("prevWeek", effectiveStart.minusWeeks(1));
    model.addAttribute("nextWeek", effectiveStart.plusWeeks(1));
    model.addAttribute("currentMonday", currentMonday);
    model.addAttribute("isCurrentWeek", effectiveStart.equals(currentMonday));

    return "planner/index";
  }

  @PostMapping("/generate")
  public String generatePlan(
      @RequestParam(value = "weekStart", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate weekStart,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    LocalDate effectiveStart =
        weekStart != null
            ? weekStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            : LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

    mealPlanService.generateIntelligentWeek(userId, effectiveStart);
    redirectAttributes.addFlashAttribute(
        "flashMessage",
        "Weekly meal plan generated successfully! Prioritized pantry items and clinical goals.");

    return "redirect:/planner?weekStart=" + effectiveStart;
  }

  @PostMapping("/replace")
  public String replaceMeal(
      @RequestParam("itemId") Long itemId,
      @RequestParam("newRecipeId") String newRecipeId,
      @RequestParam(value = "weekStart", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate weekStart,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    mealPlanService.replaceMeal(userId, itemId, newRecipeId);
    redirectAttributes.addFlashAttribute(
        "flashMessage", "Meal replaced with verified alternative.");

    String param = weekStart != null ? "?weekStart=" + weekStart : "";
    return "redirect:/planner" + param;
  }

  @PostMapping("/lock")
  public String toggleLock(
      @RequestParam("itemId") Long itemId,
      @RequestParam(value = "weekStart", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate weekStart,
      Principal principal) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    mealPlanService.toggleLock(userId, itemId);
    String param = weekStart != null ? "?weekStart=" + weekStart : "";
    return "redirect:/planner" + param;
  }

  @PostMapping("/complete")
  public String toggleComplete(
      @RequestParam("itemId") Long itemId,
      @RequestParam(value = "weekStart", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate weekStart,
      Principal principal) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    mealPlanService.toggleComplete(userId, itemId);
    String param = weekStart != null ? "?weekStart=" + weekStart : "";
    return "redirect:/planner" + param;
  }

  @PostMapping("/servings")
  public String updateServings(
      @RequestParam("itemId") Long itemId,
      @RequestParam("servings") int servings,
      @RequestParam(value = "weekStart", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate weekStart,
      Principal principal) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    mealPlanService.updateServings(userId, itemId, servings);
    String param = weekStart != null ? "?weekStart=" + weekStart : "";
    return "redirect:/planner" + param;
  }

  @PostMapping("/delete")
  public String deleteItem(
      @RequestParam("itemId") Long itemId,
      @RequestParam(value = "weekStart", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate weekStart,
      Principal principal) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    mealPlanService.removeMealPlanItem(userId, itemId);
    String param = weekStart != null ? "?weekStart=" + weekStart : "";
    return "redirect:/planner" + param;
  }

  @GetMapping("/alternatives/{itemId}")
  @ResponseBody
  public ResponseEntity<List<RecipeDetailDto>> getAlternatives(
      @PathVariable("itemId") Long itemId, Principal principal) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    List<RecipeDetailDto> alternatives = mealPlanService.getReplacementAlternatives(userId, itemId);
    return ResponseEntity.ok(alternatives);
  }

  private Long getUserId(String username) {
    return userRepository.findByUsername(username).map(UserEntity::getId).orElse(1L);
  }
}
