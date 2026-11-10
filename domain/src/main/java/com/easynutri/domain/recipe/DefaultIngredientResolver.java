package com.easynutri.domain.recipe;

import com.easynutri.domain.model.IngredientCandidate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pure Java implementation of multi-lingual ingredient resolution supporting English, Hindi
 * Devanagari, and Hinglish transliterations with quantity & unit extraction.
 */
public final class DefaultIngredientResolver implements IngredientResolver {

  public record FoodMapping(
      String foodId, String nameEn, String nameHi, String category, double defaultGrams) {}

  private static final Map<String, FoodMapping> ALIAS_MAP = new HashMap<>();

  static {
    // Rice
    registerMapping(
        new FoodMapping("food_cooked_rice", "White Rice (Cooked)", "चावल", "GRAINS", 150.0),
        "rice",
        "white rice",
        "cooked rice",
        "chawal",
        "chaawal",
        "bhat",
        "चावल",
        "भात");
    registerMapping(
        new FoodMapping("food_brown_rice", "Brown Rice (Cooked)", "भूरा चावल", "GRAINS", 150.0),
        "brown rice",
        "bhura chawal",
        "भूरा चावल");

    // Roti / Wheat
    registerMapping(
        new FoodMapping("food_roti_chapati", "Whole Wheat Roti / Chapati", "रोटी", "GRAINS", 35.0),
        "roti",
        "chapati",
        "fulka",
        "phulka",
        "wheat roti",
        "atta",
        "gehun",
        "रोटी",
        "चपाती",
        "फुल्का");

    // Dals
    registerMapping(
        new FoodMapping("food_moong_dal", "Yellow Moong Dal (Cooked)", "मूंग दाल", "PULSES", 150.0),
        "moong dal",
        "yellow dal",
        "mung dal",
        "moong",
        "mung",
        "मूंग दाल",
        "मूंग");
    registerMapping(
        new FoodMapping("food_toor_dal", "Toor / Arhar Dal (Cooked)", "तूर दाल", "PULSES", 150.0),
        "toor dal",
        "arhar dal",
        "tuvar dal",
        "toor",
        "arhar",
        "tuvar",
        "pigeon pea",
        "तूर दाल",
        "अरहर दाल");
    registerMapping(
        new FoodMapping("food_chana_dal", "Chana Dal (Bengal Gram)", "चना दाल", "PULSES", 150.0),
        "chana dal",
        "chana",
        "bengal gram",
        "चना दाल",
        "चना");
    registerMapping(
        new FoodMapping("food_rajma", "Kidney Beans (Rajma)", "राजमा", "PULSES", 150.0),
        "rajma",
        "kidney beans",
        "राजमा");

    // Dairy & Alternatives
    registerMapping(
        new FoodMapping("food_paneer", "Fresh Paneer (Cottage Cheese)", "पनीर", "DAIRY", 100.0),
        "paneer",
        "cottage cheese",
        "panir",
        "पनीर");
    registerMapping(
        new FoodMapping("food_curd_dahi", "Whole Milk Curd / Dahi", "दही", "DAIRY", 150.0),
        "curd",
        "dahi",
        "yogurt",
        "yoghurt",
        "दही");
    registerMapping(
        new FoodMapping("food_tofu", "Firm Tofu (Soy Paneer)", "टोफू", "PLANT_PROTEIN", 100.0),
        "tofu",
        "soy paneer",
        "soya paneer",
        "टोफू");

    // Vegetables
    registerMapping(
        new FoodMapping("food_spinach_palak", "Fresh Spinach / Palak", "पालक", "VEGETABLES", 100.0),
        "spinach",
        "palak",
        "paalak",
        "saag",
        "पालक");
    registerMapping(
        new FoodMapping("food_bhindi_okra", "Okra / Bhindi", "भिंडी", "VEGETABLES", 100.0),
        "bhindi",
        "okra",
        "ladyfinger",
        "bhendi",
        "भिंडी");
    registerMapping(
        new FoodMapping("food_potato_aloo", "Boiled Potato / Aloo", "आलू", "VEGETABLES", 100.0),
        "potato",
        "aloo",
        "alu",
        "batata",
        "आलू");
    registerMapping(
        new FoodMapping("food_tomato", "Fresh Tomato", "टमाटर", "VEGETABLES", 80.0),
        "tomato",
        "tamatar",
        "tomatoes",
        "टमाटर");
    registerMapping(
        new FoodMapping("food_onion", "Fresh Onion", "प्याज", "VEGETABLES", 80.0),
        "onion",
        "pyaz",
        "pyaaz",
        "kanda",
        "onions",
        "प्याज",
        "कांदा");

    // Fruits
    registerMapping(
        new FoodMapping("food_banana", "Fresh Banana", "केला", "FRUITS", 100.0),
        "banana",
        "kela",
        "bananas",
        "केला");
    registerMapping(
        new FoodMapping("food_guava_amrood", "Fresh Guava / Amrood", "अमरूद", "FRUITS", 120.0),
        "guava",
        "amrood",
        "peru",
        "अमरूद");

    // Grains & Breakfast Staples
    registerMapping(
        new FoodMapping("food_poha", "Flattened Rice (Poha)", "पोहा", "GRAINS", 70.0),
        "poha",
        "flattened rice",
        "aval",
        "chivda",
        "पोहा");
    registerMapping(
        new FoodMapping("food_besan", "Bengal Gram Flour (Besan)", "बेसन", "PULSES", 50.0),
        "besan",
        "gram flour",
        "chickpea flour",
        "बेसन");
  }

  private static void registerMapping(FoodMapping mapping, String... aliases) {
    for (String alias : aliases) {
      ALIAS_MAP.put(alias.toLowerCase(Locale.ROOT).trim(), mapping);
    }
  }

  private static final Pattern QUANTITY_PATTERN =
      Pattern.compile(
          "(?i)(\\d+(?:\\.\\d+)?)\\s*(kg|g|grams?|kilos?|katori|bowls?|cups?|tbsp|tsp|pieces?|nag)?");

  @Override
  public IngredientCandidate resolve(String input) {
    Objects.requireNonNull(input, "input cannot be null");
    String trimmed = input.trim();
    if (trimmed.isEmpty()) {
      return IngredientCandidate.of("", "food_unknown", "Unknown", "GENERAL");
    }

    double quantity = 1.0;
    String unit = "serving";
    double grams = 100.0;
    String cleanName = trimmed;

    // Check for quantity and unit in input string e.g. "2 katori chawal" or "200g paneer"
    Matcher m = QUANTITY_PATTERN.matcher(trimmed);
    if (m.find()) {
      try {
        quantity = Double.parseDouble(m.group(1));
        if (m.group(2) != null) {
          unit = m.group(2).toLowerCase(Locale.ROOT);
        }
        cleanName = trimmed.substring(0, m.start()) + trimmed.substring(m.end());
        cleanName = cleanName.trim().replaceAll("^[\\s,:-]+|[\\s,:-]+$", "");
      } catch (NumberFormatException ignored) {
      }
    }

    String lowerClean = cleanName.toLowerCase(Locale.ROOT).trim();
    // Remove common filler words
    lowerClean = lowerClean.replaceAll("^(some|a few|fresh|raw|cooked|leftover|boiled)\\s+", "");

    FoodMapping mapping = lookupMapping(lowerClean);

    if (mapping != null) {
      grams = calculateGrams(quantity, unit, mapping.defaultGrams());
      return new IngredientCandidate(
          trimmed,
          mapping.foodId(),
          mapping.nameEn(),
          mapping.nameHi(),
          mapping.category(),
          quantity,
          unit,
          grams,
          trimmed.toLowerCase(Locale.ROOT).contains("leftover")
              || trimmed.toLowerCase(Locale.ROOT).contains("cooked"),
          0,
          "REFRIGERATED");
    }

    // Fallback: Return as unmapped candidate
    return new IngredientCandidate(
        trimmed,
        "food_" + lowerClean.replaceAll("[^a-z0-9]+", "_"),
        capitalize(cleanName),
        "",
        "GENERAL",
        quantity,
        unit,
        quantity > 10.0 ? quantity : 100.0,
        trimmed.toLowerCase(Locale.ROOT).contains("leftover"),
        0,
        "FRESH");
  }

  @Override
  public List<IngredientCandidate> resolveAll(String text) {
    if (text == null || text.isBlank()) {
      return List.of();
    }

    // Clean conversational voice prefixes like "I have", "we have", "there are", "I got"
    String normalized =
        text.replaceAll("(?i)^(i have|we have|i got|there is|there are|mere paas)\\s+", "");
    // Split on delimiters: comma, "and", "aur", "plus", semicolon, newline
    String[] parts = normalized.split("(?i)\\s*(?:,|\\band\\b|\\baur\\b|\\bplus\\b|;|\\n)\\s*");

    List<IngredientCandidate> result = new ArrayList<>();
    for (String part : parts) {
      String clean = part.trim();
      if (!clean.isEmpty()) {
        result.add(resolve(clean));
      }
    }
    return List.copyOf(result);
  }

  private FoodMapping lookupMapping(String query) {
    if (ALIAS_MAP.containsKey(query)) {
      return ALIAS_MAP.get(query);
    }

    // Substring / fuzzy contains match
    for (Map.Entry<String, FoodMapping> entry : ALIAS_MAP.entrySet()) {
      if (query.contains(entry.getKey()) || entry.getKey().contains(query)) {
        return entry.getValue();
      }
    }
    return null;
  }

  private double calculateGrams(double qty, String unit, double defaultUnitGrams) {
    return switch (unit.toLowerCase(Locale.ROOT)) {
      case "kg", "kilo", "kilos" -> qty * 1000.0;
      case "g", "gram", "grams" -> qty;
      case "katori", "bowl", "bowls" -> qty * 150.0;
      case "cup", "cups" -> qty * 200.0;
      case "tbsp", "chammach" -> qty * 15.0;
      case "tsp" -> qty * 5.0;
      case "piece", "pieces", "nag" -> qty * defaultUnitGrams;
      default -> qty * defaultUnitGrams;
    };
  }

  private String capitalize(String str) {
    if (str == null || str.isEmpty()) return str;
    return Character.toUpperCase(str.charAt(0)) + (str.length() > 1 ? str.substring(1) : "");
  }
}
