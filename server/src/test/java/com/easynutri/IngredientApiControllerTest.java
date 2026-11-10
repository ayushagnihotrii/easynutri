package com.easynutri;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("dev")
class IngredientApiControllerTest {

  @Autowired private WebApplicationContext wac;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /api/v1/ingredients/resolve parses typed phrase into candidates")
  void testResolveIngredientsApi() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/ingredients/resolve")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\":\"200g paneer, 2 katori chawal\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].canonicalFoodId").value("food_paneer"))
        .andExpect(jsonPath("$[1].canonicalFoodId").value("food_cooked_rice"));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /api/v1/ingredients/barcode resolves valid barcode")
  void testBarcodeLookupApi() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/ingredients/barcode")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"barcode\":\"8901030000001\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("food_cooked_rice"));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /api/v1/ingredients/ocr parses receipt / packaging text")
  void testOcrApi() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/ingredients/ocr")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\":\"Whole Wheat Roti, Yellow Moong Dal, Tomato\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].canonicalFoodId").value("food_roti_chapati"));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /api/v1/ingredients/match returns ranked recipes with SafetyGate")
  void testMatchRecipesApi() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/ingredients/match")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ingredients\":[\"food_cooked_rice\",\"food_moong_dal\"]}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].recipeId").exists())
        .andExpect(jsonPath("$[0].matchScore").exists());
  }
}
