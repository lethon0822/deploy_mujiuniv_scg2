package com.green.scg2.configuration.model;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Getter
public class UserPrincipal implements UserDetails {
    private final long signedUserId;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(long signedUserId, String role) {
        this.signedUserId = signedUserId;
        this.authorities= Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return null;
    }
}
