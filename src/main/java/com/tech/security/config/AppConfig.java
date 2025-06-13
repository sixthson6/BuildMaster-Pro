package com.tech.security.config; // Recommended package, adjust if your base package is different

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration class to define application-wide beans.
 * This class provides the PasswordEncoder bean which is used throughout
 * the application for password hashing and verification.
 */
@Configuration
public class AppConfig {

    /**
     * Defines a BCryptPasswordEncoder bean.
     * This bean is used by Spring Security for hashing user passwords
     * (e.g., during signup) and for verifying passwords during login.
     * It's placed in a separate configuration class to avoid circular
     * dependencies that can arise when SecurityConfig itself depends
     * on PasswordEncoder and other services that also depend on it.
     *
     * @return An instance of BCryptPasswordEncoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
