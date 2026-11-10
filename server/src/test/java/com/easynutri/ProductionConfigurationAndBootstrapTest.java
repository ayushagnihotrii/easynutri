package com.easynutri;

import static org.junit.jupiter.api.Assertions.*;

import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@TestPropertySource(
    properties = {
      "spring.profiles.active=prod",
      "spring.datasource.url=jdbc:h2:mem:prodtestdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
      "spring.datasource.driverClassName=org.h2.Driver",
      "spring.datasource.username=sa",
      "spring.datasource.password=",
      "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
      "spring.jpa.hibernate.ddl-auto=update",
      "EASYNUTRI_ADMIN_BOOTSTRAP_USERNAME=bootstrap_admin",
      "EASYNUTRI_ADMIN_BOOTSTRAP_EMAIL=bootstrap_admin@easynutri.com",
      "EASYNUTRI_ADMIN_BOOTSTRAP_PASSWORD=ProductionSuperSecret2026!"
    })
@Transactional
class ProductionConfigurationAndBootstrapTest {

  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  @Test
  @DisplayName("Production Profile: Demo account is NEVER created in production")
  void testNoDemoUserInProduction() {
    assertFalse(
        userRepository.findByUsername("demo").isPresent(),
        "Development 'demo' user must NEVER be seeded in production profile");
  }

  @Test
  @DisplayName(
      "Production Profile: Administrator is bootstrapped via environment credentials with BCrypt hashing")
  void testAdminBootstrapViaEnvironment() {
    var adminOpt = userRepository.findByUsername("bootstrap_admin");
    assertTrue(
        adminOpt.isPresent(),
        "Administrator must be provisioned via bootstrap environment variables");

    UserEntity admin = adminOpt.get();
    assertEquals("ROLE_ADMIN", admin.getRole(), "Bootstrap administrator must have ROLE_ADMIN");
    assertEquals("bootstrap_admin@easynutri.com", admin.getEmail());

    assertTrue(
        passwordEncoder.matches("ProductionSuperSecret2026!", admin.getPasswordHash()),
        "Password must be securely hashed with BCrypt");

    assertNotEquals(
        "ProductionSuperSecret2026!",
        admin.getPasswordHash(),
        "Plaintext password must never be stored in database");
  }
}
