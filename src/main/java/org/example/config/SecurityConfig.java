package org.example.config;

import org.example.security.JwtAuthenticationEntryPoint;
import org.example.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // Swagger
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/swagger-resources/configuration/ui",
                                "/swagger-resources/configuration/security",
                                "/webjars/**"
                        ).permitAll()


                        // Pub
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                        .requestMatchers("/api/vehicles/available").permitAll()
                        .requestMatchers("/api/payments/success").permitAll()
                        .requestMatchers("/api/payments/cancel").permitAll()
                        .requestMatchers("/api/payments/webhook").permitAll()


                        .requestMatchers("/api/auth/profile").authenticated()

                        // user wlasne dane
                        .requestMatchers("/api/rentals/history/my").hasAuthority("user")
                        .requestMatchers("/api/rentals/active/my").hasAuthority("user")
                        .requestMatchers("/api/rentals/rent/**").hasAuthority("user")
                        .requestMatchers("/api/rentals/return/**").hasAuthority("user")

                        // platnosc
                        .requestMatchers("/api/payments/create-payment-intent").authenticated()
                        .requestMatchers("/api/payments/create-checkout-session").authenticated()
                        .requestMatchers("/api/payments/check-payment-status").authenticated()
                        .requestMatchers("/api/payments/update-status/**").authenticated()

                        //lokalizacja dla usera
                        .requestMatchers("/api/locations/set-random-location/**").hasAnyAuthority("admin", "user")
                        .requestMatchers("/api/locations/status/**").hasAnyAuthority("admin", "user")
                        .requestMatchers("/api/locations/set-location/**").hasAnyAuthority("admin", "user")

                        // admin
                        .requestMatchers("/api/vehicles/create", "/api/vehicles/delete/**").hasAuthority("admin")
                        .requestMatchers("/api/vehicles", "/api/vehicles/**", "/api/vehicles/active", "/api/vehicles/deleted").hasAuthority("admin")
                        .requestMatchers("/api/rentals", "/api/rentals/history", "/api/rentals/history/vehicle/**").hasAuthority("admin")
                        .requestMatchers("/api/rentals/history/user/**").hasAnyAuthority("admin", "user")
                        .requestMatchers("/api/users/**").hasAuthority("admin")
                        .requestMatchers("/api/roles/**").hasAuthority("admin")
                        .requestMatchers("/api/locations/allowed/**").hasAuthority("admin")
                        .requestMatchers("/api/locations/not-allowed").hasAuthority("admin")
                        .requestMatchers("/api/locations/update-all-status").hasAuthority("admin")
                        .requestMatchers("/api/locations/set-location-by-coords/**").hasAuthority("admin")

                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}