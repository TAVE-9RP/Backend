package com.nexerp.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  public static final String AT_SCHEME = "access_token";

  @Bean
  public OpenAPI customOpenAPI() {
    // 보안 방식(Scheme) 정의
    Components components = new Components()
      .addSecuritySchemes(AT_SCHEME, new SecurityScheme()
        .name("Authorization")
        .type(SecurityScheme.Type.HTTP)
        .scheme("bearer")
        .bearerFormat("JWT"));

    return new OpenAPI()
      .info(new Info()
        .title("NexERP API")
        .version("1.0")
        .description("NexERP 프로젝트 Swagger 문서입니다."))
      .components(components);
  }
}
