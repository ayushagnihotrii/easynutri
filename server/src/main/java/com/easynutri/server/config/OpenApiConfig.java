package com.easynutri.server.config;

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
                .title("EasyNutri Backend API")
                .description(
                    "Backend service for EasyNutri nutrition, meal-planning, and health-tracking native Android application.")
                .version("1.0.0")
                .contact(new Contact().name("EasyNutri Engineering").email("support@easynutri.org"))
                .license(
                    new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0")));
  }
}
