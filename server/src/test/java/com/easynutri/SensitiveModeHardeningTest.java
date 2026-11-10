package com.easynutri;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.easynutri.domain.policy.DisplayPolicy;
import com.easynutri.dto.DiaryDayDto;
import com.easynutri.entity.UserEntity;
import com.easynutri.entity.UserProfileEntity;
import com.easynutri.repository.UserProfileRepository;
import com.easynutri.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
class SensitiveModeHardeningTest {

  @Autowired private WebApplicationContext wac;
  @Autowired private UserRepository userRepository;
  @Autowired private UserProfileRepository userProfileRepository;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  @Test
  @DisplayName("DisplayPolicy strictly masks weights, BMI, calories, and deficit in sensitive mode")
  void testDisplayPolicyMaskingRules() {
    // Standard Mode (sensitiveMode = false)
    assertEquals("72.5 kg", DisplayPolicy.formatWeight(72.5, false));
    assertEquals("23.4", DisplayPolicy.formatBmi(23.4, false));
    assertEquals("+1.5 kg", DisplayPolicy.formatWeightChange(1.5, false));
    assertEquals("-0.8 kg", DisplayPolicy.formatWeightChange(-0.8, false));
    assertEquals("2100 kcal", DisplayPolicy.formatCalories(2100.0, false));
    assertEquals("75g", DisplayPolicy.formatMacroGrams(75.0, false));
    assertEquals("+250 kcal Surplus", DisplayPolicy.formatEnergyBalance(250, false));
    assertEquals("-350 kcal Deficit", DisplayPolicy.formatEnergyBalance(-350, false));

    // Sensitive Mode (sensitiveMode = true)
    assertEquals("-- kg (Masked)", DisplayPolicy.formatWeight(72.5, true));
    assertEquals("Healthy Balance (Masked)", DisplayPolicy.formatBmi(23.4, true));
    assertEquals("Gentle Body Rhythm", DisplayPolicy.formatWeightChange(1.5, true));
    assertEquals("Gentle Body Rhythm", DisplayPolicy.formatWeightChange(-0.8, true));
    assertEquals("Balanced", DisplayPolicy.formatCalories(2100.0, true));
    assertEquals("Nourishing", DisplayPolicy.formatMacroGrams(75.0, true));
    assertEquals("Sustained Energy", DisplayPolicy.formatEnergyBalance(250, true));
    assertEquals("Gentle Energy Balance", DisplayPolicy.formatEnergyBalance(-350, true));
    assertEquals("Well Balanced", DisplayPolicy.formatEnergyBalance(0, true));
  }

  @Test
  @DisplayName(
      "DisplayPolicy check-in messages provide positive reinforcement without scale/calorie fixation")
  void testDisplayPolicyCheckInMessages() {
    String sensitiveMsg = DisplayPolicy.getCheckInMessage(true);
    assertTrue(
        sensitiveMsg.contains("daily check-in"), "Sensitive message should offer gentle check-in");
    assertFalse(sensitiveMsg.contains("kcal"), "Sensitive message must never leak kcal numbers");
    assertFalse(sensitiveMsg.contains("kg"), "Sensitive message must never leak weight numbers");

    String standardMsg = DisplayPolicy.getCheckInMessage(false);
    assertNotNull(standardMsg);
    assertFalse(standardMsg.isBlank());
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName(
      "Sensitive user visiting /reports receives masked metrics and sensitive privacy badge")
  void testReportsWithSensitiveMode() throws Exception {
    UserEntity user = userRepository.findByUsername("demo").orElseThrow();
    UserProfileEntity profile = userProfileRepository.findByUserId(user.getId()).orElseThrow();
    profile.setSensitiveMode(true);
    userProfileRepository.save(profile);

    mockMvc
        .perform(get("/reports"))
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("report"))
        .andExpect(content().string(containsString("Sensitive Privacy Shield")));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("Sensitive user visiting /diary has diaryDay.sensitiveMode = true")
  void testDiaryWithSensitiveMode() throws Exception {
    UserEntity user = userRepository.findByUsername("demo").orElseThrow();
    UserProfileEntity profile = userProfileRepository.findByUserId(user.getId()).orElseThrow();
    profile.setSensitiveMode(true);
    userProfileRepository.save(profile);

    MvcResult result =
        mockMvc
            .perform(get("/diary"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("diaryDay"))
            .andReturn();

    DiaryDayDto diaryDay = (DiaryDayDto) result.getModelAndView().getModel().get("diaryDay");
    assertNotNull(diaryDay);
    assertTrue(diaryDay.sensitiveMode());
  }
}
