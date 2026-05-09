package com.jefersondev.tasks.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Task Manager API")
                        .description("REST API para gerenciamento de listas de tarefas e tarefas. " +
                                "Permite criar, listar, atualizar e deletar task lists e tasks.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Jeferson Vieira")
                                .url("https://github.com/JefersonVieira7")));
    }
}