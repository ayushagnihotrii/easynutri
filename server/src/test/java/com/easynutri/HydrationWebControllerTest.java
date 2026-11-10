package com.easynutri;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.easynutri.dto.HydrationSummaryDto;
import com.easynutri.dto.LogWaterRequest;
import com.easynutri.dto.WaterLogDto;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.HydrationService;
import java.time.LocalDate;
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
class HydrationWebControllerTest {

  @Autowired private WebApplicationContext wac;
  @Autowired private HydrationService hydrationService;
  @Autowired private UserRepository userRepository;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("GET /hydration renders water intake tracker and clinical targets")
  void testHydrationPageLoads() throws Exception {
    mockMvc
        .perform(get("/hydration"))
        .andExpect(status().isOk())
        .andExpect(view().name("hydration/index"))
        .andExpect(model().attributeExists("summary"))
        .andExpect(model().attribute("activeNav", "hydration"));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /hydration/log adds water portion and updates daily intake")
  void testLogWater() throws Exception {
    LocalDate today = LocalDate.now();

    mockMvc
        .perform(
            post("/hydration/log")
                .with(csrf())
                .param("date", today.toString())
                .param("amountMl", "250")
                .param("notes", "Glass of water"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/hydration?date=" + today));

    UserEntity user = userRepository.findByUsername("demo").orElseThrow();
    HydrationSummaryDto summary = hydrationService.getHydrationSummary(user.getId(), today);
    assertTrue(summary.currentIntakeMl() >= 250);
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /hydration/preferences saves daily target and reminder settings")
  void testSavePreferences() throws Exception {
    LocalDate today = LocalDate.now();

    mockMvc
        .perform(
            post("/hydration/preferences")
                .with(csrf())
                .param("date", today.toString())
                .param("dailyTargetMl", "2800")
                .param("reminderEnabled", "true")
                .param("reminderIntervalHours", "1")
                .param("startTime", "07:30")
                .param("endTime", "23:00"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/hydration?date=" + today));

    UserEntity user = userRepository.findByUsername("demo").orElseThrow();
    HydrationSummaryDto summary = hydrationService.getHydrationSummary(user.getId(), today);
    assertEquals(1, summary.reminderIntervalHours());
    assertTrue(summary.reminderEnabled());
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /hydration/delete/{id} deletes water log entry")
  void testDeleteWaterLog() throws Exception {
    UserEntity user = userRepository.findByUsername("demo").orElseThrow();
    LocalDate today = LocalDate.now();

    WaterLogDto log =
        hydrationService.logWater(
            user.getId(), new LogWaterRequest(today, 350, "Mug of hot water"));

    mockMvc
        .perform(post("/hydration/delete/" + log.id()).with(csrf()).param("date", today.toString()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/hydration?date=" + today));
  }
}
