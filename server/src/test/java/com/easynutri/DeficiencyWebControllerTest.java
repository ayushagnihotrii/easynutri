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
class DeficiencyWebControllerTest {

  @Autowired private WebApplicationContext wac;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName(
      "GET /deficiency-helper loads default Iron view with ICMR 2024 target and ranked sources")
  void testDefaultDeficiencyView() throws Exception {
    mockMvc
        .perform(get("/deficiency-helper"))
        .andExpect(status().isOk())
        .andExpect(view().name("deficiency/index"))
        .andExpect(model().attributeExists("allNutrients"))
        .andExpect(model().attributeExists("currentNutrient"))
        .andExpect(model().attributeExists("targetRda"))
        .andExpect(model().attributeExists("richFoods"))
        .andExpect(model().attributeExists("suggestedRecipes"));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("GET /deficiency-helper?nutrient=calcium switches nutrient to Calcium")
  void testCalciumDeficiencyView() throws Exception {
    mockMvc
        .perform(get("/deficiency-helper").param("nutrient", "calcium"))
        .andExpect(status().isOk())
        .andExpect(view().name("deficiency/index"))
        .andExpect(model().attributeExists("currentNutrient"))
        .andExpect(model().attribute("targetRda", 1000.0));
  }
}
