package com.prime.gateway.gateway.infrastructure.config;

import com.prime.gateway.gateway.infrastructure.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ApplicationConfiguration {


    @Value("${gateway.http.api-key-header}")
    private String apiKeyRequestHeader;

    @Value("${gateway.http.api-key-value}")
    private String apiKeyRequestValue;

    @Value("${gateway.http.user-role-header}")
    private String userRole;

    @Value("${gateway.http.user-email-header}")
    private String userEmail;

    @Value("${gateway.http.user-id-header}")
    private String userId;

    @Bean
    public WebClient webClient(JwtUtil jwtUtil) {

        return WebClient.builder()
                .filter(((request, next) -> {
                    if (jwtUtil.getAuthorizationHeader() == null) {
                        return next.exchange(request);
                    }
                    String token = jwtUtil.getAuthorizationHeader().substring(7);
                    ClientRequest newRequest = ClientRequest.from(request)
                            .header(userEmail, jwtUtil.getUsername(token))
                            .header(userId, jwtUtil.getUserId(token))
                            .header(userRole, getRole())
                            .build();
                    return next.exchange(newRequest);
                }))
                .defaultHeaders(headers -> {
                    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                    headers.add(apiKeyRequestHeader, apiKeyRequestValue);
                })
                .build();
    }

    private static String getRole() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().
                findFirst().map(GrantedAuthority::getAuthority).orElse(null);
    }
}
