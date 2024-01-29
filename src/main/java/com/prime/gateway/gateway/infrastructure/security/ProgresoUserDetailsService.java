package com.prime.gateway.gateway.infrastructure.security;

import com.prime.gateway.gateway.domain.account.entity.Account;
import com.prime.gateway.gateway.infrastructure.util.WebClientUtil;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ProgresoUserDetailsService implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProgresoUserDetailsService.class);
    private final WebClientUtil webClient;


    public ProgresoUserDetailsService(WebClientUtil webClient) {
        this.webClient = webClient;
    }

    @Cacheable(value = "account", key = "#email")
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        LOGGER.info("Account from Redis is null (doesn't exist) - trying to get it from Java API");

        Account account = webClient.send(email);
        LOGGER.info("Account successfully retrieved from Java API");

        LOGGER.info("Account successfully saved in Redis");

        return new ProgresoUserDetails(account.getId(), account.getUserId(), account.getEmail(),
            account.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + account.getRole())));
    }

}
