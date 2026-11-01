package com.easynutri.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI easyNutriOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("EasyNutri Web API")
                .description(
                    "REST API documentation for EasyNutri — Nutrition tracking, smart meal planning, and clinical rules platform.")
                .version("1.0.0-phase0-web")
                .contact(
                    new Contact().name("EasyNutri Engineering Team").email("support@easynutri.com"))
                .license(new License().name("Apache 2.0")));
  }
}
