package com.easynutri.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.easynutri.domain.model.ProfileContext;
import com.easynutri.domain.model.RecipeCandidate;
import com.easynutri.domain.planner.DailyMealPlan;
import com.easynutri.domain.planner.DefaultMealPlanner;
import com.easynutri.domain.planner.MealSlotProposal;
import com.easynutri.domain.planner.WeeklyMealPlan;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DefaultMealPlannerTest {

  private DefaultMealPlanner planner;
  private ProfileContext vegetarianProfile;
  private List<RecipeCandidate> sampleCatalog;

  @BeforeEach
  void setUp() {
    planner = new DefaultMealPlanner();

    vegetarianProfile =
        new ProfileContext(
            "p1", 28, "MALE", 175.0, 70.0, "VEGETARIAN", List.of("peanuts"), List.of());

    RecipeCandidate poha =
        new RecipeCandidate(
            "r_poha",
            "Kanda Poha",
            List.of("food_poha", "food_onion"),
            "BREAKFAST",
            "MAIN",
            List.of("VEGETARIAN"),
            280.0,
            6.0,
            45.0,
            8.0);

    RecipeCandidate oats =
        new RecipeCandidate(
            "r_oats",
            "Masala Oats",
            List.of("food_oats", "food_vegetables"),
            "BREAKFAST",
            "MAIN",
            List.of("VEGETARIAN"),
            250.0,
            8.0,
            40.0,
            6.0);

    RecipeCandidate khichdi =
        new RecipeCandidate(
            "r_khichdi",
            "Moong Khichdi",
            List.of("food_cooked_rice", "food_moong_dal"),
            "LUNCH",
            "MAIN",
            List.of("VEGETARIAN"),
            350.0,
            14.0,
            55.0,
            7.0);

    RecipeCandidate dalRoti =
        new RecipeCandidate(
            "r_dal_roti",
            "Toor Dal with Roti",
            List.of("food_toor_dal", "food_roti_chapati"),
            "LUNCH",
            "MAIN",
            List.of("VEGETARIAN"),
            400.0,
            18.0,
            65.0,
            8.0);

    RecipeCandidate paneerBhurji =
        new RecipeCandidate(
            "r_paneer_bhurji",
            "Paneer Bhurji with Roti",
            List.of("food_paneer", "food_roti_chapati", "food_tomato"),
            "DINNER",
            "MAIN",
            List.of("VEGETARIAN"),
            420.0,
            22.0,
            35.0,
            18.0);

    RecipeCandidate palakPaneer =
        new RecipeCandidate(
            "r_palak_paneer",
            "Palak Paneer with Roti",
            List.of("food_spinach_palak", "food_paneer", "food_roti_chapati"),
            "DINNER",
            "MAIN",
            List.of("VEGETARIAN"),
            390.0,
            20.0,
            32.0,
            16.0);

    RecipeCandidate peanutChikki =
        new RecipeCandidate(
            "r_peanut_chikki",
            "Peanut Chikki",
            List.of("food_peanuts", "food_jaggery"),
            "SNACK",
            "SNACK",
            List.of("VEGETARIAN"),
            180.0,
            5.0,
            22.0,
            9.0);

    RecipeCandidate roastedChana =
        new RecipeCandidate(
            "r_roasted_chana",
            "Roasted Chana",
            List.of("food_chana_dal"),
            "SNACK",
            "SNACK",
            List.of("VEGETARIAN"),
            140.0,
            7.0,
            20.0,
            2.5);

    sampleCatalog =
        List.of(
            poha, oats, khichdi, dalRoti, paneerBhurji, palakPaneer, peanutChikki, roastedChana);
  }

  @Test
  @DisplayName("Generate 7-day plan adhering to dietary choices and zero allergen bypass")
  void testGenerateWeeklyPlanSafetyAndDiet() {
    LocalDate start = LocalDate.now();
    WeeklyMealPlan plan =
        planner.generateIntelligentWeeklyPlan(
            start, vegetarianProfile, sampleCatalog, Map.of(), Set.of(), Map.of(), 1800);

    assertEquals(7, plan.days().size());

    for (DailyMealPlan day : plan.days()) {
      assertFalse(day.meals().isEmpty());
      for (MealSlotProposal proposal : day.meals()) {
        // Assert allergen safety: Peanut Chikki should NEVER be selected for user with peanut
        // allergy!
        assertNotEquals(
            "r_peanut_chikki",
            proposal.recipe().candidateId(),
            "SafetyGate violation: peanut recipe was scheduled for allergic user!");
      }
    }
  }

  @Test
  @DisplayName("Prioritize recipes utilizing pantry items approaching expiry")
  void testExpiryPrioritization() {
    LocalDate start = LocalDate.now();
    Set<String> expiring = Set.of("spinach", "palak");

    WeeklyMealPlan plan =
        planner.generateIntelligentWeeklyPlan(
            start, vegetarianProfile, sampleCatalog, Map.of(), expiring, Map.of(), 1800);

    // Palak Paneer should be scheduled on Day 1 (start date) due to expiring spinach
    DailyMealPlan day1 = plan.days().get(0);
    boolean hasPalakPaneer =
        day1.meals().stream().anyMatch(m -> m.recipe().candidateId().equals("r_palak_paneer"));

    assertTrue(hasPalakPaneer, "Recipe using expiring ingredient must be prioritized early!");
  }

  @Test
  @DisplayName("Locked meals survive plan regeneration")
  void testLockedMealsPreservation() {
    LocalDate start = LocalDate.now();
    LocalDate monday = start;

    RecipeCandidate customDinner = sampleCatalog.get(4); // paneer bhurji
    MealSlotProposal lockedDinner = MealSlotProposal.locked(customDinner, "DINNER", 4);

    Map<LocalDate, Map<String, MealSlotProposal>> lockedMap =
        Map.of(monday, Map.of("DINNER", lockedDinner));

    WeeklyMealPlan plan =
        planner.generateIntelligentWeeklyPlan(
            start, vegetarianProfile, sampleCatalog, Map.of(), Set.of(), lockedMap, 1800);

    DailyMealPlan day1 = plan.days().get(0);
    MealSlotProposal scheduledDinner =
        day1.meals().stream()
            .filter(m -> "DINNER".equalsIgnoreCase(m.mealSlot()))
            .findFirst()
            .orElseThrow();

    assertEquals("r_paneer_bhurji", scheduledDinner.recipe().candidateId());
    assertTrue(scheduledDinner.locked());
    assertEquals(4, scheduledDinner.servings());
  }

  @Test
  @DisplayName("One-tap meal replacement returns suitable safe alternatives")
  void testReplacementAlternatives() {
    RecipeCandidate currentBreakfast = sampleCatalog.get(0); // poha

    List<RecipeCandidate> alternatives =
        planner.findReplacementOptions(
            currentBreakfast, "BREAKFAST", vegetarianProfile, sampleCatalog, 3);

    assertFalse(alternatives.isEmpty());
    // Current recipe should not be in alternatives
    assertTrue(alternatives.stream().noneMatch(a -> a.candidateId().equals("r_poha")));
    // Alternatives must match BREAKFAST slot
    assertTrue(alternatives.stream().allMatch(a -> "BREAKFAST".equalsIgnoreCase(a.mealSlot())));
  }
}
