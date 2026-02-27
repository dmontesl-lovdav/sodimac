package com.sodimac.fiscal.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class SpringDocConfigurer {

    @Bean
    public OpenAPI configure() {

        // ----- esquema de seguridad Bearer JWT -----
        final String BEARER_SCHEME_NAME = "bearerAuth";

        SecurityScheme bearerScheme = new SecurityScheme()
                .name(BEARER_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        SecurityRequirement bearerRequirement = new SecurityRequirement()
                .addList(BEARER_SCHEME_NAME);

        return new OpenAPI()
        .specVersion(SpecVersion.V30)
                .info(new Info()
                        .title("Financiero API")
                        .description(
                                "Support RESTful operations for financiero flow. The main object is Financial Transaction.")
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME_NAME, bearerScheme))
                .addSecurityItem(bearerRequirement);
    }
}

