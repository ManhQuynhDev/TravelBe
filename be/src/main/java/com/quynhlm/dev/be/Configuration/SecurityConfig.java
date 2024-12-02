package com.quynhlm.dev.be.Configuration;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
        private final String[] PUBLIC_POST_ENDPOINTS = { "/onboarding/register", "/onboarding/login",
                        "/onboarding/send",
                        "/onboarding/verify", "/onboarding/set-password", "onboarding/auth/token" };

        private static final String SIGNER_KEY = "/q5Il7oI//Hiv4va97MQAtYOaktNo188-23WY12YVRCRGBEwYECRg0T6YcrEzYWb";

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http.authorizeHttpRequests(
                                request -> request.requestMatchers(HttpMethod.POST, PUBLIC_POST_ENDPOINTS).permitAll()
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/message/index").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/message/message").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/js/**", "/css/**", "/images/**").permitAll()
                                                .requestMatchers("/ws-message/**").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/web-server/**").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/assets/**").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/web-server/assets/**").permitAll()
                                                .anyRequest().authenticated()); // Token

                http.oauth2ResourceServer(oauth2 -> oauth2
                                .jwt(j -> j.decoder(jwtDecoder())
                                                .jwtAuthenticationConverter(jwtAuthenticationConverter())));

                http.csrf(t -> t.disable());

                return http.build();
        }

        @Bean
        public CorsFilter corsFilter() {
                CorsConfiguration config = new CorsConfiguration();
                config.addAllowedOriginPattern("*");
                config.addAllowedMethod("*");
                config.addAllowedHeader("*");
                config.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);
                return new CorsFilter(source);
        }

        @Bean
        JwtAuthenticationConverter jwtAuthenticationConverter() {
                // Tạo một đối tượng JwtGrantedAuthoritiesConverter
                JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

                // Đặt tiền tố "ROLE_" cho các quyền được lấy từ JWT
                jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

                // Tạo một đối tượng JwtAuthenticationConverter
                JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();

                // Thiết lập JwtGrantedAuthoritiesConverter cho JwtAuthenticationConverter
                jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

                // Trả về đối tượng JwtAuthenticationConverter đã được cấu hình
                return jwtAuthenticationConverter;
        }

        @Bean
        JwtDecoder jwtDecoder() {
                SecretKeySpec secretKeySpec = new SecretKeySpec(SIGNER_KEY.getBytes(), "HS512");
                return NimbusJwtDecoder
                                .withSecretKey(secretKeySpec)
                                .macAlgorithm(MacAlgorithm.HS512)
                                .build();
        }
}

// @Bean
// JwtAuthenticationConverter jwtAuthenticationConverter() {
// JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new
// JwtGrantedAuthoritiesConverter();

// jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

// JwtAuthenticationConverter jwtAuthenticationConverter = new
// JwtAuthenticationConverter();

// jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

// return jwtAuthenticationConverter;
// }
