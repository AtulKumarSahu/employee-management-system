package com.ems.employeemanagement.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration class for Springdoc OpenAPI 3.0 and interactive Swagger UI.
 *
 * <p>Configures:
 * <ul>
 *   <li>API metadata (title, version, description, contact, license)</li>
 *   <li>Global JWT Bearer Authentication scheme ("Authorize" button in Swagger UI)</li>
 *   <li>Server environment URLs</li>
 * </ul>
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // Server configurations
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server")
                ))
                // API Info metadata
                .info(new Info()
                        .title("Enterprise Employee Management System API")
                        .version("1.0.0")
                        .description("""
                                ## Enterprise Employee Management System (EMS) REST API Documentation
                                
                                A robust, secure, and production-grade backend system built with **Java 21**, **Spring Boot 3**, **Spring Security 6**, and **PostgreSQL**.
                                
                                ### Features:
                                * **Authentication & RBAC**: JWT Access & Refresh token rotation with Role-Based Access Control (`ADMIN`, `HR`, `MANAGER`, `EMPLOYEE`).
                                * **Employee Lifecycle**: Full CRUD, Soft Delete with `@SQLDelete`, Pagination, Keyword Search & Filtering.
                                * **Department Hierarchy**: Department management with designated manager assignment and circular FK protection.
                                * **Leave Engine**: Quota validation, overlap prevention, balance deductions, and approval workflows.
                                * **Attendance Management**: Daily check-in/out, late arrival tracking (> 09:15 AM), duration calculation, and monthly summary analytics.
                                
                                ### Authentication Instructions:
                                1. Call `POST /api/v1/auth/login` with your credentials (e.g. `admin@ems.com` / `Admin@123`).
                                2. Copy the returned `accessToken`.
                                3. Click the green **Authorize** button at the top right of this page.
                                4. Paste your token (the `Bearer ` prefix is attached automatically).
                                """)
                        .contact(new Contact()
                                .name("Enterprise Engineering Team")
                                .email("dev-support@ems.com")
                                .url("https://github.com/enterprise/employee-management-system"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                // Attach security requirement globally to all endpoints
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                // Define JWT Bearer security scheme in components
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT Bearer token to authorize requests.")
                        ));
    }
}

