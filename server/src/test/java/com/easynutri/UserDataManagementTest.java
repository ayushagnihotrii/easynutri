package com.easynutri;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.easynutri.dto.HouseholdDto;
import com.easynutri.dto.RegisterRequest;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.HouseholdMemberRepository;
import com.easynutri.repository.HouseholdRepository;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.HouseholdService;
import com.easynutri.service.UserService;
import java.util.List;
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
  @Autowired private HouseholdService householdService;
  @Autowired private HouseholdRepository householdRepository;
  @Autowired private HouseholdMemberRepository householdMemberRepository;

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
        .andExpect(jsonPath("$.weightHistory").isArray())
        .andExpect(jsonPath("$.mealPlans").isArray())
        .andExpect(jsonPath("$.savedRecipes").isArray())
        .andExpect(jsonPath("$.pantryStock").isArray())
        .andExpect(jsonPath("$.notifications").isArray())
        .andExpect(jsonPath("$.communityActivity").exists())
        .andExpect(jsonPath("$.challengeParticipations").isArray())
        .andExpect(jsonPath("$.professionalVerifications").isArray())
        .andExpect(jsonPath("$.aiConversations").isArray())
        .andExpect(jsonPath("$.formatVersion").value("EASYNUTRI_EXPORT_V2"))
        .andExpect(jsonPath("$.technicalNotice", containsString("data portability")));
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
  @DisplayName(
      "POST /settings/delete-account with valid password permanently erases user records and transfers household safely")
  void testDeleteAccountValidPassword() throws Exception {
    UserEntity ownerUser =
        userService.registerUser(
            new RegisterRequest(
                "owner_erase_test",
                "owner_erase@example.com",
                "SecretPass123!",
                "Owner Erase User"));
    UserEntity memberUser =
        userService.registerUser(
            new RegisterRequest(
                "member_survive_test",
                "member_survive@example.com",
                "SecretPass123!",
                "Surviving Member"));

    HouseholdDto household =
        householdService.createHousehold(ownerUser.getId(), "Erase Test Household");
    householdService.addMember(
        ownerUser.getId(),
        household.id(),
        new com.easynutri.dto.AddHouseholdMemberRequest(
            "member_survive_test",
            "SPOUSE",
            "MEMBER",
            30,
            "FEMALE",
            165.0,
            60.0,
            "MODERATE",
            "MAINTAIN",
            "VEGETARIAN",
            List.of(),
            List.of(),
            false,
            "#10b981"));

    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();

    mockMvc
        .perform(
            post("/settings/delete-account")
                .with(csrf())
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.user(ownerUser.getUsername()))
                .param("confirmPassword", "SecretPass123!"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login?deleted=true"));

    assertFalse(
        userRepository.findByUsername("owner_erase_test").isPresent(),
        "Owner user must be completely erased from database");

    assertTrue(
        userRepository.findByUsername("member_survive_test").isPresent(),
        "Surviving member must not be deleted");

    var remainingHousehold = householdRepository.findById(household.id());
    assertTrue(remainingHousehold.isPresent(), "Household must survive with surviving member");
    assertNotNull(remainingHousehold.get().getOwner(), "Household must have an owner");
    assertNotEquals(
        ownerUser.getId(),
        remainingHousehold.get().getOwner().getId(),
        "Household ownership must transfer away from erased owner to surviving member");
  }
}
