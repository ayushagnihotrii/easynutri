package com.easynutri;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.easynutri.dto.CreateCommunityRecipeRequest;
import com.easynutri.dto.RecipeIngredientInputDto;
import com.easynutri.entity.ContentReportEntity;
import com.easynutri.entity.ProfessionalVerificationEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.CommunityService;
import com.easynutri.service.ModerationService;
import com.easynutri.service.ProfessionalVerificationService;
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
class ModerationWebControllerTest {

  @Autowired private WebApplicationContext wac;
  @Autowired private ModerationService moderationService;
  @Autowired private ProfessionalVerificationService verificationService;
  @Autowired private CommunityService communityService;
  @Autowired private UserRepository userRepository;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  @Test
  @WithMockUser(
      username = "demo",
      roles = {"ADMIN"})
  @DisplayName("GET /admin/moderation renders reports, pro applications and audit log")
  void testModerationDashboard() throws Exception {
    mockMvc
        .perform(get("/admin/moderation"))
        .andExpect(status().isOk())
        .andExpect(view().name("admin/moderation"))
        .andExpect(model().attributeExists("reports"))
        .andExpect(model().attributeExists("verifications"))
        .andExpect(model().attributeExists("auditLog"));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /community/report submits content report with automated clinical AI verdict")
  void testSubmitReport() throws Exception {
    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();
    var published =
        communityService.publishRecipe(
            demo.getId(),
            new CreateCommunityRecipeRequest(
                "Energy Smoothie",
                "Healthy morning smoothie",
                null,
                1,
                5,
                0,
                "BREAKFAST",
                "Blend and enjoy.",
                List.of(new RecipeIngredientInputDto(null, "Banana", 100.0, "g"))));

    mockMvc
        .perform(
            post("/community/report")
                .with(csrf())
                .param("targetType", "RECIPE")
                .param("targetId", String.valueOf(published.id()))
                .param("reasonCategory", "DANGEROUS_MEDICAL_CLAIM")
                .param("description", "Author claims it cures diabetes in comments")
                .param("returnUrl", "/community/recipes/" + published.id()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/community/recipes/" + published.id()))
        .andExpect(flash().attributeExists("flashMessage"));

    List<ContentReportEntity> pending = moderationService.getPendingReports();
    assertFalse(pending.isEmpty());
    ContentReportEntity report = pending.get(0);
    assertEquals("RECIPE", report.getTargetType());
    assertEquals(published.id(), report.getTargetId());
  }

  @Test
  @WithMockUser(
      username = "demo",
      roles = {"ADMIN"})
  @DisplayName("POST /admin/moderation/action executes moderation decision and records audit log")
  void testModerationAction() throws Exception {
    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();
    var published =
        communityService.publishRecipe(
            demo.getId(),
            new CreateCommunityRecipeRequest(
                "Questionable Soup",
                "Detox soup",
                null,
                1,
                5,
                5,
                "LUNCH",
                "Boil and drink.",
                List.of(new RecipeIngredientInputDto(null, "Water", 200.0, "g"))));

    mockMvc
        .perform(
            post("/admin/moderation/action")
                .with(csrf())
                .param("targetType", "RECIPE")
                .param("targetId", String.valueOf(published.id()))
                .param("actionTaken", "HIDE")
                .param("moderatorNotes", "Hidden pending clinical verification"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/moderation"));

    var recipeOpt = communityService.getRecipeById(published.id(), demo.getId());
    assertTrue(recipeOpt.isPresent());
    assertEquals("HIDDEN", recipeOpt.get().status());

    var auditLog = moderationService.getAuditHistory();
    assertFalse(auditLog.isEmpty());
    assertEquals("HIDE", auditLog.get(0).getActionTaken());
  }

  @Test
  @WithMockUser(
      username = "demo",
      roles = {"ADMIN"})
  @DisplayName("Professional verification review grants verified health pro badge")
  void testProfessionalVerificationWorkflow() throws Exception {
    UserEntity demo = userRepository.findByUsername("demo").orElseThrow();
    assertFalse(demo.isVerifiedPro());

    // User submits application
    mockMvc
        .perform(
            post("/community/verify/apply")
                .with(csrf())
                .param("professionalCategory", "REGISTERED_DIETITIAN")
                .param("licenseNumber", "RDN-884920")
                .param("credentialsSummary", "MS Clinical Nutrition, State Board Certified RDN")
                .param("documentFilename", "dietitian_license.pdf"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/community/users/demo"));

    List<ProfessionalVerificationEntity> pending = verificationService.getPendingApplications();
    assertFalse(pending.isEmpty());
    Long appId = pending.get(0).getId();

    // Admin approves application
    mockMvc
        .perform(
            post("/admin/moderation/verify")
                .with(csrf())
                .param("applicationId", String.valueOf(appId))
                .param("approved", "true")
                .param("notes", "State licensing database verified"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/moderation"));

    UserEntity updatedDemo = userRepository.findByUsername("demo").orElseThrow();
    assertTrue(updatedDemo.isVerifiedPro());
    assertEquals("REGISTERED_DIETITIAN", updatedDemo.getProCategory());
  }
}
