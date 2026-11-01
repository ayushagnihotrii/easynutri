package com.easynutri.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.easynutri.domain.model.ProfileContext;
import com.easynutri.domain.recipe.SmartPantryRecommender;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SmartPantryRecommenderTest {

  @Test
  @DisplayName("Verify vegan profile receives plant protein instead of dairy curd/paneer")
  void testVeganPantryRecommendations() {
    ProfileContext veganProfile =
        new ProfileContext("u_vegan", 26, "FEMALE", 162.0, 54.0, "VEGAN", List.of(), List.of());

    var staples = SmartPantryRecommender.recommendStaples(veganProfile);

    assertFalse(staples.isEmpty());
    boolean hasDairy = staples.stream().anyMatch(s -> s.category().equals("DAIRY"));
    boolean hasTofu = staples.stream().anyMatch(s -> s.name().contains("Tofu"));

    assertFalse(hasDairy, "Vegan profile must not receive dairy recommendations");
    assertTrue(hasTofu, "Vegan profile must receive plant protein recommendations");
  }

  @Test
  @DisplayName("Verify gluten allergy profile receives sorghum/jowar instead of wheat atta")
  void testGlutenFreePantryRecommendations() {
    ProfileContext gfProfile =
        new ProfileContext(
            "u_gf", 32, "MALE", 178.0, 75.0, "VEGETARIAN", List.of("gluten"), List.of());

    var staples = SmartPantryRecommender.recommendStaples(gfProfile);

    boolean hasWheat = staples.stream().anyMatch(s -> s.name().contains("Wheat"));
    boolean hasJowar = staples.stream().anyMatch(s -> s.name().contains("Jowar"));

    assertFalse(hasWheat, "Gluten-allergic profile must not receive wheat recommendations");
    assertTrue(hasJowar, "Gluten-allergic profile must receive gluten-free millet recommendations");
  }
}
