package com.example.quizapp.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Quiz App API", version = "v1"),
        // To ustawienie mówi Swaggerowi, że domyślnie wszystkie endpointy wymagają autoryzacji "basicAuth"
        security = @SecurityRequirement(name = "basicAuth")
)
@SecurityScheme(
        name = "basicAuth",     // Nazwa schematu, do której odwołujemy się wyżej
        type = SecuritySchemeType.HTTP,
        scheme = "basic"        // Typ basic pasuje do Twojego .httpBasic() w SecurityConfig
)
public class OpenApiConfig {
    // Klasa może być pusta, wystarczą adnotacje
}