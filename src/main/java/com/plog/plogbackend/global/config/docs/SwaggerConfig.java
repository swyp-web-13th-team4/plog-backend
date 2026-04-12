package com.plog.plogbackend.global.config.docs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI customOpenApi() {
    String securityScheme = "bearerAuth";
    return new OpenAPI()
        .info(openApiInfo())
        .components(
            new Components()
                .addSecuritySchemes(
                    securityScheme,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
        .addSecurityItem(new SecurityRequirement().addList(securityScheme));
  }

  private Info openApiInfo() {
    return new Info().title("Plog Server API").description("Plog Server API").version("1.0");
  }
}
