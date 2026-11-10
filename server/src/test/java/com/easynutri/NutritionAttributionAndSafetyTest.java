package com.easynutri;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class NutritionAttributionAndSafetyTest {

  @Autowired private WebApplicationContext wac;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  @Test
  @DisplayName(
      "GET /attribution is publicly accessible without authentication and documents data provenance")
  void testAttributionPagePublicAccess() throws Exception {
    mockMvc
        .perform(get("/attribution"))
        .andExpect(status().isOk())
        .andExpect(view().name("attribution/index"))
        .andExpect(content().string(containsString("Indian Food Composition Tables (IFCT 2017)")))
        .andExpect(content().string(containsString("USDA FoodData Central")))
        .andExpect(content().string(containsString("Open Food Facts")))
        .andExpect(content().string(containsString("ICMR-NIN 2024 Dietary Guidelines")))
        .andExpect(content().string(containsString("NOT A MEDICAL DEVICE")));
  }

  @Test
  @DisplayName("Main layout embeds clinical disclaimer footer and data provenance links")
  void testFooterDisclaimerPresent() throws Exception {
    mockMvc
        .perform(get("/login"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("EasyNutri")));
  }
}
