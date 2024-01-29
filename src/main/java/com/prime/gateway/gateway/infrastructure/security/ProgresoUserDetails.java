package com.prime.gateway.gateway.infrastructure.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class ProgresoUserDetails implements UserDetails {

    private final Long id;
    private final Long userId;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public ProgresoUserDetails(Long id, Long userId, String email, String password,
        Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Long getUserId() {
        return userId;
    }
}
