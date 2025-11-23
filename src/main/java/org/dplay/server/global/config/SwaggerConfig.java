package org.dplay.server.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String jwt = "JWT";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwt);
        Components components = new Components().addSecuritySchemes(jwt, new SecurityScheme()
                .name(jwt)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER));

        return new OpenAPI()
                .info(new Info()
                        .title("DPlay Server API")
                        .description("DPlay Server API Documentation")
                        .version("v1.0.0"))
                .servers(List.of(
                        new Server().url("http://localhost:8080/api").description("Local Server"),
                        new Server().url("http://3.38.79.157/api").description("Production Server")
                ))
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}
