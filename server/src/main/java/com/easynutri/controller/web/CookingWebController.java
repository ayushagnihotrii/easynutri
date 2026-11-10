package com.easynutri.controller.web;

import com.easynutri.dto.CookingSessionDto;
import com.easynutri.dto.RecipeDetailDto;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.CookingService;
import com.easynutri.service.RecipeService;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cook")
public class CookingWebController {

  private final CookingService cookingService;
  private final RecipeService recipeService;
  private final UserRepository userRepository;

  public CookingWebController(
      CookingService cookingService, RecipeService recipeService, UserRepository userRepository) {
    this.cookingService = cookingService;
    this.recipeService = recipeService;
    this.userRepository = userRepository;
  }

  @GetMapping("/{recipeId}")
  public String cookingMode(
      @PathVariable("recipeId") String recipeId,
      @RequestParam(value = "servings", required = false) Integer servings,
      Model model,
      Principal principal) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    CookingSessionDto session = cookingService.startOrResumeSession(userId, recipeId, servings);

    RecipeDetailDto recipe =
        recipeService
            .findByIdAndServings(recipeId, session.servings())
            .orElseThrow(() -> new IllegalArgumentException("Recipe not found: " + recipeId));

    model.addAttribute("username", username);
    model.addAttribute("pageTitle", "Cooking Mode: " + recipe.titleEn());
    model.addAttribute("activeNav", "cook");
    model.addAttribute("session", session);
    model.addAttribute("recipe", recipe);

    return "cook/index";
  }

  @PostMapping("/{recipeId}/step")
  public String updateStep(
      @PathVariable("recipeId") String recipeId,
      @RequestParam("sessionId") Long sessionId,
      @RequestParam("step") int step,
      Principal principal) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    cookingService.updateStep(userId, sessionId, step);
    return "redirect:/cook/" + recipeId;
  }

  @PostMapping("/{recipeId}/finish")
  public String finishCooking(
      @PathVariable("recipeId") String recipeId,
      @RequestParam("sessionId") Long sessionId,
      @RequestParam(value = "servings", defaultValue = "2") int servings,
      @RequestParam(value = "deductPantry", defaultValue = "true") boolean deductPantry,
      @RequestParam(value = "logDiary", defaultValue = "true") boolean logDiary,
      @RequestParam(value = "notes", required = false) String notes,
      Principal principal,
      RedirectAttributes redirectAttributes) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    CookingSessionDto finished =
        cookingService.finishCooking(userId, sessionId, servings, deductPantry, logDiary, notes);

    String message = "Meal completed! Congratulations! ";
    if (finished.pantryDeducted()) {
      message += "Ingredients were automatically deducted from your pantry. ";
    }
    redirectAttributes.addFlashAttribute("flashMessage", message);

    return "redirect:/planner";
  }

  private Long getUserId(String username) {
    return userRepository.findByUsername(username).map(UserEntity::getId).orElse(1L);
  }
}
