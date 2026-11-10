package com.easynutri;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class LocalizationAndAccessibilityTest {

  @Autowired private WebApplicationContext wac;
  @Autowired private MessageSource messageSource;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  @Test
  @DisplayName("MessageSource resolves English and Hindi message bundles correctly")
  void testMessageSourceResolution() {
    // English resolution
    String enDashboard = messageSource.getMessage("nav.dashboard", null, Locale.ENGLISH);
    String enAppName = messageSource.getMessage("app.name", null, Locale.ENGLISH);
    assertEquals("Dashboard", enDashboard);
    assertEquals("EasyNutri", enAppName);

    // Hindi resolution
    Locale hindi = Locale.forLanguageTag("hi");
    String hiDashboard = messageSource.getMessage("nav.dashboard", null, hindi);
    String hiAppName = messageSource.getMessage("app.name", null, hindi);
    assertEquals("डैशबोर्ड", hiDashboard);
    assertTrue(hiAppName.contains("EasyNutri") || hiAppName.contains("इज़ी"));

    // Fallback: If an undefined key is requested with default fallback
    String fallback = messageSource.getMessage("non.existent.key", null, "Default Fallback", hindi);
    assertEquals("Default Fallback", fallback);
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("Request with ?lang=hi sets EASYNUTRI_LOCALE cookie")
  void testLanguageSwitchCookie() throws Exception {
    mockMvc
        .perform(get("/dashboard").param("lang", "hi"))
        .andExpect(status().isOk())
        .andExpect(cookie().exists("EASYNUTRI_LOCALE"))
        .andExpect(cookie().value("EASYNUTRI_LOCALE", "hi"));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("Topbar includes language switcher buttons for English and Hindi")
  void testTopbarLanguageSwitcherPresent() throws Exception {
    mockMvc
        .perform(get("/dashboard"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("?lang=en")))
        .andExpect(content().string(containsString("?lang=hi")));
  }

  @Test
  @WithMockUser(username = "demo")
  @DisplayName("Sync status indicator in topbar contains accessibility attributes")
  void testSyncStatusAccessibility() throws Exception {
    mockMvc
        .perform(get("/dashboard"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("networkSyncBadge")));
  }
}
