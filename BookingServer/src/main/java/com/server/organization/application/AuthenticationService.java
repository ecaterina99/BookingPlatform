package com.server.organization.application;

import com.server.organization.domain.users.User;
import com.server.organization.domain.users.UserEmail;
import com.server.organization.domain.users.UserRepository;
import com.server.shared.api.AuthRequest;
import com.server.shared.api.AuthResponse;
import com.server.shared.api.RegisterRequest;
import com.server.shared.api.UserInfoResponse;
import com.server.shared.infrastructure.security.CustomUserPrincipal;
import com.server.shared.infrastructure.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserService userService;
    private final long expirationMs;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            UserService userService,
            @Value("${jwt.expiration-ms}") long expirationMs
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userService = userService;
        this.expirationMs = expirationMs;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(new UserEmail(request.email()))
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword().value())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(
                user.getEmail().value(),
                user.getId(),
                user.getGlobalRole()
        );

        return new AuthResponse(token, expirationMs);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Use existing UserService to create the user
        CreateUserCommand command = new CreateUserCommand(
                request.email(),
                request.password(),
                request.fullName()
        );
        userService.createUser(command);

        // Now authenticate and return token
        User user = userRepository.findByEmail(new UserEmail(request.email()))
                .orElseThrow(() -> new RuntimeException("User registration failed"));

        String token = jwtService.generateToken(
                user.getEmail().value(),
                user.getId(),
                user.getGlobalRole()
        );

        return new AuthResponse(token, expirationMs);
    }

    public UserInfoResponse getCurrentUser() {
        CustomUserPrincipal principal = (CustomUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserInfoResponse(
                user.getId(),
                user.getEmail().value(),
                user.getFullName(),
                user.getGlobalRole()
        );
    }
}
