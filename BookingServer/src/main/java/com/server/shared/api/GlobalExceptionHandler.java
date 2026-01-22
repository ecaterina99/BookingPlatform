package com.server.shared.api;

import com.server.organization.domain.users.UserAlreadyExistsException;
import com.server.organization.domain.organizations.OrganizationAlreadyExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNoResource(NoResourceFoundException e) {
        return ApiError.of(HttpStatus.NOT_FOUND, "NOT_FOUND", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> fieldErrors.put(err.getField(), err.getDefaultMessage()));

        return ApiError.builder()
                .error("VALIDATION_FAILED")
                .message("Invalid input data")
                .status(HttpStatus.BAD_REQUEST.value())
                .fieldErrors(fieldErrors)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleGenericException(Exception e) {
        return ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "Unexpected error occurred");
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleUserAlreadyExists(UserAlreadyExistsException e) {
        return ApiError.of(HttpStatus.CONFLICT, "USER_ALREADY_EXISTS", e.getMessage());
    }

    @ExceptionHandler(OrganizationAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleOrganizationAlreadyExists(OrganizationAlreadyExistsException e) {
        return ApiError.of(HttpStatus.CONFLICT, "ORGANIZATION_ALREADY_EXISTS", e.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleBadCredentials(BadCredentialsException e) {
        return ApiError.of(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", e.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleAuthenticationException(AuthenticationException e) {
        return ApiError.of(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED", "Authentication failed");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleAccessDenied(AccessDeniedException e) {
        return ApiError.of(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "You don't have permission to access this resource");
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleEntityNotFound(EntityNotFoundException e) {
        return ApiError.of(HttpStatus.NOT_FOUND, "NOT_FOUND", e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalState(IllegalStateException e) {
        return ApiError.of(HttpStatus.BAD_REQUEST, "INVALID_OPERATION", e.getMessage());
    }

    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleSecurityException(SecurityException e) {
        return ApiError.of(HttpStatus.FORBIDDEN, "ACCESS_DENIED", e.getMessage());
    }
}
