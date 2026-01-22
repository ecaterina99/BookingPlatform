package com.server.shared.infrastructure.security;

import com.server.organization.domain.enums.GlobalRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityCurrentUserProvider implements CurrentUserProvider {

    @Override
    public int getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserPrincipal)) {
            throw new IllegalStateException("No authenticated user found");
        }
        CustomUserPrincipal principal = (CustomUserPrincipal) auth.getPrincipal();
        return principal.getUserId();
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserPrincipal)) {
            throw new IllegalStateException("No authenticated user found");
        }
        CustomUserPrincipal principal = (CustomUserPrincipal) auth.getPrincipal();
        return principal.getGlobalRole();
    }

    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserPrincipal;
    }

    public boolean isGlobalAdmin() {
        return hasRole("ROLE_GLOBAL_ADMIN");
    }
}
