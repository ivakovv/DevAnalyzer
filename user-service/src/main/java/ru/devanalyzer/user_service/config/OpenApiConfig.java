package ru.devanalyzer.user_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(@Value("${API_SERVER_URL}") String serverUrl) {
        return new OpenAPI()
                .addServersItem(new Server().url(serverUrl));
    }
}
