package com.server.shared.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - Authentication
                        .requestMatchers("/api/auth/**").permitAll()

                        // Public endpoints - Browse services and organizations
                        .requestMatchers(HttpMethod.GET, "/api/organizations").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/organizations/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/services").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/services/{id}").permitAll()

                        // Public endpoints - User registration
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()

                        // Public endpoints - Swagger/OpenAPI
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                        // GLOBAL_ADMIN only endpoints
                        .requestMatchers(HttpMethod.GET, "/api/users").hasRole("GLOBAL_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/organizations").hasRole("GLOBAL_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/organizations/{id}").hasRole("GLOBAL_ADMIN")

                        // All bookings list - GLOBAL_ADMIN or ORG_ADMIN (checked via @PreAuthorize)
                        .requestMatchers(HttpMethod.GET, "/api/bookings/").hasAnyRole("GLOBAL_ADMIN", "USER")

                        // Authenticated user endpoints - own profile and bookings (checked via @PreAuthorize)
                        .requestMatchers(HttpMethod.GET, "/api/users/{id}").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/users/{id}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/users/{id}").authenticated()

                        // Booking endpoints
                        .requestMatchers(HttpMethod.POST, "/api/bookings").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/bookings/{id}").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/bookings/client/{id}").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/bookings/specialist/{id}").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/bookings/{id}/confirm").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/bookings/{id}/cancel").authenticated()

                        // Schedule endpoints
                        .requestMatchers("/api/schedule/**").authenticated()

                        // Organization management - authenticated (checked via @PreAuthorize)
                        .requestMatchers(HttpMethod.PATCH, "/api/organizations/{id}").authenticated()
                        .requestMatchers("/api/services/**").authenticated()
                        .requestMatchers("/api/members/**").authenticated()

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
