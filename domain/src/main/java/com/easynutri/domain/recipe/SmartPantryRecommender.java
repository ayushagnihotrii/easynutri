package com.easynutri.domain.recipe;

import com.easynutri.domain.model.ProfileContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Pure Java engine generating personalized smart pantry staple recommendations based on user
 * profiles.
 */
public final class SmartPantryRecommender {

  public record PantryRecommendation(
      String foodId, String name, String category, String rationale, String typicalStockUnit) {}

  private SmartPantryRecommender() {}

  /** Generates tailored pantry staple recommendations filtered by allergies and clinical rules. */
  public static List<PantryRecommendation> recommendStaples(ProfileContext profile) {
    Objects.requireNonNull(profile, "profile cannot be null");

    List<PantryRecommendation> list = new ArrayList<>();
    String diet =
        profile.dietType() != null ? profile.dietType().toUpperCase(Locale.ROOT) : "VEGETARIAN";
    List<String> allergies =
        profile.allergies().stream().map(a -> a.toLowerCase(Locale.ROOT)).toList();
    List<String> conditions =
        profile.healthConditions().stream().map(c -> c.toUpperCase(Locale.ROOT)).toList();

    boolean isVegan = diet.contains("VEGAN");
    boolean hasDairyAllergy =
        allergies.stream().anyMatch(a -> a.contains("dairy") || a.contains("milk"));
    boolean hasGlutenAllergy =
        allergies.stream().anyMatch(a -> a.contains("gluten") || a.contains("wheat"));
    boolean isDiabetic = conditions.stream().anyMatch(c -> c.contains("DIABETES"));
    boolean isHypertensive =
        conditions.stream().anyMatch(c -> c.contains("HYPERTENSION") || c.contains("HIGH_BP"));

    // 1. Grains
    if (!hasGlutenAllergy) {
      list.add(
          new PantryRecommendation(
              "food_wheat_atta",
              "Whole Wheat Flour (Atta)",
              "GRAINS",
              "Essential complex carbohydrate source for rotis with sustained energy release.",
              "5 kg"));
    } else {
      list.add(
          new PantryRecommendation(
              "food_jowar_flour",
              "Sorghum (Jowar) Flour",
              "GRAINS",
              "Nutritious, naturally gluten-free millet flour ideal for diabetic-friendly rotis.",
              "2 kg"));
    }

    if (isDiabetic) {
      list.add(
          new PantryRecommendation(
              "food_brown_rice",
              "Brown Rice / Millets",
              "GRAINS",
              "Lower glycemic index grain choice to mitigate blood glucose spikes.",
              "2 kg"));
    } else {
      list.add(
          new PantryRecommendation(
              "food_steamed_rice",
              "Steamed Basmati Rice",
              "GRAINS",
              "Versatile everyday grain pairing with lentils and curries.",
              "5 kg"));
    }

    // 2. Pulses / Legumes
    list.add(
        new PantryRecommendation(
            "food_moong_dal",
            "Yellow Moong Dal (Split Lentils)",
            "PULSES",
            "Easy-to-digest, high-protein staple suitable for all age groups and convalescence.",
            "1 kg"));

    list.add(
        new PantryRecommendation(
            "food_toor_dal",
            "Toor Dal (Pigeon Peas)",
            "PULSES",
            "Rich in dietary protein and iron; standard base for everyday sambar and dal tadka.",
            "1 kg"));

    list.add(
        new PantryRecommendation(
            "food_chana_dal",
            "Bengal Gram (Chana Dal)",
            "PULSES",
            "Very high fiber and low GI legume supporting steady energy and satiety.",
            "1 kg"));

    // 3. Dairy / Alternatives
    if (!isVegan && !hasDairyAllergy) {
      list.add(
          new PantryRecommendation(
              "food_fresh_curd",
              "Homemade Dahi / Curd",
              "DAIRY",
              "Natural probiotic culture aiding gut microbiome and delivering bioavailable calcium.",
              "500 g"));
      list.add(
          new PantryRecommendation(
              "food_paneer",
              "Fresh Paneer (Cottage Cheese)",
              "DAIRY",
              "Dense vegetarian complete protein source containing essential leucine.",
              "500 g"));
    } else {
      list.add(
          new PantryRecommendation(
              "food_tofu",
              "Organic Firm Tofu",
              "PLANT_PROTEIN",
              "Plant-based calcium-set protein providing all 9 essential amino acids without dairy.",
              "400 g"));
    }

    // 4. Vegetables & Seasonings
    list.add(
        new PantryRecommendation(
            "food_spinach",
            "Fresh Palak (Spinach)",
            "VEGETABLES",
            "Rich in non-heme iron, folate, and carotenoids.",
            "500 g"));

    if (isHypertensive) {
      list.add(
          new PantryRecommendation(
              "food_rock_salt",
              "Low-Sodium Rock Salt / Sendha Namak",
              "SEASONING",
              "Helps maintain sodium discipline under 2000mg/day clinical hypertension boundary.",
              "500 g"));
    }

    list.add(
        new PantryRecommendation(
            "food_turmeric",
            "Pure Haldi (Turmeric Powder)",
            "SPICES",
            "Contains active curcumin providing potent antioxidant and anti-inflammatory properties.",
            "200 g"));

    return List.copyOf(list);
  }
}
