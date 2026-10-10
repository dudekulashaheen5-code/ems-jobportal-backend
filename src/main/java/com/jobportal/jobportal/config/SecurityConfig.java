package com.jobportal.jobportal.config;

import com.jobportal.jobportal.security.JwtAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of("http://localhost:5173")
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of("*")
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .cors(cors ->
                    cors.configurationSource(
                            corsConfigurationSource()
                    )
            )

            .formLogin(form -> form.disable())

            .httpBasic(basic -> basic.disable())

            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                        HttpMethod.OPTIONS,
                        "/**"
                ).permitAll()

                .requestMatchers(
                        "/api/users/register"
                ).permitAll()

                .requestMatchers(
                        "/api/users/login"
                ).permitAll()

                .requestMatchers(
                        HttpMethod.GET,
                        "/api/company-jobs/**"
                ).permitAll()

                /*
                 * External company applications
                 */

                .requestMatchers(
                        HttpMethod.POST,
                        "/api/external-applications/click"
                ).hasRole("USER")

                .requestMatchers(
                        HttpMethod.GET,
                        "/api/external-applications/user/**"
                ).authenticated()

                .requestMatchers(
                        HttpMethod.GET,
                        "/api/external-applications/**"
                ).hasRole("ADMIN")

                /*
                 * Manual jobs
                 */

                .requestMatchers(
                        HttpMethod.POST,
                        "/api/jobs/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                        HttpMethod.DELETE,
                        "/api/jobs/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                        HttpMethod.GET,
                        "/api/jobs/**"
                ).authenticated()

                /*
                 * Manual applications
                 */

                .requestMatchers(
                        HttpMethod.POST,
                        "/api/applications"
                ).hasRole("USER")

                .requestMatchers(
                        HttpMethod.GET,
                        "/api/applications/user/**"
                ).authenticated()

                .requestMatchers(
                        HttpMethod.GET,
                        "/api/applications/job/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                        HttpMethod.PUT,
                        "/api/applications/**"
                ).hasRole("ADMIN")

                /*
                 * User profile
                 */

                .requestMatchers(
                        HttpMethod.POST,
                        "/api/profile"
                ).hasRole("USER")

                .requestMatchers(
                        HttpMethod.PUT,
                        "/api/profile/*/resume"
                ).hasRole("USER")

                .requestMatchers(
                        HttpMethod.PUT,
                        "/api/profile/**"
                ).hasRole("USER")

                .requestMatchers(
                        HttpMethod.GET,
                        "/api/profile/*/resume"
                ).hasRole("ADMIN")

                .requestMatchers(
                        HttpMethod.GET,
                        "/api/profile/**"
                ).authenticated()

                .anyRequest().authenticated()
            )

            .addFilterBefore(
                    jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}