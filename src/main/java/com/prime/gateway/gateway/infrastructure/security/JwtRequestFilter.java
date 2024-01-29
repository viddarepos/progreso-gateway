package com.prime.gateway.gateway.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtRequestFilter.class);

    @Value("Authorization")
    private String headerName;

    @Value("Bearer")
    private String tokenPrefix;

    @Value("${gateway.http.user-role-header}")
    private String userRoleRequestHeader;

    private final ProgresoUserDetailsService progresoUserDetailsService;
    private final JwtUtil jwtUtil;

    public JwtRequestFilter(ProgresoUserDetailsService progresoUserDetailsService, JwtUtil jwtUtil) {
        this.progresoUserDetailsService = progresoUserDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwtToken = getJwtFromRequest(request);

            if (StringUtils.hasText(jwtToken) && jwtUtil.validateJwtToken(jwtToken)) {
                String email = jwtUtil.getUsername(jwtToken);

                UserDetails userDetails = progresoUserDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }
        } catch (Exception ex) {
            LOGGER.error("Cannot set authentication", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(headerName);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(tokenPrefix)) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
