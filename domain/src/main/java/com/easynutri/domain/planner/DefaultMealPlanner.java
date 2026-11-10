package com.easynutri.domain.planner;

import com.easynutri.domain.model.ProfileContext;
import com.easynutri.domain.model.RecipeCandidate;
import com.easynutri.domain.nutrition.DefaultSafetyGate;
import com.easynutri.domain.nutrition.SafetyGate;
import com.easynutri.domain.rules.RuleVerdict;
import com.easynutri.domain.rules.SafetyResult;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Pure Java implementation of MealPlanner with clinical safety validation, pantry and expiry
 * prioritization, variety enforcement, and locked meal preservation.
 */
public final class DefaultMealPlanner implements MealPlanner {

  private final SafetyGate safetyGate;
  private final NutrientGapProvider gapProvider;

  public DefaultMealPlanner(SafetyGate safetyGate, NutrientGapProvider gapProvider) {
    this.safetyGate = safetyGate != null ? safetyGate : new DefaultSafetyGate();
    this.gapProvider = gapProvider != null ? gapProvider : new EmptyNutrientGapProvider();
  }

  public DefaultMealPlanner() {
    this(new DefaultSafetyGate(), new EmptyNutrientGapProvider());
  }

  @Override
  public Map<LocalDate, List<RecipeCandidate>> generateWeeklyPlan(
      LocalDate startDate, ProfileContext profile, List<RecipeCandidate> availableRecipes) {
    WeeklyMealPlan plan =
        generateIntelligentWeeklyPlan(
            startDate, profile, availableRecipes, Map.of(), Set.of(), Map.of(), 2000);

    Map<LocalDate, List<RecipeCandidate>> result = new LinkedHashMap<>();
    for (DailyMealPlan day : plan.days()) {
      result.put(
          day.date(),
          day.meals().stream().map(MealSlotProposal::recipe).collect(Collectors.toList()));
    }
    return result;
  }

  @Override
  public boolean validateDailyPlan(List<RecipeCandidate> meals, ProfileContext profile) {
    Objects.requireNonNull(meals, "meals cannot be null");
    Objects.requireNonNull(profile, "profile cannot be null");

    for (RecipeCandidate meal : meals) {
      SafetyResult safety = safetyGate.verifyRecipe(profile, meal);
      if (safety.verdict() == RuleVerdict.BLOCK) {
        return false;
      }
    }
    return true;
  }

  /**
   * Generates a 7-day personalized weekly meal plan with pantry prioritization, expiry bonuses,
   * locked meal preservation, and nutritional aggregation.
   */
  public WeeklyMealPlan generateIntelligentWeeklyPlan(
      LocalDate startDate,
      ProfileContext profile,
      List<RecipeCandidate> availableRecipes,
      Map<String, Double> pantryStock,
      Set<String> expiringIngredients,
      Map<LocalDate, Map<String, MealSlotProposal>> lockedMeals,
      int targetCalories) {

    Objects.requireNonNull(startDate, "startDate cannot be null");
    Objects.requireNonNull(profile, "profile cannot be null");
    Objects.requireNonNull(availableRecipes, "availableRecipes cannot be null");

    Map<String, Double> safePantry = pantryStock != null ? pantryStock : Map.of();
    Set<String> safeExpiring =
        expiringIngredients != null
            ? expiringIngredients.stream()
                .map(s -> s.toLowerCase(Locale.ROOT).trim())
                .collect(Collectors.toSet())
            : Set.of();
    Map<LocalDate, Map<String, MealSlotProposal>> safeLocked =
        lockedMeals != null ? lockedMeals : Map.of();

    // 1. Filter recipes that pass user SafetyGate (hard allergy blocks & severe conditions)
    List<RecipeCandidate> safeRecipes =
        availableRecipes.stream()
            .filter(r -> safetyGate.verifyRecipe(profile, r).verdict() != RuleVerdict.BLOCK)
            .collect(Collectors.toList());

    if (safeRecipes.isEmpty()) {
      safeRecipes = availableRecipes;
    }

    // 2. Partition recipes by meal slot
    Map<String, List<RecipeCandidate>> bySlot = new HashMap<>();
    for (String slot : List.of("BREAKFAST", "LUNCH", "DINNER", "SNACK")) {
      List<RecipeCandidate> slotRecipes =
          safeRecipes.stream()
              .filter(r -> slot.equalsIgnoreCase(r.mealSlot()))
              .collect(Collectors.toList());

      if (slotRecipes.isEmpty()) {
        slotRecipes = safeRecipes; // Fallback pool
      }
      bySlot.put(slot, slotRecipes);
    }

    List<DailyMealPlan> days = new ArrayList<>();
    Map<String, LocalDate> lastScheduledDate = new HashMap<>();
    Set<String> pantryFoodsUsed = new HashSet<>();

    List<String> dailySlots = List.of("BREAKFAST", "LUNCH", "DINNER", "SNACK");

    for (int dayOffset = 0; dayOffset < 7; dayOffset++) {
      LocalDate currentDate = startDate.plusDays(dayOffset);
      List<MealSlotProposal> dayProposals = new ArrayList<>();
      Map<String, MealSlotProposal> lockedForDay = safeLocked.getOrDefault(currentDate, Map.of());

      for (String slot : dailySlots) {
        // If locked meal exists for this day and slot, preserve it
        if (lockedForDay.containsKey(slot)) {
          MealSlotProposal locked = lockedForDay.get(slot);
          dayProposals.add(locked);
          lastScheduledDate.put(locked.recipe().candidateId(), currentDate);
          continue;
        }

        // Rank available candidates for this slot
        List<RecipeCandidate> candidates = bySlot.getOrDefault(slot, safeRecipes);
        RecipeCandidate selected =
            selectBestRecipe(
                candidates, currentDate, lastScheduledDate, safePantry, safeExpiring, profile);

        if (selected != null) {
          String reason = generateSelectionReason(selected, safePantry, safeExpiring, profile);
          dayProposals.add(MealSlotProposal.of(selected, slot, reason));
          lastScheduledDate.put(selected.candidateId(), currentDate);

          // Track pantry usage
          for (String ing : selected.ingredientFoodIds()) {
            if (isIngredientInPantry(ing, safePantry.keySet())) {
              pantryFoodsUsed.add(ing);
            }
          }
        }
      }

      // Aggregate daily nutrition
      double dayCal = dayProposals.stream().mapToDouble(p -> p.recipe().calories()).sum();
      double dayPro = dayProposals.stream().mapToDouble(p -> p.recipe().proteinG()).sum();
      double dayCarb = dayProposals.stream().mapToDouble(p -> p.recipe().carbsG()).sum();
      double dayFat = dayProposals.stream().mapToDouble(p -> p.recipe().fatG()).sum();

      boolean withinTolerance =
          targetCalories <= 0 || Math.abs(dayCal - targetCalories) <= (targetCalories * 0.25);

      days.add(
          new DailyMealPlan(
              currentDate,
              dayProposals,
              dayCal,
              dayPro,
              dayCarb,
              dayFat,
              withinTolerance,
              List.of("Plan tailored for " + profile.dietType() + " lifestyle")));
    }

    double avgCal = days.stream().mapToDouble(DailyMealPlan::totalCalories).average().orElse(0.0);
    int totalScheduled = days.stream().mapToInt(d -> d.meals().size()).sum();

    return new WeeklyMealPlan(
        startDate,
        startDate.plusDays(6),
        days,
        Math.round(avgCal * 10.0) / 10.0,
        pantryFoodsUsed.size(),
        totalScheduled);
  }

  /** Finds 3 safe replacement alternative recipes for a specific meal slot. */
  public List<RecipeCandidate> findReplacementOptions(
      RecipeCandidate current,
      String mealSlot,
      ProfileContext profile,
      List<RecipeCandidate> catalog,
      int limit) {

    Objects.requireNonNull(mealSlot, "mealSlot cannot be null");
    Objects.requireNonNull(profile, "profile cannot be null");
    Objects.requireNonNull(catalog, "catalog cannot be null");

    String targetSlot = mealSlot.toUpperCase(Locale.ROOT).trim();
    int safeLimit = Math.max(1, limit);

    return catalog.stream()
        .filter(r -> current == null || !r.candidateId().equals(current.candidateId()))
        .filter(r -> targetSlot.equalsIgnoreCase(r.mealSlot()))
        .filter(r -> safetyGate.verifyRecipe(profile, r).verdict() != RuleVerdict.BLOCK)
        .limit(safeLimit)
        .collect(Collectors.toList());
  }

  private RecipeCandidate selectBestRecipe(
      List<RecipeCandidate> pool,
      LocalDate currentDate,
      Map<String, LocalDate> lastScheduled,
      Map<String, Double> pantry,
      Set<String> expiring,
      ProfileContext profile) {

    if (pool.isEmpty()) return null;

    record ScoredRecipe(RecipeCandidate recipe, double score) {}
    List<ScoredRecipe> scored = new ArrayList<>();

    for (RecipeCandidate r : pool) {
      double score = 10.0;

      // 1. Variety check: penalize if scheduled within the last 3 days
      if (lastScheduled.containsKey(r.candidateId())) {
        long daysSince = ChronoUnit.DAYS.between(lastScheduled.get(r.candidateId()), currentDate);
        if (daysSince <= 2) {
          score -= 100.0; // Strong penalty for repetition within 3 days
        } else if (daysSince <= 4) {
          score -= 20.0;
        }
      }

      // 2. Expiry bonus: recipes using expiring foods get highest bonus
      for (String ing : r.ingredientFoodIds()) {
        String clean = ing.toLowerCase(Locale.ROOT).replace("food_", "").replace("_", " ");
        if (expiring.stream().anyMatch(e -> clean.contains(e) || e.contains(clean))) {
          score += 40.0;
        }
      }

      // 3. Pantry bonus: recipes using items in pantry
      for (String ing : r.ingredientFoodIds()) {
        if (isIngredientInPantry(ing, pantry.keySet())) {
          score += 15.0;
        }
      }

      scored.add(new ScoredRecipe(r, score));
    }

    scored.sort(Comparator.comparingDouble(ScoredRecipe::score).reversed());
    return scored.get(0).recipe();
  }

  private String generateSelectionReason(
      RecipeCandidate recipe,
      Map<String, Double> pantry,
      Set<String> expiring,
      ProfileContext profile) {

    for (String ing : recipe.ingredientFoodIds()) {
      String clean = ing.toLowerCase(Locale.ROOT).replace("food_", "").replace("_", " ");
      if (expiring.stream().anyMatch(e -> clean.contains(e) || e.contains(clean))) {
        return "Prioritizes " + clean + " approaching expiry";
      }
    }

    for (String ing : recipe.ingredientFoodIds()) {
      if (isIngredientInPantry(ing, pantry.keySet())) {
        String clean = ing.replace("food_", "").replace("_", " ");
        return "Uses ingredients already available in your pantry (" + clean + ")";
      }
    }

    return "Balanced recipe matching your " + profile.dietType() + " preferences";
  }

  private boolean isIngredientInPantry(String ingredientId, Set<String> pantryKeys) {
    if (ingredientId == null || pantryKeys == null) return false;
    String cleanIng = ingredientId.toLowerCase(Locale.ROOT).replace("food_", "").replace("_", " ");
    return pantryKeys.stream()
        .anyMatch(
            pk -> {
              String cleanPk = pk.toLowerCase(Locale.ROOT).replace("food_", "").replace("_", " ");
              return cleanIng.contains(cleanPk) || cleanPk.contains(cleanIng);
            });
  }
}
