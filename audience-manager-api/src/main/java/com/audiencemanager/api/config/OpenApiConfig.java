package com.audiencemanager.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration for OpenAPI documentation.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI audienceManagerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Audience Manager API")
                        .description("REST API for managing audience segments and real-time user segmentation")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Audience Manager Team")
                                .email("team@audiencemanager.com")
                                .url("https://audiencemanager.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development server"),
                        new Server()
                                .url("https://api.audiencemanager.com")
                                .description("Production server")
                ));
    }
}