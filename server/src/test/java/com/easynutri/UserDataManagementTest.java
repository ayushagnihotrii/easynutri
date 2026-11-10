package com.easynutri;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.easynutri.dto.RegisterRequest;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.UserService;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class UserDataManagementTest {

  @Autowired private WebApplicationContext wac;
  @Autowired private UserRepository userRepository;
  @Autowired private UserService userService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName(
      "GET /settings/export-data exports user's personal health telemetry as downloadable JSON")
  void testExportPersonalData() throws Exception {
    mockMvc
        .perform(get("/settings/export-data"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(
            header()
                .string(
                    "Content-Disposition",
                    containsString("attachment; filename=\"easynutri-export-demo-")))
        .andExpect(jsonPath("$.account.username").value("demo"))
        .andExpect(jsonPath("$.profile").exists())
        .andExpect(jsonPath("$.diaryEntries").isArray())
        .andExpect(jsonPath("$.formatVersion").value("EASYNUTRI_DPDP_V1"))
        .andExpect(jsonPath("$.legalNotice", containsString("ICMR-NIN IFCT 2017")));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /settings/delete-account with invalid password aborts and returns error")
  void testDeleteAccountInvalidPassword() throws Exception {
    mockMvc
        .perform(
            post("/settings/delete-account")
                .with(csrf())
                .param("confirmPassword", "WrongPassword!"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/settings"))
        .andExpect(flash().attributeExists("errorMessage"));

    assertTrue(
        userRepository.findByUsername("demo").isPresent(),
        "Demo user must not be deleted on wrong password");
  }

  @Test
  @DisplayName("POST /settings/delete-account with valid password permanently erases user records")
  void testDeleteAccountValidPassword() throws Exception {
    // Create dedicated user for deletion test
    UserEntity testUser =
        userService.registerUser(
            new RegisterRequest(
                "erasure_test", "erase@example.com", "SecretPass123!", "Erasure Test User"));

    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();

    mockMvc
        .perform(
            post("/settings/delete-account")
                .with(csrf())
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(testUser.getUsername()))
                .param("confirmPassword", "SecretPass123!"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login?deleted=true"));

    assertFalse(
        userRepository.findByUsername("erasure_test").isPresent(),
        "User must be completely erased from database");
  }
}
