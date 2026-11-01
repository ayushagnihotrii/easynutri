package com.easynutri;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.easynutri.service.UserProfileService;
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
class OnboardingWebControllerTest {

  @Autowired private WebApplicationContext wac;
  @Autowired private UserProfileService profileService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("GET /onboarding displays onboarding wizard view")
  void testShowOnboardingWizard() throws Exception {
    mockMvc
        .perform(get("/onboarding"))
        .andExpect(status().isOk())
        .andExpect(view().name("onboarding/wizard"))
        .andExpect(model().attributeExists("onboardingForm"));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /onboarding processes biometrics and persists calculated targets")
  void testProcessOnboarding() throws Exception {
    mockMvc
        .perform(
            post("/onboarding")
                .with(csrf())
                .param("age", "32")
                .param("biologicalSex", "FEMALE")
                .param("heightCm", "165.0")
                .param("weightKg", "62.0")
                .param("activityLevel", "MODERATE")
                .param("healthGoal", "LOSE_WEIGHT")
                .param("dietaryPreference", "VEGETARIAN")
                .param("allergies", "peanut")
                .param("healthConditions", "HYPERTENSION"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/dashboard?onboarded=true"));

    assertTrue(profileService.hasCompletedOnboarding("demo"));
    var profile = profileService.findByUsername("demo").orElseThrow();
    assertTrue(profile.getTargetCalories() > 1200);
    assertTrue(profile.getAllergies().contains("peanut"));
    assertTrue(profile.getHealthConditions().contains("HYPERTENSION"));
  }
}
