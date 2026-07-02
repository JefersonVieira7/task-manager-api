package com.jefersondev.tasks.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .addServersItem(new Server()
                        .url("https://task-manager-api-production-3374.up.railway.app")
                        .description("Servidor de Produção"))
                .addServersItem(new Server()
                        .url("http://localhost:8080")
                        .description("Servidor Local"))
                .info(new Info()
                        .title("Task Manager API")
                        .description("""
                                REST API para gerenciamento de listas de tarefas e tarefas.
                                
                                **Como usar:**
                                1. Use `POST /auth/register` para criar uma conta
                                2. Use `POST /auth/login` para obter o token JWT
                                3. Clique em **Authorize** e cole o token no campo
                                4. Agora você pode usar todos os endpoints protegidos
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Jeferson Vieira")
                                .url("https://github.com/JefersonVieira7")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Cole o token JWT obtido no login. Exemplo: eyJhbGci...")));
    }
}