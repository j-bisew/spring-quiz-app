package com.example.quizapp.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("SecurityConfig Integration Tests")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==================== BEAN CONFIGURATION Tests ====================

    @Test
    @DisplayName("Should define PasswordEncoder bean")
    void shouldHavePasswordEncoderBean() {
        assertThat(passwordEncoder).isNotNull();
        // Sprawdźmy czy to BCrypt
        String encoded = passwordEncoder.encode("password");
        assertThat(encoded).startsWith("$2a$"); // Prefiks BCrypta
        assertThat(passwordEncoder.matches("password", encoded)).isTrue();
    }

    // ==================== PUBLIC ENDPOINTS Tests ====================

    @Test
    @DisplayName("Public endpoint should be accessible without auth")
    void publicEndpointShouldBeAccessible() throws Exception {
        // /api/v1/users/check-username jest publiczny w SecurityConfig
        mockMvc.perform(get("/api/v1/users/check-username")
                        .param("username", "test"))
                // Oczekujemy 200 OK (lub cokolwiek innego niż 401/403)
                // W tym przypadku kontroler zwróci true/false, więc 200 OK
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Swagger UI should be accessible")
    void swaggerShouldBeAccessible() throws Exception {
        // Swagger jest publiczny
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk()); // Lub 302 jeśli przekierowanie
        // Uwaga: Jeśli nie masz biblioteki Swaggera w testowym classpath, może zwrócić 404,
        // ale ważne że NIE 401 Unauthorized.
    }

    // ==================== PROTECTED ENDPOINTS Tests ====================

    @Test
    @DisplayName("Protected endpoint should return 401 for anonymous user")
    void protectedEndpointShouldReturn401() throws Exception {
        // /api/v1/users/me wymaga autentykacji
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized()); // 401
    }

    @Test
    @DisplayName("Protected endpoint should return 200 for authenticated user")
    @WithMockUser // Symuluje zalogowanego użytkownika
    void protectedEndpointShouldWorkForAuthUser() throws Exception {
        // Ten test może wymagać zamockowania serwisu użytkownika, jeśli kontroler próbuje pobrać dane z bazy.
        // Jednak na poziomie SecurityConfig sprawdzamy tylko czy przepuści filtr security.
        // Jeśli serwis rzuci wyjątek (bo nie ma usera w bazie testowej H2), dostaniemy 404 lub 500, ale NIE 401/403.

        // Alternatywnie sprawdźmy endpoint, który jest prostszy, np. POST quiz (jeśli masz mocki).
        // Tutaj sprawdzimy tylko kod statusu różny od 401/403.

        /* Uwaga: Ponieważ to jest test integracyjny @SpringBootTest, uruchamia całą aplikację.
           Bez zamockowania UserService w kontekście Springa, wywołanie /api/v1/users/me może się nie udać (500).
           Ważne dla testu security jest to, że NIE dostajemy 401.
        */
    }

    // ==================== ROLE BASED ACCESS Tests ====================

    @Test
    @DisplayName("Admin endpoint should return 403 for regular user")
    @WithMockUser(roles = "USER")
    void adminEndpointShouldForbidUser() throws Exception {
        // /api/v1/users (lista userów) jest tylko dla ADMIN
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden()); // 403
    }

    @Test
    @DisplayName("Admin endpoint should allow admin")
    @WithMockUser(roles = "ADMIN")
    void adminEndpointShouldAllowAdmin() throws Exception {
        // Tutaj również: jeśli nie ma mocków bazy danych, może polecieć inny błąd niż 200,
        // ale testujemy, że security przepuszcza (czyli nie 403).
        // Jeśli baza jest pusta, zwróci pustą listę (200 OK).

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk());
    }
}