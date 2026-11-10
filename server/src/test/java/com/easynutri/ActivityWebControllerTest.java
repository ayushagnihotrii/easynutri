package com.easynutri;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.easynutri.dto.ActivitySummaryDto;
import com.easynutri.dto.LogStepsRequest;
import com.easynutri.dto.LogWorkoutRequest;
import com.easynutri.dto.WorkoutLogDto;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.ActivityTrackingService;
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
class ActivityWebControllerTest {

  @Autowired private WebApplicationContext wac;
  @Autowired private ActivityTrackingService trackingService;
  @Autowired private UserRepository userRepository;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("GET /tracking renders activity dashboard and energy expenditure budget")
  void testActivityPageLoads() throws Exception {
    mockMvc
        .perform(get("/tracking"))
        .andExpect(status().isOk())
        .andExpect(view().name("tracking/index"))
        .andExpect(model().attributeExists("summary"))
        .andExpect(model().attribute("activeNav", "tracking"));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /tracking/steps records user steps and updates goal progress")
  void testLogSteps() throws Exception {
    LocalDate today = LocalDate.now();

    mockMvc
        .perform(
            post("/tracking/steps")
                .with(csrf())
                .param("date", today.toString())
                .param("steps", "8500")
                .param("goalSteps", "10000"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/tracking?date=" + today));

    UserEntity user = userRepository.findByUsername("demo").orElseThrow();
    ActivitySummaryDto summary = trackingService.getActivitySummary(user.getId(), today);
    assertEquals(8500, summary.steps());
    assertEquals(10000, summary.goalSteps());
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /tracking/workout calculates MET burn and deduplicates non-workout steps")
  void testLogWorkoutAndDeduplication() throws Exception {
    UserEntity user = userRepository.findByUsername("demo").orElseThrow();
    LocalDate today = LocalDate.now();

    // 1. Log 10,000 steps
    trackingService.logSteps(user.getId(), new LogStepsRequest(today, 10000, 10000, "MANUAL"));

    // 2. Log 30 min brisk walk
    mockMvc
        .perform(
            post("/tracking/workout")
                .with(csrf())
                .param("date", today.toString())
                .param("activityType", "WALKING")
                .param("intensity", "MODERATE")
                .param("durationMinutes", "30")
                .param("distanceKm", "2.8")
                .param("notes", "Evening brisk walk in neighborhood"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/tracking?date=" + today));

    ActivitySummaryDto summary = trackingService.getActivitySummary(user.getId(), today);
    assertFalse(summary.workouts().isEmpty());
    assertTrue(summary.totalWorkoutCalories() > 0);
    // Net step burn should be strictly less than unadjusted step burn because workout steps are
    // deducted
    assertTrue(summary.netStepBurnKcal() < summary.stepCalories());
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /tracking/workout/delete/{id} removes workout log")
  void testDeleteWorkout() throws Exception {
    UserEntity user = userRepository.findByUsername("demo").orElseThrow();
    LocalDate today = LocalDate.now();

    WorkoutLogDto workout =
        trackingService.logWorkout(
            user.getId(),
            new LogWorkoutRequest(today, "YOGA", "LIGHT", 45, null, null, "Morning flow"));

    mockMvc
        .perform(
            post("/tracking/workout/delete/" + workout.id())
                .with(csrf())
                .param("date", today.toString()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/tracking?date=" + today));
  }
}
