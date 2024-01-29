package com.prime.gateway.gateway.domain.account.service;

import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;

public interface AccountService {
    ResponseEntity<Object> updateRedis(HttpServletRequest request, ResponseEntity<Object> response);

    void deleteRedis(ResponseEntity<Object> response, String email);

    String getAccountEmail(Long userId);
}
