package com.easynutri.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.easynutri.domain.model.FoodCandidate;
import com.easynutri.domain.model.ProfileContext;
import com.easynutri.domain.nutrition.DefaultDeficiencyService;
import com.easynutri.domain.nutrition.NutrientInfo;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DefaultDeficiencyServiceTest {

  private final DefaultDeficiencyService deficiencyService = new DefaultDeficiencyService();

  @Test
  @DisplayName("Check supported 10 clinical nutrients are initialized")
  void testNutrientCatalog() {
    List<NutrientInfo> list = deficiencyService.getAllSupportedNutrients();
    assertEquals(10, list.size());

    NutrientInfo iron = deficiencyService.getNutrient("iron");
    assertNotNull(iron);
    assertEquals(19.0, iron.rdaMale());
    assertEquals(29.0, iron.rdaFemale());
    assertFalse(iron.absorptionTips().isEmpty());
  }

  @Test
  @DisplayName("Detect deficiency when intake is below 70% of ICMR RDA")
  void testDetectDeficiencies() {
    ProfileContext profile =
        new ProfileContext("p1", 28, "FEMALE", 160.0, 55.0, "VEGETARIAN", List.of(), List.of());

    // Iron RDA for female is 29mg; 10mg is well below 70% (20.3mg)
    // Calcium RDA is 1000mg; 400mg is below 70% (700mg)
    // Protein RDA is 46g; 50g is adequate
    Map<String, Double> intake =
        Map.of(
            "iron", 10.0,
            "calcium", 400.0,
            "protein", 50.0);

    Set<String> deficiencies = deficiencyService.detectDeficiencies(intake, profile);
    assertTrue(deficiencies.contains("iron"));
    assertTrue(deficiencies.contains("calcium"));
    assertFalse(deficiencies.contains("protein"));
  }

  @Test
  @DisplayName("Find foods rich in target nutrient and sort by density while respecting allergies")
  void testFindFoodsRichInWithAllergyFiltering() {
    ProfileContext profile =
        new ProfileContext(
            "p1", 28, "FEMALE", 160.0, 55.0, "VEGETARIAN", List.of("dairy"), List.of());

    FoodCandidate spinach =
        new FoodCandidate(
            "spinach",
            "Palak",
            "VEGETABLES",
            Map.of("iron_mg", 3.0),
            List.of(),
            List.of("VEGETARIAN"));

    FoodCandidate paneer =
        new FoodCandidate(
            "paneer",
            "Paneer",
            "DAIRY",
            Map.of("iron_mg", 0.5),
            List.of("dairy"),
            List.of("VEGETARIAN"));

    FoodCandidate moongDal =
        new FoodCandidate(
            "moong",
            "Moong Dal",
            "PULSES",
            Map.of("iron_mg", 3.9),
            List.of(),
            List.of("VEGETARIAN"));

    List<FoodCandidate> rich =
        deficiencyService.findFoodsRichIn("iron", List.of(spinach, paneer, moongDal), profile, 5);

    // Paneer should be excluded because profile has "dairy" allergy
    assertEquals(2, rich.size());
    // Moong dal (3.9mg) has more iron than spinach (3.0mg), so it ranks first
    assertEquals("moong", rich.get(0).candidateId());
    assertEquals("spinach", rich.get(1).candidateId());
  }
}
