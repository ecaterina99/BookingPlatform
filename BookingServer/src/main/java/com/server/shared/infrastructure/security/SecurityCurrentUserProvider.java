package com.server.shared.infrastructure.security;

import com.server.organization.domain.enums.GlobalRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityCurrentUserProvider implements CurrentUserProvider {

    @Override
    public int getUserId() {
        return getJwt()
                .orElseThrow(() -> new IllegalStateException("No authenticated user found"))
                .getClaim("userId");
    }

    @Override
    public boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }

    public GlobalRole getGlobalRole() {
        String role = getJwt()
                .orElseThrow(() -> new IllegalStateException("No authenticated user found"))
                .getClaim("globalRole");
        return GlobalRole.valueOf(role);
    }

    public String getEmail() {
        return getJwt()
                .orElseThrow(() -> new IllegalStateException("No authenticated user found"))
                .getSubject();
    }

    public boolean isAuthenticated() {
        return getJwt().isPresent();
    }

    public boolean isGlobalAdmin() {
        return hasRole("ROLE_GLOBAL_ADMIN");
    }

    private Optional<Jwt> getJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return Optional.of(jwtAuth.getToken());
        }
        return Optional.empty();
    }
}
