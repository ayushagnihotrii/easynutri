package com.easynutri.controller.web;

import com.easynutri.dto.IngredientMatchRequest;
import com.easynutri.dto.RecipeMatchResponseDto;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.IngredientService;
import com.easynutri.service.PantryService;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class IngredientWebController {

  private final IngredientService ingredientService;
  private final UserRepository userRepository;
  private final PantryService pantryService;

  public IngredientWebController(
      IngredientService ingredientService,
      UserRepository userRepository,
      PantryService pantryService) {
    this.ingredientService = ingredientService;
    this.userRepository = userRepository;
    this.pantryService = pantryService;
  }

  @GetMapping("/ingredients")
  public String ingredientsPage(
      @RequestParam(value = "q", required = false) String initialQuery,
      Model model,
      Principal principal) {
    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    model.addAttribute("username", username);
    model.addAttribute("pageTitle", "Clinical Ingredient Finder");
    model.addAttribute("activeNav", "ingredients");

    // Load pantry staples for quick-selection chips
    var pantryItems = pantryService.getUserPantry(username);
    model.addAttribute("pantryStaples", pantryItems);

    if (initialQuery != null && !initialQuery.isBlank()) {
      List<String> list =
          Arrays.stream(initialQuery.split(","))
              .map(String::trim)
              .filter(s -> !s.isEmpty())
              .collect(Collectors.toList());

      IngredientMatchRequest request =
          new IngredientMatchRequest(list, false, List.of(), 0, "REFRIGERATED");
      List<RecipeMatchResponseDto> matches = ingredientService.findMatchingRecipes(userId, request);
      model.addAttribute("matches", matches);
      model.addAttribute("initialQuery", initialQuery);
    } else {
      model.addAttribute("matches", List.of());
      model.addAttribute("initialQuery", "");
    }

    return "ingredients/finder";
  }

  @PostMapping("/ingredients/match")
  public String matchRecipes(
      @RequestParam(value = "ingredients", required = false) String ingredientsStr,
      @RequestParam(value = "leftoverMode", defaultValue = "false") boolean leftoverMode,
      @RequestParam(value = "leftoverIngredients", required = false) String leftoversStr,
      @RequestParam(value = "hoursAgo", defaultValue = "2") int hoursAgo,
      @RequestParam(value = "storageMethod", defaultValue = "REFRIGERATED") String storageMethod,
      Model model,
      Principal principal) {

    String username = principal != null ? principal.getName() : "demo";
    Long userId = getUserId(username);

    model.addAttribute("username", username);
    model.addAttribute("pageTitle", "Clinical Ingredient Finder");
    model.addAttribute("activeNav", "ingredients");
    model.addAttribute("pantryStaples", pantryService.getUserPantry(username));

    List<String> ingList =
        ingredientsStr != null
            ? Arrays.stream(ingredientsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList())
            : List.of();

    List<String> leftList =
        leftoversStr != null
            ? Arrays.stream(leftoversStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList())
            : List.of();

    IngredientMatchRequest request =
        new IngredientMatchRequest(ingList, leftoverMode, leftList, hoursAgo, storageMethod);

    List<RecipeMatchResponseDto> matches = ingredientService.findMatchingRecipes(userId, request);

    model.addAttribute("matches", matches);
    model.addAttribute("initialQuery", ingredientsStr);
    model.addAttribute("leftoverMode", leftoverMode);
    model.addAttribute("hoursAgo", hoursAgo);
    model.addAttribute("storageMethod", storageMethod);

    return "ingredients/finder";
  }

  private Long getUserId(String username) {
    return userRepository.findByUsername(username).map(UserEntity::getId).orElse(1L);
  }
}
