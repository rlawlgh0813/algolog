package com.algolog.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI algologOpenApi() {
        SecurityScheme bearerAuthScheme = new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT");

        return new OpenAPI()
            .info(new Info()
                .title("AlgoLog API")
                .description("Algorithm learning log backend API")
                .version("v1"))
            .components(new Components().addSecuritySchemes(BEARER_AUTH, bearerAuthScheme))
            .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
            .group("auth")
            .pathsToMatch("/api/auth/**")
            .build();
    }

    @Bean
    public GroupedOpenApi problemApi() {
        return GroupedOpenApi.builder()
            .group("problem")
            .pathsToMatch("/api/problems/**")
            .build();
    }

    @Bean
    public GroupedOpenApi solutionRecordApi() {
        return GroupedOpenApi.builder()
            .group("solution-record")
            .pathsToMatch("/api/solution-records/**", "/api/me/solution-records/**")
            .build();
    }

    @Bean
    public GroupedOpenApi counterExampleApi() {
        return GroupedOpenApi.builder()
            .group("counter-example")
            .pathsToMatch("/api/solution-records/**/counter-examples/**")
            .build();
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
            .group("public")
            .pathsToMatch("/api/public/**", "/api/problems/*/public-solution-records")
            .build();
    }
}
