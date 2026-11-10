package com.easynutri.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.easynutri.domain.grocery.DefaultGroceryBuilder;
import com.easynutri.domain.model.RecipeCandidate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DefaultGroceryBuilderTest {

  private final DefaultGroceryBuilder groceryBuilder = new DefaultGroceryBuilder();

  @Test
  @DisplayName("Subtract existing pantry stock from recipe ingredient demand")
  void testPantrySubtraction() {
    RecipeCandidate khichdi =
        new RecipeCandidate(
            "r1",
            "Moong Khichdi",
            List.of("food_cooked_rice", "food_moong_dal"),
            "LUNCH",
            "MAIN",
            List.of());

    // Pantry has 100g rice and 50g moong dal
    // Demand is 150g each -> Required shopping: 50g rice, 100g moong dal
    Map<String, Double> pantry = Map.of("rice", 100.0, "moong dal", 50.0);

    Map<String, Double> shoppingList = groceryBuilder.buildGroceryList(List.of(khichdi), pantry);

    assertNotNull(shoppingList);
    assertTrue(shoppingList.containsKey("cooked rice"));
    assertTrue(shoppingList.containsKey("moong dal"));
    assertEquals(50.0, shoppingList.get("cooked rice"), 0.01);
    assertEquals(100.0, shoppingList.get("moong dal"), 0.01);
  }

  @Test
  @DisplayName("Items fully present in pantry are excluded from shopping list")
  void testFullyStockedItemExcluded() {
    RecipeCandidate simpleTea =
        new RecipeCandidate(
            "r2",
            "Masala Tea",
            List.of("food_tea_leaves", "food_milk"),
            "SNACK",
            "SNACK",
            List.of());

    // Pantry has 500g milk and 200g tea leaves (exceeds demand of 150g)
    Map<String, Double> pantry = Map.of("milk", 500.0, "tea leaves", 200.0);

    Map<String, Double> shoppingList = groceryBuilder.buildGroceryList(List.of(simpleTea), pantry);

    assertTrue(shoppingList.isEmpty(), "Fully stocked ingredients must not be on the grocery list");
  }

  @Test
  @DisplayName("Reconcile detailed demands with category grouping")
  void testReconciledListWithCategories() {
    Map<String, Double> demands =
        Map.of(
            "food_tomato", 400.0,
            "food_paneer", 300.0,
            "food_moong_dal", 250.0);

    Map<String, Double> pantry = Map.of("food_tomato", 150.0, "food_paneer", 300.0);

    List<DefaultGroceryBuilder.ReconciledItem> items =
        groceryBuilder.buildReconciledList(demands, Map.of(), pantry);

    assertEquals(3, items.size());

    // Paneer is fully stocked (toBuy = 0)
    var paneer =
        items.stream().filter(i -> i.foodId().contains("paneer")).findFirst().orElseThrow();
    assertEquals(0.0, paneer.toBuyGrams());
    assertEquals("DAIRY", paneer.category());

    // Tomato needs 250g purchase (400 - 150)
    var tomato =
        items.stream().filter(i -> i.foodId().contains("tomato")).findFirst().orElseThrow();
    assertEquals(250.0, tomato.toBuyGrams());
    assertEquals("VEGETABLES", tomato.category());
  }
}
