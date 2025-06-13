package com.tech.exception;

import com.tech.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;


@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final AuditLogService auditLogService;

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorMessage> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        String username = "anonymous";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String && "anonymousUser".equals(authentication.getPrincipal()))) {
            username = authentication.getName(); // Get authenticated user's name
        }

        logger.warn("Access Denied: {} - {} by user {}", ex.getMessage(), request.getDescription(false), username);
        // NEW: Log unauthorized access attempt (403 Forbidden)
        auditLogService.logUnauthorizedAccess(username, request.getDescription(false), "Insufficient privileges: " + ex.getMessage());

        ErrorMessage message = new ErrorMessage(
                HttpStatus.FORBIDDEN.value(),
                System.currentTimeMillis(),
                "Access Denied: You do not have sufficient permissions to access this resource.",
                request.getDescription(false));
        return new ResponseEntity<>(message, HttpStatus.FORBIDDEN);
    }


    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorMessage> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        String emailAttempted = "unknown";

        logger.warn("Authentication Failed: {} - {}", ex.getMessage(), request.getDescription(false));

        ErrorMessage message = new ErrorMessage(
                HttpStatus.UNAUTHORIZED.value(),
                System.currentTimeMillis(),
                "Authentication Failed: Invalid email or password.",
                request.getDescription(false));
        return new ResponseEntity<>(message, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> handleGlobalException(Exception ex, WebRequest request) {
        String username = "anonymous";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String && "anonymousUser".equals(authentication.getPrincipal()))) {
            username = authentication.getName();
        }

        logger.error("An unexpected error occurred: {} - {} by user {}", ex.getMessage(), request.getDescription(false), username, ex); // Log full stack trace
        auditLogService.logUnauthorizedAccess(username, request.getDescription(false), "Unexpected internal server error: " + ex.getMessage());

        ErrorMessage message = new ErrorMessage(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                System.currentTimeMillis(),
                "An unexpected error occurred. Please try again later.",
                request.getDescription(false));
        return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static class ErrorMessage {
        private int statusCode;
        private long timestamp;
        private String message;
        private String description;

        public ErrorMessage(int statusCode, long timestamp, String message, String description) {
            this.statusCode = statusCode;
            this.timestamp = timestamp;
            this.message = message;
            this.description = description;
        }

        public int getStatusCode() { return statusCode; }
        public long getTimestamp() { return timestamp; }
        public String getMessage() { return message; }
        public String getDescription() { return description; }
    }
}
