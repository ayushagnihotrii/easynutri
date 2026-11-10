package com.easynutri;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.easynutri.dto.ProgressReportDto;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.ProgressAnalyticsService;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
class ProgressWebControllerTest {

  @Autowired private WebApplicationContext wac;
  @Autowired private ProgressAnalyticsService progressService;
  @Autowired private UserRepository userRepository;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("GET /reports renders multi-day progress reports, charts, and insights")
  void testProgressReportsPageLoads() throws Exception {
    mockMvc
        .perform(get("/reports").param("days", "7"))
        .andExpect(status().isOk())
        .andExpect(view().name("reports/index"))
        .andExpect(model().attributeExists("report"))
        .andExpect(model().attribute("selectedDays", 7))
        .andExpect(model().attribute("activeNav", "reports"));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("POST /reports/weight logs user weight measurement and updates current biometrics")
  void testLogWeight() throws Exception {
    LocalDate today = LocalDate.now();

    mockMvc
        .perform(
            post("/reports/weight")
                .with(csrf())
                .param("date", today.toString())
                .param("weightKg", "67.5")
                .param("notes", "Morning post-workout weigh-in")
                .param("days", "7"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/reports?days=7"));

    UserEntity user = userRepository.findByUsername("demo").orElseThrow();
    ProgressReportDto report = progressService.getProgressReport(user.getId(), 7);
    assertEquals(67.5, report.currentWeightKg());
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("GET /reports/export-pdf generates and downloads downloadable PDF report")
  void testExportPdf() throws Exception {
    MvcResult result =
        mockMvc
            .perform(get("/reports/export-pdf").param("days", "7"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_PDF))
            .andExpect(
                header()
                    .string(
                        "Content-Disposition",
                        org.hamcrest.Matchers.containsString("EasyNutri-Report")))
            .andReturn();

    byte[] bytes = result.getResponse().getContentAsByteArray();
    assertNotNull(bytes);
    assertTrue(bytes.length > 500, "Generated PDF should contain data");
    // Standard PDF header signature check (%PDF-)
    String header = new String(bytes, 0, 5);
    assertEquals("%PDF-", header);
  }
}
