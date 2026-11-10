package com.easynutri.controller.web;

import com.easynutri.domain.model.FoodCandidate;
import com.easynutri.domain.model.ProfileContext;
import com.easynutri.domain.nutrition.DefaultDeficiencyService;
import com.easynutri.domain.nutrition.NutrientInfo;
import com.easynutri.dto.RecipeDetailDto;
import com.easynutri.entity.FoodEntity;
import com.easynutri.entity.UserProfileEntity;
import com.easynutri.repository.FoodRepository;
import com.easynutri.service.FoodService;
import com.easynutri.service.RecipeService;
import com.easynutri.service.UserProfileService;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DeficiencyWebController {

  private final DefaultDeficiencyService deficiencyService = new DefaultDeficiencyService();
  private final FoodRepository foodRepository;
  private final FoodService foodService;
  private final RecipeService recipeService;
  private final UserProfileService profileService;

  public DeficiencyWebController(
      FoodRepository foodRepository,
      FoodService foodService,
      RecipeService recipeService,
      UserProfileService profileService) {
    this.foodRepository = foodRepository;
    this.foodService = foodService;
    this.recipeService = recipeService;
    this.profileService = profileService;
  }

  @GetMapping("/deficiency-helper")
  public String deficiencyHelper(
      @RequestParam(value = "nutrient", required = false, defaultValue = "iron") String nutrientKey,
      Model model,
      Principal principal) {

    String username = principal != null ? principal.getName() : "demo";
    model.addAttribute("username", username);
    model.addAttribute("pageTitle", "Clinical Deficiency Assistant");
    model.addAttribute("activeNav", "deficiency");

    Optional<UserProfileEntity> profileOpt = profileService.findByUsername(username);
    ProfileContext profile =
        profileOpt
            .map(profileService::toProfileContext)
            .orElseGet(
                () ->
                    new ProfileContext(
                        "demo", 28, "MALE", 175.0, 72.0, "VEGETARIAN", List.of(), List.of()));

    List<NutrientInfo> allNutrients = deficiencyService.getAllSupportedNutrients();
    model.addAttribute("allNutrients", allNutrients);

    NutrientInfo currentNutrient = deficiencyService.getNutrient(nutrientKey);
    if (currentNutrient == null) {
      currentNutrient = deficiencyService.getNutrient("iron");
    }
    model.addAttribute("currentNutrient", currentNutrient);

    double targetRda = deficiencyService.getRdaForProfile(currentNutrient.key(), profile);
    model.addAttribute("targetRda", targetRda);

    // Convert FoodEntities to FoodCandidates for pure Java domain ranking
    List<FoodEntity> foodEntities = foodRepository.findAll();
    List<FoodCandidate> candidates = new ArrayList<>();
    for (FoodEntity f : foodEntities) {
      Map<String, Double> nutrients = new HashMap<>();
      nutrients.put("iron_mg", f.getIronMg());
      nutrients.put("calcium_mg", f.getCalciumMg());
      nutrients.put("protein_g", f.getProteinG());
      nutrients.put("fibre_g", f.getFibreG());
      nutrients.put("potassium_mg", f.getPotassiumMg());
      nutrients.put("sodium_mg", f.getSodiumMg());

      List<String> allergens =
          f.getAllergenTags() != null ? Arrays.asList(f.getAllergenTags().split(",")) : List.of();
      List<String> dietFlags =
          f.getDietFlags() != null ? Arrays.asList(f.getDietFlags().split(",")) : List.of();

      candidates.add(
          new FoodCandidate(
              f.getId(), f.getNameEn(), f.getCategory(), nutrients, allergens, dietFlags));
    }

    List<FoodCandidate> richCandidates =
        deficiencyService.findFoodsRichIn(currentNutrient.key(), candidates, profile, 8);

    record FoodDensityCard(
        String id,
        String nameEn,
        String nameHi,
        String category,
        double amountPer100g,
        String unit,
        int percentRda) {}

    List<FoodDensityCard> densityCards = new ArrayList<>();
    for (FoodCandidate fc : richCandidates) {
      FoodEntity entity =
          foodEntities.stream()
              .filter(e -> e.getId().equals(fc.candidateId()))
              .findFirst()
              .orElse(null);

      if (entity != null) {
        double val =
            switch (currentNutrient.key()) {
              case "iron" -> entity.getIronMg();
              case "calcium" -> entity.getCalciumMg();
              case "protein" -> entity.getProteinG();
              case "fibre" -> entity.getFibreG();
              default -> entity.getProteinG();
            };

        int pct = targetRda > 0 ? (int) Math.round((val / targetRda) * 100.0) : 0;
        densityCards.add(
            new FoodDensityCard(
                entity.getId(),
                entity.getNameEn(),
                entity.getNameHi(),
                entity.getCategory(),
                val,
                currentNutrient.unit(),
                pct));
      }
    }
    model.addAttribute("richFoods", densityCards);

    // Find recipes highlighting this nutrient
    List<RecipeDetailDto> allRecipes = recipeService.findAll();
    List<RecipeDetailDto> suggestedRecipes =
        allRecipes.stream()
            .filter(
                r ->
                    r.ingredients().stream()
                        .anyMatch(
                            i ->
                                richCandidates.stream()
                                    .anyMatch(rc -> rc.candidateId().equals(i.foodId()))))
            .limit(3)
            .collect(Collectors.toList());

    if (suggestedRecipes.isEmpty()) {
      suggestedRecipes = allRecipes.stream().limit(2).collect(Collectors.toList());
    }
    model.addAttribute("suggestedRecipes", suggestedRecipes);

    return "deficiency/index";
  }
}
