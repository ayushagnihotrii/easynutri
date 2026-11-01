package com.easynutri.server;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.easynutri.server.api.AuthStubController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AuthStubControllerTest {

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(new AuthStubController()).build();
  }

  @Test
  @DisplayName("POST /api/v1/auth/login returns STUBBED status explaining Phase 6 delivery")
  void testLoginPlaceholder() throws Exception {
    mockMvc
        .perform(post("/api/v1/auth/login"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("STUBBED"))
        .andExpect(jsonPath("$.message").isNotEmpty());
  }

  @Test
  @DisplayName("POST /api/v1/auth/register returns STUBBED status explaining Phase 6 delivery")
  void testRegisterPlaceholder() throws Exception {
    mockMvc
        .perform(post("/api/v1/auth/register"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("STUBBED"))
        .andExpect(jsonPath("$.message").isNotEmpty());
  }
}
