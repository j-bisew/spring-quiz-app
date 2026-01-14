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
                        // Public endpoints (no authentication required)
                        .requestMatchers(
                                "/",
                                "/api/v1/game/**",          // Game endpoints (player mode)
                                "/api/v1/rankings/**",      // Rankings are public
                                "/swagger-ui/**",           // Swagger UI
                                "/v3/api-docs/**",          // OpenAPI docs
                                "/error",
                                "/h2-console/**"            // H2 console (dev only)
                        ).permitAll()

                        // Admin only endpoints
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // Quiz creator endpoints (authenticated users)
                        .requestMatchers(
                                "/api/v1/quizzes/**",
                                "/api/v1/questions/**"
                        ).hasAnyRole("USER", "ADMIN")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                // Form login configuration
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/perform_login")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                // Logout configuration
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                // HTTP Basic for API (optional)
                .httpBasic(basic -> {})

                // Exception handling
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/access-denied")
                )

                // H2 Console configuration (dev only)
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin()) // Allow H2 console frames
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