package com.easynutri;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.easynutri.dto.BadgeDto;
import com.easynutri.dto.ChallengeDto;
import com.easynutri.dto.UserStreakDto;
import com.easynutri.entity.FoodDiaryEntryEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.FoodDiaryEntryRepository;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.GamificationService;
import java.time.LocalDate;
import java.util.List;
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
class AchievementsWebControllerTest {

  @Autowired private WebApplicationContext wac;
  @Autowired private GamificationService gamificationService;
  @Autowired private UserRepository userRepository;
  @Autowired private FoodDiaryEntryRepository diaryEntryRepository;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("GET /achievements renders compassionate habit streaks and badge collection")
  void testAchievementsDashboard() throws Exception {
    mockMvc
        .perform(get("/achievements"))
        .andExpect(status().isOk())
        .andExpect(view().name("achievements/index"))
        .andExpect(model().attributeExists("streaks"))
        .andExpect(model().attributeExists("badges"))
        .andExpect(model().attributeExists("sensitiveMode"))
        .andExpect(model().attribute("activeNav", "achievements"));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("Logging food diary automatically qualifies and awards FIRST_MEAL_LOGGED badge")
  void testBadgeAwarding() {
    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();

    // Ensure at least one diary entry exists
    FoodDiaryEntryEntity entry = new FoodDiaryEntryEntity(demo, LocalDate.now(), "BREAKFAST");
    diaryEntryRepository.save(entry);

    List<BadgeDto> badges = gamificationService.getUserBadges(demo.getId());
    assertFalse(badges.isEmpty());

    var firstMealBadge =
        badges.stream().filter(b -> "FIRST_MEAL_LOGGED".equals(b.id())).findFirst();
    assertTrue(firstMealBadge.isPresent());
    assertTrue(firstMealBadge.get().isUnlocked());
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("Habit streaks reflect 1-day compassionate grace period correctly")
  void testStreaksCalculation() {
    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();

    // Log yesterday only (today missing) -> should activate 1-day grace period
    diaryEntryRepository.deleteAll();
    FoodDiaryEntryEntity yesterdayEntry =
        new FoodDiaryEntryEntity(demo, LocalDate.now().minusDays(1), "LUNCH");
    diaryEntryRepository.save(yesterdayEntry);

    List<UserStreakDto> streaks = gamificationService.getUserStreaks(demo.getId());
    UserStreakDto diaryStreak =
        streaks.stream()
            .filter(s -> "DIARY_LOGGING".equals(s.streakType()))
            .findFirst()
            .orElseThrow();

    assertEquals(1, diaryStreak.currentStreakDays());
    assertTrue(diaryStreak.gracePeriodActive());
    assertFalse(diaryStreak.loggedToday());
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("GET /challenges renders community challenges and supports joining")
  void testChallengesWorkflow() throws Exception {
    mockMvc
        .perform(get("/challenges"))
        .andExpect(status().isOk())
        .andExpect(view().name("challenges/index"))
        .andExpect(model().attributeExists("challenges"))
        .andExpect(model().attribute("activeNav", "challenges"));

    // Join challenge
    mockMvc
        .perform(post("/challenges/HYDRATION_CONSISTENCY_7D/join").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/challenges"))
        .andExpect(flash().attributeExists("flashMessage"));

    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();
    List<ChallengeDto> challenges = gamificationService.getChallenges(demo.getId());
    ChallengeDto hydrationChallenge =
        challenges.stream()
            .filter(c -> "HYDRATION_CONSISTENCY_7D".equals(c.id()))
            .findFirst()
            .orElseThrow();

    assertTrue(hydrationChallenge.isJoined());
  }
}
