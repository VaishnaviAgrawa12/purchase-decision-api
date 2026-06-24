package com.vaishnavi.purchase_decision_api.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // disable CSRF — not needed for stateless JWT APIs
                // CSRF protects browser sessions, we don't have sessions
                .csrf(AbstractHttpConfigurer::disable)

                // make the app stateless — no sessions, ever
                // Spring will never create an HttpSession
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // define which endpoints are public vs protected
                .authorizeHttpRequests(auth -> auth
                        // anyone can register and login — no token needed
                        .requestMatchers("/api/auth/**").permitAll()
                        // everything else — must have a valid JWT
                        .anyRequest().authenticated()
                )

                // plug JwtFilter into the chain
                // runs BEFORE Spring's built-in UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
