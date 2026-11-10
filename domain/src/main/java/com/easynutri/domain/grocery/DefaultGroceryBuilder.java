package com.easynutri.domain.grocery;

import com.easynutri.domain.model.RecipeCandidate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Pure Java implementation of GroceryBuilder for plan-to-pantry reconciliation. Aggregates
 * multi-recipe ingredient demands and subtracts existing pantry stock.
 */
public final class DefaultGroceryBuilder implements GroceryBuilder {

  public record ReconciledItem(
      String foodId,
      String itemName,
      double requiredGrams,
      double inPantryGrams,
      double toBuyGrams,
      String unit,
      String category) {}

  @Override
  public Map<String, Double> buildGroceryList(
      List<RecipeCandidate> plannedRecipes, Map<String, Double> pantryStock) {

    Objects.requireNonNull(plannedRecipes, "plannedRecipes cannot be null");
    Map<String, Double> safePantry = pantryStock != null ? pantryStock : Map.of();

    // 1. Consolidate demand from planned recipes (default assumption: 100g per ingredient reference
    // if not specified)
    Map<String, Double> requiredMap = new HashMap<>();
    for (RecipeCandidate recipe : plannedRecipes) {
      for (String ing : recipe.ingredientFoodIds()) {
        String clean = cleanName(ing);
        requiredMap.put(clean, requiredMap.getOrDefault(clean, 0.0) + 150.0);
      }
    }

    // 2. Subtract available pantry stock
    Map<String, Double> toBuyMap = new LinkedHashMap<>();
    for (Map.Entry<String, Double> entry : requiredMap.entrySet()) {
      String item = entry.getKey();
      double needed = entry.getValue();
      double inPantry = findPantryQuantity(item, safePantry);

      double deficit = needed - inPantry;
      if (deficit > 0.0) {
        toBuyMap.put(item, round(deficit, 1));
      }
    }

    return Collections.unmodifiableMap(toBuyMap);
  }

  @Override
  public Map<String, Double> consolidateIngredients(List<Map<String, Double>> ingredientDemands) {
    if (ingredientDemands == null) return Map.of();

    Map<String, Double> consolidated = new LinkedHashMap<>();
    for (Map<String, Double> demand : ingredientDemands) {
      for (Map.Entry<String, Double> entry : demand.entrySet()) {
        String key = cleanName(entry.getKey());
        consolidated.put(key, consolidated.getOrDefault(key, 0.0) + entry.getValue());
      }
    }
    return Collections.unmodifiableMap(consolidated);
  }

  /** Reconciles detailed ingredient demands against pantry stock with category grouping. */
  public List<ReconciledItem> buildReconciledList(
      Map<String, Double> demandsGrams,
      Map<String, String> foodCategories,
      Map<String, Double> pantryStockGrams) {

    Objects.requireNonNull(demandsGrams, "demandsGrams cannot be null");
    Map<String, Double> safePantry = pantryStockGrams != null ? pantryStockGrams : Map.of();
    Map<String, String> safeCats = foodCategories != null ? foodCategories : Map.of();

    List<ReconciledItem> items = new ArrayList<>();

    for (Map.Entry<String, Double> entry : demandsGrams.entrySet()) {
      String foodId = entry.getKey();
      double needed = entry.getValue();
      double inPantry = safePantry.getOrDefault(foodId, 0.0);

      double toBuy = Math.max(0.0, needed - inPantry);
      String category = safeCats.getOrDefault(foodId, categorizeFood(foodId));

      items.add(
          new ReconciledItem(
              foodId,
              cleanDisplayName(foodId),
              round(needed, 1),
              round(inPantry, 1),
              round(toBuy, 1),
              "g",
              category));
    }

    // Sort: items needing purchase first, then by category
    items.sort(
        Comparator.comparing((ReconciledItem r) -> r.toBuyGrams() > 0 ? 0 : 1)
            .thenComparing(ReconciledItem::category)
            .thenComparing(ReconciledItem::itemName));

    return Collections.unmodifiableList(items);
  }

  private double findPantryQuantity(String itemName, Map<String, Double> pantry) {
    String cleanTarget = cleanName(itemName);
    for (Map.Entry<String, Double> entry : pantry.entrySet()) {
      String cleanPantryKey = cleanName(entry.getKey());
      if (cleanTarget.contains(cleanPantryKey) || cleanPantryKey.contains(cleanTarget)) {
        return entry.getValue();
      }
    }
    return 0.0;
  }

  private String cleanName(String name) {
    if (name == null) return "";
    String cleaned = name.toLowerCase(Locale.ROOT).trim();
    if (cleaned.startsWith("food_")) {
      cleaned = cleaned.substring(5);
    }
    return cleaned.replace('_', ' ').trim();
  }

  private String cleanDisplayName(String foodId) {
    String name = cleanName(foodId);
    if (name.isEmpty()) return "General Item";
    return Arrays.stream(name.split(" "))
        .filter(s -> !s.isEmpty())
        .map(s -> Character.toUpperCase(s.charAt(0)) + (s.length() > 1 ? s.substring(1) : ""))
        .reduce((a, b) -> a + " " + b)
        .orElse(name);
  }

  private String categorizeFood(String foodId) {
    String lower = foodId.toLowerCase(Locale.ROOT);
    if (lower.contains("dal") || lower.contains("chana") || lower.contains("rajma"))
      return "PULSES";
    if (lower.contains("rice")
        || lower.contains("roti")
        || lower.contains("atta")
        || lower.contains("poha")) return "GRAINS";
    if (lower.contains("paneer")
        || lower.contains("curd")
        || lower.contains("milk")
        || lower.contains("dahi")) return "DAIRY";
    if (lower.contains("tomato")
        || lower.contains("onion")
        || lower.contains("spinach")
        || lower.contains("potato")
        || lower.contains("bhindi")) return "VEGETABLES";
    if (lower.contains("banana") || lower.contains("guava") || lower.contains("apple"))
      return "FRUITS";
    if (lower.contains("ghee")
        || lower.contains("oil")
        || lower.contains("jeera")
        || lower.contains("haldi")
        || lower.contains("masala")) return "SPICES";
    return "GENERAL";
  }

  private double round(double val, int places) {
    return BigDecimal.valueOf(val).setScale(places, RoundingMode.HALF_UP).doubleValue();
  }
}
