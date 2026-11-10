package com.easynutri.domain.recipe;

import com.easynutri.domain.model.RecipeCandidate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Pure Java implementation of ingredient-based recipe discovery and ranking, supporting
 * substitution detection, leftover prioritization, and granular match labeling.
 */
public final class DefaultRecipeMatcher implements RecipeMatcher {

  private static final Map<String, List<String>> COMMON_SUBSTITUTIONS = new HashMap<>();

  static {
    COMMON_SUBSTITUTIONS.put("paneer", List.of("tofu", "soy paneer", "soya paneer"));
    COMMON_SUBSTITUTIONS.put("tofu", List.of("paneer", "cottage cheese"));
    COMMON_SUBSTITUTIONS.put("curd", List.of("yogurt", "yoghurt", "lemon juice", "dahi"));
    COMMON_SUBSTITUTIONS.put(
        "toor dal", List.of("moong dal", "chana dal", "masoor dal", "yellow dal"));
    COMMON_SUBSTITUTIONS.put("moong dal", List.of("toor dal", "chana dal", "masoor dal"));
    COMMON_SUBSTITUTIONS.put("rice", List.of("brown rice", "millets", "poha", "quinoa"));
    COMMON_SUBSTITUTIONS.put("besan", List.of("atta", "oats flour"));
    COMMON_SUBSTITUTIONS.put("spinach", List.of("methi", "kale", "palak"));
    COMMON_SUBSTITUTIONS.put("potato", List.of("sweet potato", "aloo"));
  }

  @Override
  public List<RecipeCandidate> matchRecipes(
      Set<String> availableIngredients,
      List<RecipeCandidate> candidates,
      double minMatchPercentage) {

    Objects.requireNonNull(availableIngredients, "availableIngredients cannot be null");
    Objects.requireNonNull(candidates, "candidates cannot be null");

    double threshold = Math.max(0.0, Math.min(1.0, minMatchPercentage));

    record ScoredCandidate(RecipeCandidate candidate, double score) {}

    List<ScoredCandidate> scored = new ArrayList<>();
    for (RecipeCandidate candidate : candidates) {
      double score = calculateMatchScore(availableIngredients, candidate);
      if (score >= threshold) {
        scored.add(new ScoredCandidate(candidate, score));
      }
    }

    scored.sort(Comparator.comparingDouble(ScoredCandidate::score).reversed());

    return scored.stream().map(ScoredCandidate::candidate).collect(Collectors.toList());
  }

  @Override
  public double calculateMatchScore(Set<String> availableIngredients, RecipeCandidate candidate) {
    Objects.requireNonNull(availableIngredients, "availableIngredients cannot be null");
    Objects.requireNonNull(candidate, "candidate cannot be null");

    List<String> requiredIngredients = candidate.ingredientFoodIds();
    if (requiredIngredients.isEmpty()) {
      return 1.0;
    }

    Set<String> normalizedAvailable =
        availableIngredients.stream()
            .map(s -> s.trim().toLowerCase(Locale.ROOT))
            .collect(Collectors.toSet());

    int matchedCount = 0;
    for (String req : requiredIngredients) {
      String cleanReq = cleanIngredientId(req);
      boolean matched = isMatched(cleanReq, normalizedAvailable);
      if (matched) {
        matchedCount++;
      }
    }

    double rawScore = (double) matchedCount / (double) requiredIngredients.size();
    return BigDecimal.valueOf(rawScore).setScale(2, RoundingMode.HALF_UP).doubleValue();
  }

  @Override
  public List<RecipeMatchResult> matchRecipesDetailed(
      Set<String> availableIngredients,
      List<RecipeCandidate> candidates,
      Set<String> leftoverIngredients) {

    Objects.requireNonNull(availableIngredients, "availableIngredients cannot be null");
    Objects.requireNonNull(candidates, "candidates cannot be null");

    Set<String> normalizedAvailable =
        availableIngredients.stream()
            .map(s -> s.trim().toLowerCase(Locale.ROOT))
            .collect(Collectors.toSet());

    Set<String> normalizedLeftovers =
        leftoverIngredients != null
            ? leftoverIngredients.stream()
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet())
            : Set.of();

    List<RecipeMatchResult> results = new ArrayList<>();

    for (RecipeCandidate candidate : candidates) {
      List<String> required = candidate.ingredientFoodIds();
      List<String> matched = new ArrayList<>();
      List<String> missing = new ArrayList<>();
      List<String> suggestedSwaps = new ArrayList<>();
      boolean usesLeftover = false;

      for (String req : required) {
        String cleanReq = cleanIngredientId(req);
        if (isMatched(cleanReq, normalizedAvailable)) {
          matched.add(cleanReq);
          if (isMatched(cleanReq, normalizedLeftovers)) {
            usesLeftover = true;
          }
        } else {
          // Check if a known substitution is available
          String foundSwap = findAvailableSwap(cleanReq, normalizedAvailable);
          if (foundSwap != null) {
            matched.add(cleanReq);
            suggestedSwaps.add("Swap " + foundSwap + " for " + cleanReq);
            if (isMatched(foundSwap, normalizedLeftovers)) {
              usesLeftover = true;
            }
          } else {
            missing.add(cleanReq);
          }
        }
      }

      int totalRequired = Math.max(1, required.size());
      double directMatches = matched.size() - suggestedSwaps.size();
      double score = (directMatches + (suggestedSwaps.size() * 0.85)) / totalRequired;
      if (usesLeftover) {
        score = Math.min(1.0, score + 0.1);
      }
      double roundedScore =
          BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP).doubleValue();

      RecipeMatchLabel label;
      if (missing.isEmpty()) {
        label =
            suggestedSwaps.isEmpty()
                ? RecipeMatchLabel.READY_NOW
                : RecipeMatchLabel.READY_WITH_SWAP;
      } else if (missing.size() == 1) {
        label = RecipeMatchLabel.MISSING_ONE;
      } else if (missing.size() == 2) {
        label = RecipeMatchLabel.MISSING_TWO;
      } else {
        label = RecipeMatchLabel.MISSING_MANY;
      }

      results.add(
          new RecipeMatchResult(
              candidate, roundedScore, label, matched, missing, suggestedSwaps, usesLeftover));
    }

    // Sort: READY_NOW > READY_WITH_SWAP > MISSING_ONE > MISSING_TWO > MISSING_MANY
    // Secondary: usesLeftover true first
    // Tertiary: score descending
    results.sort(
        Comparator.comparingInt((RecipeMatchResult r) -> r.matchLabel().ordinal())
            .thenComparing((RecipeMatchResult r) -> r.usesLeftover() ? 0 : 1)
            .thenComparing(Comparator.comparingDouble(RecipeMatchResult::matchScore).reversed()));

    return results;
  }

  private boolean isMatched(String cleanReq, Set<String> available) {
    return available.stream()
        .anyMatch(
            avail -> {
              String cleanAvail = cleanIngredientId(avail);
              return cleanReq.contains(cleanAvail) || cleanAvail.contains(cleanReq);
            });
  }

  private String findAvailableSwap(String cleanReq, Set<String> available) {
    for (Map.Entry<String, List<String>> entry : COMMON_SUBSTITUTIONS.entrySet()) {
      if (cleanReq.contains(entry.getKey()) || entry.getKey().contains(cleanReq)) {
        for (String substitute : entry.getValue()) {
          for (String avail : available) {
            String cleanAvail = cleanIngredientId(avail);
            if (cleanAvail.contains(substitute) || substitute.contains(cleanAvail)) {
              return substitute;
            }
          }
        }
      }
    }
    return null;
  }

  private String cleanIngredientId(String id) {
    if (id == null) return "";
    String cleaned = id.trim().toLowerCase(Locale.ROOT);
    if (cleaned.startsWith("food_")) {
      cleaned = cleaned.substring(5);
    }
    return cleaned.replace('_', ' ').trim();
  }
}
