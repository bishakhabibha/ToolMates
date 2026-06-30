package com.javaproj.ToolMates.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origins:http://localhost:63342,http://127.0.0.1:63342,http://localhost:8080,https://toolmates.onrender.com}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .addFilterBefore(sessionRequiredFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/",
                                "/error",
                                "/dashboard",
                                "/login",
                                "/signup",
                                "/rental-history",
                                "/rental-details",
                                "/**/*.html",
                                "/**/*.css",
                                "/**/*.js",
                                "/api/auth/send-otp",
                                "/api/auth/verify-otp",
                                "/api/auth/signup",
                                "/api/auth/login",
                                "/api/auth/logout",
                                "/api/auth/me",
                                "/api/auth/forgot-password",
                                "/api/auth/verify-reset-otp",
                                "/api/auth/reset-password",
                                "/api/tools/**",
                                "/api/rent-requests/**",
                                "/api/rental-history/**",
                                "/api/messages/**",
                                "/api/notifications/**",
                                "/api/reviews/**",
                                "/api/profile/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }

    @Bean
    public OncePerRequestFilter sessionRequiredFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                if (requiresSession(request) && request.getSession(false) == null) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Not logged in.\"}");
                    return;
                }
                if (requiresSession(request) && request.getSession(false).getAttribute("userId") == null) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Not logged in.\"}");
                    return;
                }
                filterChain.doFilter(request, response);
            }
        };
    }

    private boolean requiresSession(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();

        if (HttpMethod.OPTIONS.matches(method)) return false;
        if (!path.startsWith("/api/")) return false;
        if (path.startsWith("/api/auth/")) return false;

        if (path.startsWith("/api/rent-requests/")) return true;
        if (path.startsWith("/api/rental-history/")) return true;
        if (path.startsWith("/api/messages/") || path.equals("/api/messages")) return true;
        if (path.startsWith("/api/notifications/")) return true;
        if (path.equals("/api/profile/me") || path.startsWith("/api/profile/me/")) return true;

        if (path.startsWith("/api/tools/")) {
            return !HttpMethod.GET.matches(method);
        }
        if (path.equals("/api/reviews")) {
            return HttpMethod.POST.matches(method);
        }

        return false;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = new ArrayList<>(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList());
        if (!origins.contains("https://*.onrender.com")) {
            origins.add("https://*.onrender.com");
        }
        configuration.setAllowedOriginPatterns(origins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization", "Accept"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
