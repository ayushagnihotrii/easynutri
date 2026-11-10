package com.easynutri;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.easynutri.dto.AddHouseholdMemberRequest;
import com.easynutri.dto.HouseholdDto;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.HouseholdService;
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
class HouseholdWebControllerTest {

  @Autowired private WebApplicationContext wac;
  @Autowired private HouseholdService householdService;
  @Autowired private UserRepository userRepository;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("GET /family renders household dashboard with member cards and active profile")
  void testFamilyDashboardLoads() throws Exception {
    mockMvc
        .perform(get("/family"))
        .andExpect(status().isOk())
        .andExpect(view().name("family/index"))
        .andExpect(model().attributeExists("household"))
        .andExpect(model().attributeExists("members"))
        .andExpect(model().attributeExists("activeProfileId"))
        .andExpect(model().attribute("activeNav", "family"));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /family/create creates household with authenticated user as owner")
  void testCreateHousehold() throws Exception {
    mockMvc
        .perform(post("/family/create").with(csrf()).param("name", "Sharma Household"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/family"))
        .andExpect(flash().attributeExists("flashMessage"));

    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();
    var householdOpt = householdService.getHouseholdForUser(demo.getId());
    assertTrue(householdOpt.isPresent());
    assertEquals("Sharma Household", householdOpt.get().name());
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName(
      "POST /family/members/add creates member with independent profile and calculated targets")
  void testAddMember() throws Exception {
    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();
    HouseholdDto household = householdService.createHousehold(demo.getId(), "Demo Household");

    mockMvc
        .perform(
            post("/family/members/add")
                .with(csrf())
                .param("householdId", String.valueOf(household.id()))
                .param("displayName", "Maya")
                .param("relationship", "SPOUSE")
                .param("role", "MEMBER")
                .param("age", "28")
                .param("biologicalSex", "FEMALE")
                .param("heightCm", "162.0")
                .param("weightKg", "58.0")
                .param("activityLevel", "MODERATE")
                .param("healthGoal", "MAINTAIN")
                .param("dietaryPreference", "VEGETARIAN")
                .param("allergies", "Peanuts, Dairy")
                .param("sensitiveMode", "true")
                .param("avatarColor", "#2563EB"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/family"))
        .andExpect(flash().attributeExists("flashMessage"));

    var members = householdService.getHouseholdMembers(household.id(), demo.getId());
    assertEquals(2, members.size());

    var maya =
        members.stream().filter(m -> "Maya".equals(m.displayName())).findFirst().orElseThrow();
    assertEquals("SPOUSE", maya.relationship());
    assertEquals("VEGETARIAN", maya.dietaryPreference());
    assertTrue(maya.sensitiveMode());
    assertTrue(maya.allergies().contains("Peanuts"));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /family/switch/{profileId} updates active profile context in session")
  void testSwitchProfile() throws Exception {
    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();
    HouseholdDto household = householdService.createHousehold(demo.getId(), "Demo Household");

    var added =
        householdService.addMember(
            demo.getId(),
            household.id(),
            new AddHouseholdMemberRequest(
                "Kiddo",
                "CHILD",
                "DEPENDENT",
                8,
                "MALE",
                125.0,
                26.0,
                "MODERATE",
                "MAINTAIN",
                "VEGETARIAN",
                List.of(),
                List.of(),
                false,
                "#D97706"));

    // Switch to Kiddo's profile
    mockMvc
        .perform(post("/family/switch/" + added.userId()).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attribute("flashMessage", "Active profile switched successfully."));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName(
      "Profile isolation: prevents unauthorized switching into another household's profile")
  void testProfileIsolationSecurity() throws Exception {
    // Other user not in demo's household
    UserEntity stranger =
        userRepository.save(
            new UserEntity("stranger", "stranger@other.local", "hashedpass", "Stranger"));

    mockMvc
        .perform(post("/family/switch/" + stranger.getId()).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(flash().attributeExists("flashError"));
  }
}
