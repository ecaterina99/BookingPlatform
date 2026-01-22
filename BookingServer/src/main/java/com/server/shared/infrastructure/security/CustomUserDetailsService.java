package com.server.shared.infrastructure.security;

import com.server.organization.domain.users.User;
import com.server.organization.domain.users.UserEmail;
import com.server.organization.domain.users.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(new UserEmail(email))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new CustomUserPrincipal(
                user.getId(),
                user.getEmail().value(),
                user.getPassword().value(),
                user.getGlobalRole()
        );
    }
}
