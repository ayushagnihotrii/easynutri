package com.easynutri;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("dev")
class FoodWebControllerTest {

  @Autowired private WebApplicationContext wac;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("GET /recipes returns 200 with list of clinical recipes")
  void testListRecipes() throws Exception {
    mockMvc
        .perform(get("/recipes"))
        .andExpect(status().isOk())
        .andExpect(view().name("recipes/index"))
        .andExpect(model().attributeExists("recipes"));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("GET /recipes/{id} with serving scaling recalculates nutritional values")
  void testRecipeDetailAndScaler() throws Exception {
    mockMvc
        .perform(get("/recipes/recipe_moong_khichdi").param("servings", "4"))
        .andExpect(status().isOk())
        .andExpect(view().name("recipes/detail"))
        .andExpect(model().attributeExists("recipe"))
        .andExpect(model().attribute("servings", 4));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("GET /foods displays canonical food database with search and filters")
  void testFoodDatabaseListingAndSearch() throws Exception {
    mockMvc
        .perform(get("/foods").param("category", "PULSES"))
        .andExpect(status().isOk())
        .andExpect(view().name("recipes/finder"))
        .andExpect(model().attributeExists("foods"))
        .andExpect(model().attribute("currentCategory", "PULSES"));

    mockMvc
        .perform(get("/foods").param("q", "Rice"))
        .andExpect(status().isOk())
        .andExpect(view().name("recipes/finder"))
        .andExpect(model().attributeExists("foods"));
  }
}
