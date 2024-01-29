package com.prime.gateway.gateway.infrastructure.redis;

import com.prime.gateway.gateway.domain.account.entity.Account;
import com.prime.gateway.gateway.infrastructure.security.ProgresoUserDetails;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisService.class);

    @CacheEvict(value = "account", key = "#email")
    public ResponseEntity<Object> delete(ResponseEntity<Object> response, String email) {
        LOGGER.info("Successfully deleted Account from Redis");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response.getBody());
    }

    @CachePut(value = "account", key = "#account.email")
    public ProgresoUserDetails update(Account account) {
        LOGGER.info("Successfully updated Account from Redis");
      return new ProgresoUserDetails(account.getId(), account.getUserId(), account.getEmail(),
            account.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + account.getRole())));
    }
}
