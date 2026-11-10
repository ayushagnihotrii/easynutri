package com.easynutri;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.easynutri.dto.HouseholdDto;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.HouseholdService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class SecurityAndAuthorizationTest {

  @Autowired private WebApplicationContext wac;
  @Autowired private UserRepository userRepository;
  @Autowired private HouseholdService householdService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  @Test
  @WithMockUser(
      username = "demo",
      roles = {"USER"})
  @DisplayName("Non-admin user accessing /admin/moderation is blocked with HTTP 403 Forbidden")
  void testNonAdminBlockedFromModeration() throws Exception {
    mockMvc.perform(get("/admin/moderation")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(
      username = "admin",
      roles = {"ADMIN"})
  @DisplayName("Admin user accessing /admin/moderation succeeds with HTTP 200 OK")
  void testAdminAllowedOnModeration() throws Exception {
    mockMvc
        .perform(get("/admin/moderation"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/moderation"));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName(
      "Security: Cross-household access in /api/v1/household/recommendations is rejected with HTTP 403")
  void testCrossHouseholdAccessBlocked() throws Exception {
    // Create an independent second user and their household
    UserEntity otherUser =
        userRepository.save(
            new UserEntity("other_user", "other@example.com", "hash", "Other User"));
    HouseholdDto otherHousehold =
        householdService.createHousehold(otherUser.getId(), "Other Household");

    // "demo" attempts to access "otherHousehold" recommendations
    mockMvc
        .perform(
            get("/api/v1/household/recommendations")
                .param("householdId", String.valueOf(otherHousehold.id())))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value("Forbidden"))
        .andExpect(jsonPath("$.message", containsString("User does not belong to household")));
  }

  @Test
  @DisplayName(
      "Response includes modern HTTP security headers (CSP, X-Content-Type-Options, Frame-Options)")
  void testSecurityHeadersPresent() throws Exception {
    mockMvc
        .perform(get("/login"))
        .andExpect(status().isOk())
        .andExpect(header().string("X-Content-Type-Options", "nosniff"))
        .andExpect(header().string("X-Frame-Options", "SAMEORIGIN"))
        .andExpect(header().exists("Content-Security-Policy"))
        .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"));
  }
}
