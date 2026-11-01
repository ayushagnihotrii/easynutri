package com.easynutri.config;

import com.easynutri.dto.RegisterRequest;
import com.easynutri.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

  private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

  @Bean
  public CommandLineRunner seedDemoUser(UserService userService) {
    return args -> {
      if (!userService.existsByUsername("demo")) {
        userService.registerUser(
            new RegisterRequest(
                "demo", "demo@easynutri.com", "Password123!", "EasyNutri Explorer"));
        log.info("Initialized default demo user: username='demo', password='Password123!'");
      }
    };
  }
}
