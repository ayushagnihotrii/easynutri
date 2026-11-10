package com.easynutri.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.easynutri.domain.model.IngredientCandidate;
import com.easynutri.domain.recipe.DefaultIngredientResolver;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DefaultIngredientResolverTest {

  private final DefaultIngredientResolver resolver = new DefaultIngredientResolver();

  @Test
  @DisplayName("Resolve standard English and Hinglish ingredients")
  void testResolveBasicIngredients() {
    IngredientCandidate rice = resolver.resolve("rice");
    assertEquals("food_cooked_rice", rice.canonicalFoodId());
    assertEquals("GRAINS", rice.category());

    IngredientCandidate chawal = resolver.resolve("chawal");
    assertEquals("food_cooked_rice", chawal.canonicalFoodId());

    IngredientCandidate paneer = resolver.resolve("paneer");
    assertEquals("food_paneer", paneer.canonicalFoodId());
    assertEquals("DAIRY", paneer.category());
  }

  @Test
  @DisplayName("Resolve Hindi Devanagari ingredients")
  void testResolveHindiIngredients() {
    IngredientCandidate dal = resolver.resolve("मूंग दाल");
    assertEquals("food_moong_dal", dal.canonicalFoodId());
    assertEquals("PULSES", dal.category());

    IngredientCandidate aloo = resolver.resolve("आलू");
    assertEquals("food_potato_aloo", aloo.canonicalFoodId());
  }

  @Test
  @DisplayName("Extract quantity and units accurately")
  void testQuantityAndUnitExtraction() {
    IngredientCandidate item1 = resolver.resolve("200g paneer");
    assertEquals("food_paneer", item1.canonicalFoodId());
    assertEquals(200.0, item1.quantity());
    assertEquals(200.0, item1.quantityGrams());

    IngredientCandidate item2 = resolver.resolve("2 katori chawal");
    assertEquals("food_cooked_rice", item2.canonicalFoodId());
    assertEquals(2.0, item2.quantity());
    assertEquals(300.0, item2.quantityGrams()); // 2 * 150g
  }

  @Test
  @DisplayName("Parse compound multi-ingredient phrase from voice or typed input")
  void testResolveCompoundPhrase() {
    String input = "I have rice, 200g paneer, tomato and 2 onions";
    List<IngredientCandidate> candidates = resolver.resolveAll(input);

    assertEquals(4, candidates.size());
    assertEquals("food_cooked_rice", candidates.get(0).canonicalFoodId());
    assertEquals("food_paneer", candidates.get(1).canonicalFoodId());
    assertEquals("food_tomato", candidates.get(2).canonicalFoodId());
    assertEquals("food_onion", candidates.get(3).canonicalFoodId());
  }
}
