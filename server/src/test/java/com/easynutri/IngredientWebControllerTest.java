package com.easynutri;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class IngredientWebControllerTest {

  @Autowired private WebApplicationContext wac;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("GET /ingredients returns 200 with ingredient finder page")
  void testIngredientsPage() throws Exception {
    mockMvc
        .perform(get("/ingredients"))
        .andExpect(status().isOk())
        .andExpect(view().name("ingredients/finder"))
        .andExpect(model().attributeExists("pantryStaples"))
        .andExpect(model().attributeExists("matches"));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /ingredients/match returns ranked recipes with SafetyGate evaluation")
  void testMatchIngredients() throws Exception {
    mockMvc
        .perform(
            post("/ingredients/match")
                .with(csrf())
                .param("ingredients", "food_cooked_rice,food_moong_dal,food_tomato")
                .param("leftoverMode", "false"))
        .andExpect(status().isOk())
        .andExpect(view().name("ingredients/finder"))
        .andExpect(model().attributeExists("matches"));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /ingredients/match with Leftover Mode triggers microbiological safety checks")
  void testLeftoverMatch() throws Exception {
    mockMvc
        .perform(
            post("/ingredients/match")
                .with(csrf())
                .param("ingredients", "food_cooked_rice")
                .param("leftoverMode", "true")
                .param("leftoverIngredients", "food_cooked_rice")
                .param("hoursAgo", "3")
                .param("storageMethod", "REFRIGERATED"))
        .andExpect(status().isOk())
        .andExpect(view().name("ingredients/finder"))
        .andExpect(model().attribute("leftoverMode", true));
  }
}
