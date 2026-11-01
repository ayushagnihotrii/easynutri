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
class DashboardWebControllerTest {

  @Autowired private WebApplicationContext wac;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  @Test
  @DisplayName("GET / returns 200 and loads dashboard view for permitted landing")
  void testPublicDashboardLanding() throws Exception {
    mockMvc
        .perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(view().name("dashboard/index"))
        .andExpect(model().attributeExists("dailyCalorieGoal"))
        .andExpect(model().attributeExists("pantryAlerts"))
        .andExpect(model().attributeExists("sampleRecipes"));
  }

  @Test
  @WithMockUser(username = "sarah_athlete")
  @DisplayName("GET /dashboard with authenticated user displays personalized content")
  void testAuthenticatedDashboard() throws Exception {
    mockMvc
        .perform(get("/dashboard"))
        .andExpect(status().isOk())
        .andExpect(view().name("dashboard/index"))
        .andExpect(model().attribute("username", "sarah_athlete"));
  }
}
