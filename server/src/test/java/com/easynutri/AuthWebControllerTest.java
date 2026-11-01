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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("dev")
class AuthWebControllerTest {

  @Autowired private WebApplicationContext wac;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  @Test
  @DisplayName("GET /login returns login view")
  void testLoginPage() throws Exception {
    mockMvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("auth/login"));
  }

  @Test
  @DisplayName("GET /register returns registration view")
  void testRegisterPage() throws Exception {
    mockMvc
        .perform(get("/register"))
        .andExpect(status().isOk())
        .andExpect(view().name("auth/register"))
        .andExpect(model().attributeExists("registerForm"));
  }

  @Test
  @DisplayName("POST /register successfully registers a new user and redirects")
  void testRegisterSubmit() throws Exception {
    mockMvc
        .perform(
            post("/register")
                .with(csrf())
                .param("username", "testuser_" + System.currentTimeMillis())
                .param("email", "test_" + System.currentTimeMillis() + "@easynutri.com")
                .param("password", "SecurePassword123!")
                .param("fullName", "Test User"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login?registered=true"));
  }
}
