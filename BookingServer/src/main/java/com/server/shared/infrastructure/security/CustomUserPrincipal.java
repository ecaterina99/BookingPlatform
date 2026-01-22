package com.server.shared.infrastructure.security;

import com.server.organization.domain.enums.GlobalRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserPrincipal implements UserDetails {

    private final int userId;
    private final String email;
    private final String password;
    private final GlobalRole globalRole;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserPrincipal(
            int userId,
            String email,
            String password,
            GlobalRole globalRole
    ) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.globalRole = globalRole;
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + globalRole.name()));
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

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
