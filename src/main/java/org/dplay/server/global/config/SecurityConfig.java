package org.dplay.server.global.config;

import lombok.RequiredArgsConstructor;
import org.dplay.server.global.auth.DPlayAccessDeniedHandler;
import org.dplay.server.global.auth.DPlayJwtAuthenticationEntryPoint;
import org.dplay.server.global.auth.filter.JwtAuthenticationFilter;
import org.dplay.server.global.auth.filter.JwtExceptionFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtExceptionFilter jwtExceptionFilter;
    private final DPlayJwtAuthenticationEntryPoint dPlayJwtAuthenticationEntryPoint;
    private final DPlayAccessDeniedHandler dPlayAccessDeniedHandler;

    public static final String[] AUTH_WHITE_LIST = {
            "/health/success/ok",
            "/health/success/created",
            "/v1/auth/login",
            "/v1/auth/signup"
    };

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .requestCache(RequestCacheConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception ->
                {
                    exception.authenticationEntryPoint(dPlayJwtAuthenticationEntryPoint);
                    exception.accessDeniedHandler(dPlayAccessDeniedHandler);
                });


        http.authorizeHttpRequests(auth -> {
                    auth.requestMatchers(AUTH_WHITE_LIST).permitAll();
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtExceptionFilter, JwtAuthenticationFilter.class);

        return http.build();
    }
}
