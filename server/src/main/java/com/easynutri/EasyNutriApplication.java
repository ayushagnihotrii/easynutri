package com.easynutri;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the EasyNutri Full-Stack Web Application.
 *
 * <p>Powers the Spring MVC web layer, Thymeleaf responsive UI, REST APIs, Spring Data JPA
 * persistence, and pure Java domain rules engine.
 */
@SpringBootApplication
public class EasyNutriApplication {

  public static void main(String[] args) {
    SpringApplication.run(EasyNutriApplication.class, args);
  }
}
