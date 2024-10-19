package com.quynhlm.dev.be.Configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    // private final String[] PUBLIC_POST_ENDPOINTS = { "/onboarding/register", "/onboarding/login" , "/onboarding/send","/onboarding/verify","/onboarding/set-password" };
    // private final String[] PUBLIC_GET_ENDPOINTS = { "/onboarding/users"};

    protected static final String SIGNER_KEY = "/q5Il7oI//Hiv4va97MQAtYOaktNo188-23WY12YVRCRGBEwYECRg0T6YcrEzYWb\r\n";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(
                request -> request.requestMatchers(HttpMethod.POST).permitAll()
                        .requestMatchers(HttpMethod.GET).permitAll()
                        .requestMatchers(HttpMethod.DELETE).permitAll()
                        .anyRequest().authenticated()); // Token
        http.csrf(t -> t.disable());

        return http.build();
    }
}
