package kcz.rfid.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        final String securityScheme = "bearerAuth";

        SecurityScheme bearerAuthScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name(securityScheme)
                .description("JWT Bearer token authentication");

        return new OpenAPI()
                .info(new Info().title("RFID-backend")
                        .version("1.0")
                        .description("Backend application Api for device localisation system."))
                .addSecurityItem(new SecurityRequirement().addList(securityScheme))
                .components(new Components().addSecuritySchemes(securityScheme, bearerAuthScheme));
    }
}
