package com.prime.gateway.gateway.domain.account.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prime.gateway.gateway.domain.account.entity.Account;
import com.prime.gateway.gateway.infrastructure.redis.RedisService;
import com.prime.gateway.gateway.infrastructure.util.WebClientUtil;
import java.util.LinkedHashMap;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl implements AccountService {

    private final WebClientUtil webClient;
    private final RedisService redisService;

    public AccountServiceImpl(WebClientUtil webClient, RedisService redisService) {
        this.webClient = webClient;
        this.redisService = redisService;
    }

    @Override
    public ResponseEntity<Object> updateRedis(HttpServletRequest request,
        ResponseEntity<Object> response) {
        var body = (LinkedHashMap<?, ?>) response.getBody();
        assert body != null;

        if (request.getRequestURI().contains("password")) {
            updatePassword(body);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response.getBody());
        } else {
            return handleRequestMethod(request, response, body);
        }
    }

    @Override
    public void deleteRedis(ResponseEntity<Object> response, String email) {
        redisService.delete(response, email);
    }

    @Override
    public String getAccountEmail(Long userId) {
        Account account = webClient.send(userId);
        return account.getEmail();
    }

    private ResponseEntity<Object> handleRequestMethod(HttpServletRequest request,
        ResponseEntity<Object> response, LinkedHashMap<?, ?> body) {
        if (request.getMethod().equals("PATCH")) {
            if (request.getRequestURI().contains("archive") && body.containsKey("email")) {
                return redisService.delete(response, (String) body.get("email"));
            } else {
                Account accountFromResponse = new ObjectMapper().convertValue(body.get("account"),
                    Account.class);
                Account account = webClient.send(accountFromResponse.getEmail());
                redisService.update(account);
                return null;
            }
        }

        if (request.getMethod().equals("DELETE")) {
            return redisService.delete(response, (String) body.get("email"));
        }

        return null;
    }

    private void updatePassword(LinkedHashMap<?, ?> response) {
        String email = response.get("email").toString();
        Account account = webClient.send(email);
        redisService.update(account);
    }
}
