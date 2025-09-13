package com.example.attendancesystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration class.
 * This class defines the security filter chain, password encoder, and authentication manager.
 * It has been updated to use the modern lambda-based syntax introduced in Spring Security 6.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtUtil jwtUtil;

    /**
     * Constructs the SecurityConfig with required dependencies.
     * Spring automatically injects the JwtAuthenticationFilter bean.
     *
     * @param jwtAuthenticationFilter The custom JWT filter to be added to the security chain.
     * @param jwtUtil The utility for handling JWT tokens.
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, JwtUtil jwtUtil) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Configures the security filter chain.
     *
     * @param http The HttpSecurity object to configure.
     * @return The configured SecurityFilterChain.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Disables CSRF protection as we are using a stateless REST API with JWT.
                .csrf(csrf -> csrf.disable())

                // Configures session management to be stateless, ensuring no session is created or used.
                // This is a key part of using JWT for authentication.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Defines authorization rules for HTTP requests.
                // This replaces the deprecated authorizeHttpRequests().and() syntax.
                .authorizeHttpRequests(authorize -> authorize
                        // Allows all requests to public endpoints (e.g., login, registration) without authentication.
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        // Requires authentication for requests to admin endpoints.
                        .requestMatchers("/api/admin/**").hasAnyRole("PRINCIPAL", "TEACHER")
                        // Requires authentication for any other request not covered by the above rules.
                        .anyRequest().authenticated()
                )

                // Adds our custom JWT filter before Spring's default UsernamePasswordAuthenticationFilter.
                // This ensures our token-based authentication logic runs first.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Provides a BCryptPasswordEncoder bean for encoding passwords.
     *
     * @return A PasswordEncoder bean.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Exposes the AuthenticationManager bean.
     *
     * @param authConfig The AuthenticationConfiguration object to get the manager from.
     * @return The AuthenticationManager bean.
     * @throws Exception if an error occurs.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
