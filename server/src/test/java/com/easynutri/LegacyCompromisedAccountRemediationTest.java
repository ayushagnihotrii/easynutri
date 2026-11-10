package com.easynutri;

import static org.junit.jupiter.api.Assertions.*;

import com.easynutri.config.DataInitializer;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.FoodRepository;
import com.easynutri.repository.PantryItemRepository;
import com.easynutri.repository.RecipeRepository;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.UserProfileService;
import com.easynutri.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class LegacyCompromisedAccountRemediationTest {

  @Autowired private UserRepository userRepository;
  @Autowired private UserService userService;
  @Autowired private UserProfileService profileService;
  @Autowired private FoodRepository foodRepository;
  @Autowired private RecipeRepository recipeRepository;
  @Autowired private PantryItemRepository pantryRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private DataInitializer dataInitializer;

  @Test
  @DisplayName(
      "Legacy Audit: Existing admin with compromised password is automatically disabled if no new password is provided")
  void testCompromisedAdminDisabledWhenNoNewPassword() throws Exception {
    UserEntity admin =
        userRepository
            .findByUsername("admin")
            .orElseGet(
                () -> {
                  UserEntity u =
                      new UserEntity(
                          "admin",
                          "admin@easynutri.com",
                          passwordEncoder.encode("AdminSecure2026!"),
                          "Admin");
                  u.setRole("ROLE_ADMIN");
                  return userRepository.save(u);
                });
    admin.setPasswordHash(passwordEncoder.encode("AdminSecure2026!"));
    admin.setEnabled(true);
    userRepository.save(admin);

    MockEnvironment env = new MockEnvironment();
    env.setActiveProfiles("prod");

    dataInitializer.initializeData(
        userService,
        profileService,
        userRepository,
        foodRepository,
        recipeRepository,
        pantryRepository,
        passwordEncoder,
        env);

    UserEntity refreshed = userRepository.findByUsername("admin").orElseThrow();
    assertFalse(refreshed.isEnabled(), "Compromised admin account must be automatically disabled");
  }

  @Test
  @DisplayName(
      "Legacy Audit: Existing admin with compromised password is automatically rotated when new bootstrap password is provided")
  void testCompromisedAdminRotatedWhenNewPasswordProvided() throws Exception {
    UserEntity admin =
        userRepository
            .findByUsername("admin")
            .orElseGet(
                () -> {
                  UserEntity u =
                      new UserEntity(
                          "admin",
                          "admin@easynutri.com",
                          passwordEncoder.encode("AdminSecure2026!"),
                          "Admin");
                  u.setRole("ROLE_ADMIN");
                  return userRepository.save(u);
                });
    admin.setPasswordHash(passwordEncoder.encode("AdminSecure2026!"));
    admin.setEnabled(false);
    userRepository.save(admin);

    MockEnvironment env = new MockEnvironment();
    env.setActiveProfiles("prod");
    env.setProperty("EASYNUTRI_ADMIN_BOOTSTRAP_PASSWORD", "BrandNewSecurePass2026!#");

    dataInitializer.initializeData(
        userService,
        profileService,
        userRepository,
        foodRepository,
        recipeRepository,
        pantryRepository,
        passwordEncoder,
        env);

    UserEntity refreshed = userRepository.findByUsername("admin").orElseThrow();
    assertTrue(refreshed.isEnabled(), "Admin account must be re-enabled after rotation");
    assertTrue(
        passwordEncoder.matches("BrandNewSecurePass2026!#", refreshed.getPasswordHash()),
        "Password must be updated to new password");
    assertFalse(
        passwordEncoder.matches("AdminSecure2026!", refreshed.getPasswordHash()),
        "Compromised password must no longer match");
  }
}
