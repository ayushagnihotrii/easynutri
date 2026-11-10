package com.easynutri.controller.web;

import com.easynutri.dto.RecipeAdaptationDto;
import com.easynutri.entity.RecipeAdaptationEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.RecipeAdaptationService;
import java.security.Principal;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/recipes")
public class RecipeAdaptationWebController {

  private final RecipeAdaptationService adaptationService;
  private final UserRepository userRepository;

  public RecipeAdaptationWebController(
      RecipeAdaptationService adaptationService, UserRepository userRepository) {
    this.adaptationService = adaptationService;
    this.userRepository = userRepository;
  }

  @GetMapping("/adapt/{id}")
  public String adaptRecipe(
      @PathVariable("id") String recipeId,
      @RequestParam(value = "type", defaultValue = "VEGAN") String type,
      @RequestParam(value = "prompt", required = false) String prompt,
      Model model,
      Principal principal) {
    Long userId = resolveUserId(principal);
    RecipeAdaptationDto adaptation =
        adaptationService.previewAdaptation(userId, recipeId, type, prompt);

    model.addAttribute("username", principal != null ? principal.getName() : "Demo User");
    model.addAttribute("pageTitle", "Adapt Recipe: " + adaptation.baseRecipeTitle());
    model.addAttribute("activeNav", "recipes");
    model.addAttribute("adaptation", adaptation);
    model.addAttribute("selectedType", type);
    model.addAttribute("prompt", prompt != null ? prompt : "");
    model.addAttribute(
        "availableTypes", List.of("VEGAN", "JAIN", "DAIRY_FREE", "LOW_SODIUM", "CUSTOM"));
    return "recipes/adapt";
  }

  @PostMapping("/adapt/{id}/save")
  public String saveAdaptation(
      @PathVariable("id") String recipeId,
      @RequestParam("type") String type,
      @RequestParam(value = "prompt", required = false) String prompt,
      Principal principal,
      RedirectAttributes redirectAttributes) {
    Long userId = resolveUserId(principal);
    RecipeAdaptationDto saved = adaptationService.saveAdaptation(userId, recipeId, type, prompt);
    redirectAttributes.addFlashAttribute(
        "successMessage", "Successfully saved variant: " + saved.adaptationTitle());
    return "redirect:/recipes/adaptations";
  }

  @GetMapping("/adaptations")
  public String listAdaptations(Model model, Principal principal) {
    Long userId = resolveUserId(principal);
    List<RecipeAdaptationEntity> adaptations = adaptationService.getUserAdaptations(userId);

    model.addAttribute("username", principal != null ? principal.getName() : "Demo User");
    model.addAttribute("pageTitle", "My Adapted Recipes");
    model.addAttribute("activeNav", "recipes");
    model.addAttribute("adaptations", adaptations);
    return "recipes/adaptations";
  }

  private Long resolveUserId(Principal principal) {
    if (principal == null) {
      return userRepository.findByUsername("demo").map(UserEntity::getId).orElse(1L);
    }
    return userRepository.findByUsername(principal.getName()).map(UserEntity::getId).orElse(1L);
  }
}
