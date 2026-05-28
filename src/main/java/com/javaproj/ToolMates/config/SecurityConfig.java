package com.javaproj.ToolMates.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/send-otp",
                                "/api/auth/verify-otp",
                                "/api/auth/signup",
                                "/api/auth/login",
                                "/api/auth/logout",
                                "/api/auth/forgot-password",
                                "/api/auth/verify-reset-otp",
                                "/api/auth/reset-password"
                        ).permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}