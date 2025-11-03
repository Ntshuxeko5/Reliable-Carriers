package com.reliablecarriers.Reliable.Carriers.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) configuration
 */
@Configuration
public class OpenApiConfig {

    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;

    @Bean
    public OpenAPI reliableCarriersOpenAPI() {
        Server server = new Server();
        server.setUrl(baseUrl);
        server.setDescription("Reliable Carriers API Server");

        Contact contact = new Contact();
        contact.setEmail("support@reliablecarriers.co.za");
        contact.setName("Reliable Carriers Support");
        contact.setUrl("https://reliablecarriers.co.za");

        License license = new License()
            .name("Proprietary")
            .url("https://reliablecarriers.co.za/license");

        Info info = new Info()
            .title("Reliable Carriers API")
            .version("1.0.0")
            .contact(contact)
            .description("Comprehensive API for Reliable Carriers courier and logistics services. " +
                "This API provides endpoints for package tracking, booking management, driver operations, " +
                "business integrations, and administrative functions.")
            .license(license)
            .termsOfService("https://reliablecarriers.co.za/terms");

        return new OpenAPI()
            .info(info)
            .servers(List.of(server));
    }
}
