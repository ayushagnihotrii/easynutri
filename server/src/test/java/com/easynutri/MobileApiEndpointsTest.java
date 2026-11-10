package com.easynutri;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.easynutri.dto.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class MobileApiEndpointsTest {

  @Autowired private WebApplicationContext wac;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  @Test
  @DisplayName("Unauthenticated request to /api/v1/profile redirects to /login")
  void testUnauthenticatedApiRedirectsToLogin() throws Exception {
    mockMvc
        .perform(get("/api/v1/profile"))
        .andExpect(status().is3xxRedirection())
        .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/login")));
  }

  @Test
  @DisplayName(
      "POST /api/v1/auth/login with valid demo credentials establishes authenticated session")
  void testMobileLoginAndSessionPersistence() throws Exception {
    LoginRequest loginRequest = new LoginRequest("demo", "DevDemoPass2026!#");

    MvcResult loginResult =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.username").value("demo"))
            .andReturn();

    MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

    // Verify subsequent call with session succeeds
    mockMvc
        .perform(get("/api/v1/profile").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("demo"))
        .andExpect(jsonPath("$.targetCalories").isNumber());
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("GET /api/v1/foods returns food catalog items")
  void testGetFoods() throws Exception {
    mockMvc
        .perform(get("/api/v1/foods"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()", greaterThan(0)));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("GET /api/v1/recipes returns verified recipe items")
  void testGetRecipes() throws Exception {
    mockMvc
        .perform(get("/api/v1/recipes"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()", greaterThan(0)));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("GET /api/v1/dashboard returns unified daily metrics and suggested recipes")
  void testGetDashboard() throws Exception {
    mockMvc
        .perform(get("/api/v1/dashboard"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.targetCalories").isNumber())
        .andExpect(jsonPath("$.todayMeals").isArray())
        .andExpect(jsonPath("$.suggestedRecipes").isArray());
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /api/v1/profile/sensitive-mode toggles sensitive mode state")
  void testToggleSensitiveMode() throws Exception {
    mockMvc
        .perform(post("/api/v1/profile/sensitive-mode").param("enabled", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.sensitiveMode").value(true));
  }
}
