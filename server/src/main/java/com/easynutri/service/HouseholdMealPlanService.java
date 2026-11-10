package com.easynutri.service;

import com.easynutri.domain.household.HouseholdDietaryAggregator;
import com.easynutri.domain.household.HouseholdDietaryAggregator.*;
import com.easynutri.dto.HouseholdMealPlanDto;
import com.easynutri.dto.HouseholdMealPlanRequest;
import com.easynutri.dto.HouseholdMemberDto;
import com.easynutri.dto.HouseholdRecipeRecommendationDto;
import com.easynutri.dto.MemberPortionDto;
import com.easynutri.entity.*;
import com.easynutri.repository.*;
import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class HouseholdMealPlanService {

  private final HouseholdRepository householdRepository;
  private final HouseholdMemberRepository memberRepository;
  private final HouseholdMealPlanRepository householdMealPlanRepository;
  private final MealPlanItemRepository mealPlanItemRepository;
  private final RecipeRepository recipeRepository;
  private final UserRepository userRepository;
  private final HouseholdService householdService;

  public HouseholdMealPlanService(
      HouseholdRepository householdRepository,
      HouseholdMemberRepository memberRepository,
      HouseholdMealPlanRepository householdMealPlanRepository,
      MealPlanItemRepository mealPlanItemRepository,
      RecipeRepository recipeRepository,
      UserRepository userRepository,
      HouseholdService householdService) {
    this.householdRepository = householdRepository;
    this.memberRepository = memberRepository;
    this.householdMealPlanRepository = householdMealPlanRepository;
    this.mealPlanItemRepository = mealPlanItemRepository;
    this.recipeRepository = recipeRepository;
    this.userRepository = userRepository;
    this.householdService = householdService;
  }

  @Transactional(readOnly = true)
  public List<HouseholdRecipeRecommendationDto> recommendHouseholdMeals(
      Long householdId, List<Long> memberUserIds) {

    List<MemberDietaryProfile> profiles =
        householdService.getMemberDietaryProfiles(householdId, memberUserIds);

    List<RecipeEntity> allRecipes = recipeRepository.findAll();
    List<HouseholdRecipeRecommendationDto> recommendations = new ArrayList<>();

    for (RecipeEntity recipe : allRecipes) {
      Set<String> recipeAllergens = extractRecipeAllergens(recipe);
      String recipeDiet = extractRecipeDiet(recipe);
      double calPerServing = estimateCaloriesPerServing(recipe);
      double proPerServing = estimateProteinPerServing(recipe);

      RecipeForEvaluation evalRecipe =
          new RecipeForEvaluation(
              recipe.getId(),
              recipe.getTitleEn(),
              recipeAllergens,
              recipeDiet,
              calPerServing,
              proPerServing);

      HouseholdSafetyEvaluation eval =
          HouseholdDietaryAggregator.evaluateHouseholdMeal(profiles, evalRecipe);
      List<MemberPortion> portions =
          HouseholdDietaryAggregator.computeIndividualPortions(profiles, evalRecipe);

      double totalServings =
          portions.stream().mapToDouble(MemberPortion::recommendedServings).sum();
      totalServings = Math.round(totalServings * 10.0) / 10.0;

      List<MemberPortionDto> portionDtos =
          portions.stream()
              .map(
                  p ->
                      new MemberPortionDto(
                          p.memberId(),
                          p.displayName(),
                          p.recommendedServings(),
                          p.individualCalories(),
                          p.individualProteinG()))
              .toList();

      recommendations.add(
          new HouseholdRecipeRecommendationDto(
              recipe.getId(),
              recipe.getTitleEn(),
              "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&w=600&q=80",
              recipe.getMealSlot(),
              recipe.getPrepMinutes(),
              recipe.getCookMinutes(),
              eval.isSafe(),
              eval.safetyViolations(),
              eval.explanation(),
              totalServings,
              portionDtos));
    }

    // Safe recipes first, then by title
    recommendations.sort(
        Comparator.comparing(HouseholdRecipeRecommendationDto::isSafe)
            .reversed()
            .thenComparing(HouseholdRecipeRecommendationDto::title));

    return recommendations;
  }

  public HouseholdMealPlanDto scheduleHouseholdMeal(
      Long requesterUserId, HouseholdMealPlanRequest request) {

    HouseholdEntity household =
        householdRepository
            .findById(request.householdId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException("Household not found: " + request.householdId()));

    if (!memberRepository.existsByHouseholdIdAndUserId(household.getId(), requesterUserId)) {
      throw new SecurityException("User does not belong to this household");
    }

    RecipeEntity recipe =
        recipeRepository
            .findById(request.recipeId())
            .orElseThrow(
                () -> new IllegalArgumentException("Recipe not found: " + request.recipeId()));

    List<MemberDietaryProfile> profiles =
        householdService.getMemberDietaryProfiles(
            household.getId(), request.participatingUserIds());

    Set<String> recipeAllergens = extractRecipeAllergens(recipe);
    String recipeDiet = extractRecipeDiet(recipe);
    double calPerServing = estimateCaloriesPerServing(recipe);
    double proPerServing = estimateProteinPerServing(recipe);

    RecipeForEvaluation evalRecipe =
        new RecipeForEvaluation(
            recipe.getId(),
            recipe.getTitleEn(),
            recipeAllergens,
            recipeDiet,
            calPerServing,
            proPerServing);

    HouseholdSafetyEvaluation eval =
        HouseholdDietaryAggregator.evaluateHouseholdMeal(profiles, evalRecipe);

    if (!eval.isSafe()) {
      throw new IllegalArgumentException(
          "Cannot schedule meal due to safety violations: "
              + String.join("; ", eval.safetyViolations()));
    }

    List<MemberPortion> portions =
        HouseholdDietaryAggregator.computeIndividualPortions(profiles, evalRecipe);
    double totalServings = portions.stream().mapToDouble(MemberPortion::recommendedServings).sum();
    totalServings = Math.round(totalServings * 10.0) / 10.0;

    HouseholdMealPlanEntity plan =
        new HouseholdMealPlanEntity(
            household,
            recipe,
            request.planDate(),
            request.mealSlot(),
            request.participatingUserIds(),
            totalServings);

    HouseholdMealPlanEntity saved = householdMealPlanRepository.save(plan);

    // Also synchronize individual meal plans for each participating member
    for (MemberPortion portion : portions) {
      userRepository
          .findById(portion.memberId())
          .ifPresent(
              u -> {
                MealPlanItemEntity personalItem =
                    new MealPlanItemEntity(
                        u,
                        recipe,
                        request.planDate(),
                        request.mealSlot(),
                        (int) Math.ceil(portion.recommendedServings()));
                mealPlanItemRepository.save(personalItem);
              });
    }

    return mapToDto(saved, portions);
  }

  @Transactional(readOnly = true)
  public List<HouseholdMealPlanDto> getHouseholdMealPlans(Long householdId, LocalDate planDate) {
    return householdMealPlanRepository
        .findByHouseholdIdAndPlanDateOrderByMealSlotAsc(householdId, planDate)
        .stream()
        .map(
            p -> {
              List<MemberDietaryProfile> profiles =
                  householdService.getMemberDietaryProfiles(
                      householdId, p.getParticipatingUserIdList());
              RecipeForEvaluation evalRecipe =
                  new RecipeForEvaluation(
                      p.getRecipe().getId(),
                      p.getRecipe().getTitleEn(),
                      extractRecipeAllergens(p.getRecipe()),
                      extractRecipeDiet(p.getRecipe()),
                      estimateCaloriesPerServing(p.getRecipe()),
                      estimateProteinPerServing(p.getRecipe()));
              List<MemberPortion> portions =
                  HouseholdDietaryAggregator.computeIndividualPortions(profiles, evalRecipe);
              return mapToDto(p, portions);
            })
        .toList();
  }

  private HouseholdMealPlanDto mapToDto(HouseholdMealPlanEntity p, List<MemberPortion> portions) {
    List<HouseholdMemberDto> members =
        householdService
            .getHouseholdMembers(p.getHousehold().getId(), p.getHousehold().getOwner().getId())
            .stream()
            .filter(m -> p.getParticipatingUserIdList().contains(m.userId()))
            .toList();

    List<MemberPortionDto> portionDtos =
        portions.stream()
            .map(
                po ->
                    new MemberPortionDto(
                        po.memberId(),
                        po.displayName(),
                        po.recommendedServings(),
                        po.individualCalories(),
                        po.individualProteinG()))
            .toList();

    return new HouseholdMealPlanDto(
        p.getId(),
        p.getHousehold().getId(),
        p.getRecipe().getId(),
        p.getRecipe().getTitleEn(),
        "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&w=600&q=80",
        p.getPlanDate(),
        p.getMealSlot(),
        members,
        p.getTotalServings(),
        portionDtos);
  }

  private Set<String> extractRecipeAllergens(RecipeEntity recipe) {
    Set<String> allergens = new HashSet<>();
    for (RecipeIngredientEntity ing : recipe.getIngredients()) {
      if (ing.getFood() != null && ing.getFood().getAllergenTags() != null) {
        Arrays.stream(ing.getFood().getAllergenTags().split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .forEach(allergens::add);
      }
    }
    return allergens;
  }

  private String extractRecipeDiet(RecipeEntity recipe) {
    boolean allVegan = true;
    boolean allJain = true;
    boolean allVeg = true;

    for (RecipeIngredientEntity ing : recipe.getIngredients()) {
      if (ing.getFood() != null && ing.getFood().getDietFlags() != null) {
        String flags = ing.getFood().getDietFlags().toUpperCase();
        if (!flags.contains("VEGAN")) allVegan = false;
        if (!flags.contains("JAIN")) allJain = false;
        if (!flags.contains("VEGETARIAN") && !flags.contains("VEG")) allVeg = false;
      }
    }

    if (allJain) return "JAIN";
    if (allVegan) return "VEGAN";
    if (allVeg) return "VEGETARIAN";
    return "NON_VEG";
  }

  private double estimateCaloriesPerServing(RecipeEntity recipe) {
    int servings = Math.max(1, recipe.getBaseServings());
    double totalKcal = 0;
    for (RecipeIngredientEntity ing : recipe.getIngredients()) {
      if (ing.getFood() != null) {
        totalKcal += (ing.getFood().getEnergyKcal() * ing.getAmountG()) / 100.0;
      }
    }
    return Math.round(totalKcal / servings);
  }

  private double estimateProteinPerServing(RecipeEntity recipe) {
    int servings = Math.max(1, recipe.getBaseServings());
    double totalPro = 0;
    for (RecipeIngredientEntity ing : recipe.getIngredients()) {
      if (ing.getFood() != null) {
        totalPro += (ing.getFood().getProteinG() * ing.getAmountG()) / 100.0;
      }
    }
    return Math.round((totalPro / servings) * 10.0) / 10.0;
  }
}
