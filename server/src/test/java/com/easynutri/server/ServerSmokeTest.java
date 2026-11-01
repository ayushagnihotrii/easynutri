package com.easynutri.server;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class ServerSmokeTest {

  @Autowired private ApplicationContext applicationContext;

  @Test
  @DisplayName("Verify Spring application context loads on dev profile")
  void contextLoads() {
    assertNotNull(applicationContext, "Application context must load cleanly on dev profile");
  }
}
