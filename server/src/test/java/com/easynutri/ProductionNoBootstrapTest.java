package com.easynutri;

import static org.junit.jupiter.api.Assertions.*;

import com.easynutri.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@TestPropertySource(
    properties = {
      "spring.profiles.active=prod",
      "spring.datasource.url=jdbc:h2:mem:prodtest_nobootstrap_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
      "spring.datasource.driverClassName=org.h2.Driver",
      "spring.datasource.username=sa",
      "spring.datasource.password=",
      "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
      "spring.jpa.hibernate.ddl-auto=update"
    })
@Transactional
class ProductionNoBootstrapTest {

  @Autowired private UserRepository userRepository;

  @Test
  @DisplayName("Production Profile Without Bootstrap: Zero users seeded automatically")
  void testZeroDefaultUsersInProductionWithoutBootstrap() {
    assertFalse(
        userRepository.findByUsername("demo").isPresent(),
        "Development demo user must not exist in production");

    assertFalse(
        userRepository.existsByRole("ROLE_ADMIN"),
        "No administrator must exist when bootstrap credentials are not configured");

    assertEquals(
        0,
        userRepository.count(),
        "Production database must start completely clean without default accounts");
  }
}
