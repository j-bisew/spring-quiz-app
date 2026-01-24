package com.example.quizapp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for Quiz Application
 *
 * Authorization levels:
 * - PUBLIC: No authentication required (game play, rankings, Swagger)
 * - USER: Requires USER or ADMIN role (quiz/question creation)
 * - ADMIN: Requires ADMIN role only (user management, analytics, system operations)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    @SuppressWarnings({"java:S112", "java:S1130"})
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF configuration
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**") // Disable CSRF for API endpoints
                )

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // ============================================
                        // PUBLIC ENDPOINTS (No authentication required)
                        // ============================================

                        // Home and static resources
                        .requestMatchers(
                                "/",
                                "/home",
                                "/error",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico"
                        ).permitAll()

                        // Swagger/OpenAPI documentation
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/webjars/**",
                                "/swagger-resources/**",
                                "/configuration/**"
                        ).permitAll()

                        // User registration (public)
                        .requestMatchers(
                                "/register",
                                "/api/v1/users/register",
                                "/api/v1/users/check-username",
                                "/api/v1/users/check-email"
                        ).permitAll()

                        // Quiz browsing (public - read only)
                        .requestMatchers(
                                "/quizzes",                     // MVC: List all quizzes
                                "/quizzes/{id}",               // MVC: View quiz details
                                "/api/v1/quizzes"              // API: Get all quizzes (GET only)
                        ).permitAll()

                        // Game play (public - no login required for players)
                        .requestMatchers(
                                "/game/**",                     // MVC: All game views
                                "/api/v1/game/**"              // API: Start, submit, results
                        ).permitAll()

                        // Rankings (public - anyone can view)
                        .requestMatchers(
                                "/rankings/**",                 // MVC: Rankings views
                                "/api/v1/rankings/**"          // API: All ranking endpoints
                        ).permitAll()

                        // H2 Console (dev only - should be disabled in production)
                        .requestMatchers("/h2-console/**").permitAll()

                        // ============================================
                        // AUTHENTICATED USER ENDPOINTS (USER + ADMIN)
                        // ============================================

                        // Quiz creation and management (authenticated users)
                        .requestMatchers(
                                "/creator",                     // MVC: Creator dashboard
                                "/creator/**"                   // MVC: Creator pages
                        ).hasAnyRole("USER", "ADMIN")

                        // Quiz API - Create, Update, Delete
                        .requestMatchers(
                                "/api/v1/quizzes/{id}",        // GET by ID
                                "/api/v1/quizzes/{id}/with-questions"
                        ).permitAll()  // Reading specific quiz is public

                        .requestMatchers(
                                "/api/v1/quizzes/paginated",
                                "/api/v1/quizzes/search",
                                "/api/v1/quizzes/search/paginated",
                                "/api/v1/quizzes/creator/**",
                                "/api/v1/quizzes/count"
                        ).permitAll()  // Search and stats are public

                        .requestMatchers(
                                org.springframework.http.HttpMethod.POST,
                                "/api/v1/quizzes"
                        ).hasAnyRole("USER", "ADMIN")  // Create quiz

                        .requestMatchers(
                                org.springframework.http.HttpMethod.PUT,
                                "/api/v1/quizzes/**"
                        ).hasAnyRole("USER", "ADMIN")  // Update quiz

                        .requestMatchers(
                                org.springframework.http.HttpMethod.DELETE,
                                "/api/v1/quizzes/**"
                        ).hasAnyRole("USER", "ADMIN")  // Delete quiz

                        // Question management (authenticated users)
                        .requestMatchers(
                                "/api/v1/questions/quiz/{quizId}",
                                "/api/v1/questions/quiz/{quizId}/ordered",
                                "/api/v1/questions/quiz/{quizId}/count",
                                "/api/v1/questions/quiz/{quizId}/total-points",
                                "/api/v1/questions/{id}"
                        ).permitAll()  // Reading questions is public for game play

                        .requestMatchers(
                                org.springframework.http.HttpMethod.POST,
                                "/api/v1/questions"
                        ).hasAnyRole("USER", "ADMIN")  // Create question

                        .requestMatchers(
                                org.springframework.http.HttpMethod.POST,
                                "/api/v1/questions/{id}/validate"
                        ).permitAll()  // Validation is public for game play

                        .requestMatchers(
                                org.springframework.http.HttpMethod.PUT,
                                "/api/v1/questions/**"
                        ).hasAnyRole("USER", "ADMIN")  // Update question

                        .requestMatchers(
                                org.springframework.http.HttpMethod.DELETE,
                                "/api/v1/questions/**"
                        ).hasAnyRole("USER", "ADMIN")  // Delete question

                        // User profile (authenticated users)
                        .requestMatchers(
                                "/api/v1/users/me"
                        ).authenticated()  // Any authenticated user

                        // Analytics (some endpoints for authenticated users)
                        .requestMatchers(
                                "/api/v1/analytics/quiz/**",
                                "/api/v1/analytics/player/**",
                                "/api/v1/analytics/result/**"
                        ).hasAnyRole("USER", "ADMIN")

                        // ============================================
                        // ADMIN ONLY ENDPOINTS
                        // ============================================

                        // User management (admin only)
                        .requestMatchers(
                                "/api/v1/users",
                                "/api/v1/users/{id}"
                        ).hasRole("ADMIN")

                        // Platform analytics (admin only)
                        .requestMatchers(
                                "/api/v1/analytics/platform",
                                "/api/v1/analytics/cleanup/**",
                                "/api/v1/analytics/quizzes/bulk-status"
                        ).hasRole("ADMIN")

                        // Admin endpoints (if any)
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // ============================================
                        // DEFAULT: All other requests require authentication
                        // ============================================
                        .anyRequest().authenticated()
                )

                // Form login configuration - use default Spring login page
                .formLogin(form -> form
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )

                // Logout configuration
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                // HTTP Basic for API (enables API access with credentials)
                .httpBasic(basic -> {})

                // Exception handling
                .exceptionHandling(ex -> {})

                // H2 Console configuration (dev only)
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) {
        try {
            return authConfig.getAuthenticationManager();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to configure authentication manager", e);
        }
    }
}