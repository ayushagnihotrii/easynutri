package com.easynutri.service;

import com.easynutri.dto.CookingSessionDto;
import com.easynutri.dto.CookingStepDto;
import com.easynutri.dto.ScaledIngredientDto;
import com.easynutri.entity.CookingSessionEntity;
import com.easynutri.entity.RecipeEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.CookingSessionRepository;
import com.easynutri.repository.RecipeRepository;
import com.easynutri.repository.UserRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CookingService {

  private static final Pattern TIMER_PATTERN =
      Pattern.compile(
          "(\\d+)(?:\\s*-\\s*\\d+)?\\s*(minutes?|mins?|seconds?|secs?|hours?|hrs?)",
          Pattern.CASE_INSENSITIVE);

  private final CookingSessionRepository cookingSessionRepository;
  private final RecipeRepository recipeRepository;
  private final UserRepository userRepository;
  private final PantryService pantryService;

  public CookingService(
      CookingSessionRepository cookingSessionRepository,
      RecipeRepository recipeRepository,
      UserRepository userRepository,
      PantryService pantryService) {
    this.cookingSessionRepository = cookingSessionRepository;
    this.recipeRepository = recipeRepository;
    this.userRepository = userRepository;
    this.pantryService = pantryService;
  }

  public CookingSessionDto startOrResumeSession(Long userId, String recipeId, Integer servings) {
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    RecipeEntity recipe =
        recipeRepository
            .findById(recipeId)
            .orElseThrow(() -> new IllegalArgumentException("Recipe not found: " + recipeId));

    int targetServings = (servings != null && servings > 0) ? servings : recipe.getBaseServings();

    Optional<CookingSessionEntity> active =
        cookingSessionRepository.findTopByUserIdAndRecipeIdAndCompletedAtIsNullOrderByStartedAtDesc(
            userId, recipeId);

    CookingSessionEntity session;
    if (active.isPresent()) {
      session = active.get();
      if (servings != null && servings > 0 && session.getServings() != servings) {
        session.setServings(targetServings);
        session = cookingSessionRepository.save(session);
      }
    } else {
      session = new CookingSessionEntity(user, recipe, targetServings);
      session = cookingSessionRepository.save(session);
    }

    return toDto(session);
  }

  public CookingSessionDto updateStep(Long userId, Long sessionId, int step) {
    CookingSessionEntity session =
        cookingSessionRepository
            .findByIdAndUserId(sessionId, userId)
            .orElseThrow(
                () -> new IllegalArgumentException("Cooking session not found: " + sessionId));

    session.setCurrentStep(Math.max(1, step));
    return toDto(cookingSessionRepository.save(session));
  }

  public CookingSessionDto finishCooking(
      Long userId,
      Long sessionId,
      Integer servings,
      boolean deductPantry,
      boolean logDiary,
      String notes) {
    CookingSessionEntity session =
        cookingSessionRepository
            .findByIdAndUserId(sessionId, userId)
            .orElseThrow(
                () -> new IllegalArgumentException("Cooking session not found: " + sessionId));

    int actualServings = (servings != null && servings > 0) ? servings : session.getServings();
    session.setServings(actualServings);
    session.setCompletedAt(Instant.now());
    if (notes != null) {
      session.setNotes(notes);
    }

    // Double-deduction prevention check
    if (deductPantry && !session.isPantryDeducted()) {
      pantryService.deductIngredients(userId, session.getRecipe().getId(), actualServings);
      session.setPantryDeducted(true);
    }

    if (logDiary && !session.isDiaryLogged()) {
      session.setDiaryLogged(true);
    }

    CookingSessionEntity saved = cookingSessionRepository.save(session);
    return toDto(saved);
  }

  @Transactional(readOnly = true)
  public CookingSessionDto getSession(Long userId, Long sessionId) {
    CookingSessionEntity session =
        cookingSessionRepository
            .findByIdAndUserId(sessionId, userId)
            .orElseThrow(
                () -> new IllegalArgumentException("Cooking session not found: " + sessionId));
    return toDto(session);
  }

  public List<CookingStepDto> parseSteps(String instructionsText) {
    List<CookingStepDto> steps = new ArrayList<>();
    if (instructionsText == null || instructionsText.isBlank()) {
      steps.add(new CookingStepDto(1, "Follow recipe instructions and enjoy your meal.", null));
      return steps;
    }

    String[] lines = instructionsText.split("\\r?\\n");
    int stepNum = 1;
    for (String raw : lines) {
      String line = raw.trim();
      if (line.isBlank()) continue;

      // Strip leading "1.", "1)", "Step 1:", etc.
      String cleaned = line.replaceFirst("^(?:Step\\s*)?\\d+[.):\\s]+", "").trim();
      if (cleaned.isBlank()) {
        cleaned = line;
      }

      Integer timerSeconds = extractTimerSeconds(cleaned);
      steps.add(new CookingStepDto(stepNum++, cleaned, timerSeconds));
    }

    if (steps.isEmpty()) {
      steps.add(new CookingStepDto(1, instructionsText.trim(), null));
    }
    return steps;
  }

  private Integer extractTimerSeconds(String text) {
    Matcher m = TIMER_PATTERN.matcher(text);
    if (m.find()) {
      try {
        int val = Integer.parseInt(m.group(1));
        String unit = m.group(2).toLowerCase();
        if (unit.startsWith("min")) {
          return val * 60;
        } else if (unit.startsWith("sec")) {
          return val;
        } else if (unit.startsWith("h")) {
          return val * 3600;
        }
      } catch (NumberFormatException ignored) {
      }
    }
    return null;
  }

  private CookingSessionDto toDto(CookingSessionEntity session) {
    RecipeEntity recipe = session.getRecipe();
    List<CookingStepDto> steps = parseSteps(recipe.getInstructionsText());

    double scale = (double) session.getServings() / Math.max(1, recipe.getBaseServings());
    List<ScaledIngredientDto> ingredients =
        recipe.getIngredients().stream()
            .map(
                ing ->
                    new ScaledIngredientDto(
                        ing.getFood().getId(),
                        ing.getFood().getNameEn(),
                        ing.getFood().getNameHi(),
                        ing.getAmountG(),
                        Math.round(ing.getAmountG() * scale * 10.0) / 10.0,
                        ing.isOptional()))
            .collect(Collectors.toList());

    return new CookingSessionDto(
        session.getId(),
        recipe.getId(),
        recipe.getTitleEn(),
        session.getServings(),
        session.getCurrentStep(),
        steps.size(),
        session.isCompleted(),
        session.isPantryDeducted(),
        session.isDiaryLogged(),
        session.getNotes(),
        steps,
        ingredients);
  }
}
