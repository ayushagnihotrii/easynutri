package com.easynutri.service;

import com.easynutri.domain.model.ProfileContext;
import com.easynutri.domain.model.RecipeCandidate;
import com.easynutri.domain.nutrition.DefaultSafetyGate;
import com.easynutri.domain.nutrition.SafetyGate;
import com.easynutri.domain.recipe.DefaultFoodRecognitionProvider;
import com.easynutri.domain.recipe.DefaultIngredientResolver;
import com.easynutri.domain.recipe.DefaultOcrProvider;
import com.easynutri.domain.recipe.DefaultRecipeMatcher;
import com.easynutri.domain.recipe.FoodRecognitionProvider;
import com.easynutri.domain.recipe.IngredientResolver;
import com.easynutri.domain.recipe.LeftoverSafetyEvaluator;
import com.easynutri.domain.recipe.OcrProvider;
import com.easynutri.domain.recipe.RecipeMatchResult;
import com.easynutri.domain.recipe.RecipeMatcher;
import com.easynutri.domain.rules.RuleVerdict;
import com.easynutri.domain.rules.SafetyResult;
import com.easynutri.dto.IngredientCandidateDto;
import com.easynutri.dto.IngredientMatchRequest;
import com.easynutri.dto.RecipeDetailDto;
import com.easynutri.dto.RecipeMatchResponseDto;
import com.easynutri.entity.RecipeEntity;
import com.easynutri.entity.RecipeIngredientEntity;
import com.easynutri.repository.RecipeRepository;
import com.easynutri.repository.SavedRecipeRepository;
import com.easynutri.repository.UserProfileRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class IngredientService {

  private final RecipeRepository recipeRepository;
  private final RecipeService recipeService;
  private final SavedRecipeRepository savedRecipeRepository;
  private final UserProfileRepository userProfileRepository;

  private final IngredientResolver resolver = new DefaultIngredientResolver();
  private final RecipeMatcher matcher = new DefaultRecipeMatcher();
  private final SafetyGate safetyGate = new DefaultSafetyGate();
  private final FoodRecognitionProvider recognitionProvider =
      new DefaultFoodRecognitionProvider(resolver);
  private final OcrProvider ocrProvider = new DefaultOcrProvider(resolver);

  public IngredientService(
      RecipeRepository recipeRepository,
      RecipeService recipeService,
      SavedRecipeRepository savedRecipeRepository,
      UserProfileRepository userProfileRepository) {
    this.recipeRepository = recipeRepository;
    this.recipeService = recipeService;
    this.savedRecipeRepository = savedRecipeRepository;
    this.userProfileRepository = userProfileRepository;
  }

  public List<IngredientCandidateDto> resolveIngredients(String text) {
    if (text == null || text.isBlank()) {
      return List.of();
    }
    return resolver.resolveAll(text).stream()
        .map(IngredientCandidateDto::fromDomain)
        .collect(Collectors.toList());
  }

  public List<IngredientCandidateDto> processImage(byte[] imageBytes, String contentType) {
    return recognitionProvider.recognizeImage(imageBytes, contentType).stream()
        .map(IngredientCandidateDto::fromDomain)
        .collect(Collectors.toList());
  }

  public List<IngredientCandidateDto> processOcr(String ocrText) {
    return ocrProvider.extractIngredients(ocrText).stream()
        .map(IngredientCandidateDto::fromDomain)
        .collect(Collectors.toList());
  }

  public SafetyResult checkLeftoverSafety(String foodName, int hoursAgo, String storageMethod) {
    return LeftoverSafetyEvaluator.evaluateLeftoverSafety(foodName, hoursAgo, storageMethod);
  }

  public List<RecipeMatchResponseDto> findMatchingRecipes(
      Long userId, IngredientMatchRequest request) {

    // 1. Build profile context for current user
    ProfileContext profile = getProfileContext(userId);

    // 2. Evaluate Leftover Safety if leftover mode is active
    Set<String> leftoverIngredients = new HashSet<>();
    if (request.leftoverMode() && !request.leftoverIngredients().isEmpty()) {
      for (String leftover : request.leftoverIngredients()) {
        SafetyResult safety =
            LeftoverSafetyEvaluator.evaluateLeftoverSafety(
                leftover, request.hoursAgo(), request.storageMethod());
        // If hazardous (BLOCK), skip this leftover ingredient from safe consumption
        if (safety.verdict() != RuleVerdict.BLOCK) {
          leftoverIngredients.add(leftover);
        }
      }
    }

    // 3. Aggregate available ingredients
    Set<String> available = new HashSet<>(request.ingredients());
    available.addAll(leftoverIngredients);

    // 4. Fetch all active recipes and convert to RecipeCandidate domain records
    List<RecipeEntity> entities = recipeRepository.findAll();
    List<RecipeCandidate> candidates = new ArrayList<>();
    for (RecipeEntity entity : entities) {
      List<String> foodIds =
          entity.getIngredients().stream()
              .map(RecipeIngredientEntity::getFood)
              .map(f -> f.getId())
              .collect(Collectors.toList());

      List<String> tags =
          entity.getTags() != null
              ? Arrays.stream(entity.getTags().split(","))
                  .map(String::trim)
                  .filter(s -> !s.isEmpty())
                  .collect(Collectors.toList())
              : List.of();

      candidates.add(
          new RecipeCandidate(
              entity.getId(),
              entity.getTitleEn(),
              foodIds,
              entity.getMealSlot(),
              entity.getMealRole(),
              tags));
    }

    // 5. Run pure Java matcher with substitutions and leftover prioritization
    List<RecipeMatchResult> matchResults =
        matcher.matchRecipesDetailed(available, candidates, leftoverIngredients);

    // 6. Verify against SafetyGate (blocking allergen violations) and map to Response DTOs
    List<RecipeMatchResponseDto> responseList = new ArrayList<>();

    for (RecipeMatchResult match : matchResults) {
      RecipeCandidate candidate = match.candidate();

      // Clinical Safety Gate Audit
      SafetyResult safety = safetyGate.verifyRecipe(profile, candidate);
      if (safety.verdict() == RuleVerdict.BLOCK) {
        // Non-negotiable rule: NEVER show an allergen/condition BLOCKED recipe to user!
        continue;
      }

      RecipeEntity entity =
          entities.stream()
              .filter(e -> e.getId().equals(candidate.candidateId()))
              .findFirst()
              .orElse(null);

      if (entity == null) continue;

      RecipeDetailDto detail =
          recipeService.computeScaledNutrition(entity, entity.getBaseServings());
      boolean isSaved =
          userId != null && savedRecipeRepository.existsByUserIdAndRecipeId(userId, entity.getId());

      responseList.add(
          new RecipeMatchResponseDto(
              entity.getId(),
              entity.getTitleEn(),
              entity.getTitleHi(),
              entity.getMealSlot(),
              entity.getCookMinutes(),
              entity.getPrepMinutes(),
              entity.getBaseServings(),
              match.matchScore(),
              match.matchLabel().displayName(),
              match.matchLabel().badgeClass(),
              match.matchedIngredients(),
              match.missingIngredients(),
              match.suggestedSwaps(),
              match.usesLeftover(),
              detail.perServingCalories(),
              detail.perServingProteinG(),
              detail.perServingCarbG(),
              detail.perServingFatG(),
              safety.verdict().name(),
              safety.reasons(),
              isSaved));
    }

    return responseList;
  }

  private ProfileContext getProfileContext(Long userId) {
    if (userId == null) {
      return new ProfileContext(
          "guest", 30, "MALE", 175.0, 70.0, "VEGETARIAN", List.of(), List.of());
    }

    return userProfileRepository
        .findByUserId(userId)
        .map(
            up ->
                new ProfileContext(
                    String.valueOf(up.getId()),
                    up.getAge(),
                    up.getBiologicalSex(),
                    up.getHeightCm(),
                    up.getWeightKg(),
                    up.getDietaryPreference(),
                    List.copyOf(up.getAllergies()),
                    List.copyOf(up.getHealthConditions())))
        .orElseGet(
            () ->
                new ProfileContext(
                    String.valueOf(userId),
                    30,
                    "MALE",
                    175.0,
                    70.0,
                    "VEGETARIAN",
                    List.of(),
                    List.of()));
  }
}
