package com.easynutri.domain.recipe;

import com.easynutri.domain.model.RecipeCandidate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** Pure Java implementation of ingredient-based recipe discovery and ranking. */
public final class DefaultRecipeMatcher implements RecipeMatcher {

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
      String cleanReq = req.trim().toLowerCase(Locale.ROOT);
      boolean matched =
          normalizedAvailable.stream()
              .anyMatch(avail -> cleanReq.contains(avail) || avail.contains(cleanReq));
      if (matched) {
        matchedCount++;
      }
    }

    double rawScore = (double) matchedCount / (double) requiredIngredients.size();
    return BigDecimal.valueOf(rawScore).setScale(2, RoundingMode.HALF_UP).doubleValue();
  }
}
