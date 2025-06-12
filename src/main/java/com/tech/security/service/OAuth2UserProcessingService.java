package com.tech.security.service;

import com.tech.security.repository.RoleRepository;
import com.tech.security.repository.UserRepository;
import com.tech.security.model.Role;
import com.tech.security.model.User;
import com.tech.security.model.Role.ERole;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OAuth2UserProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2UserProcessingService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public User processOAuth2User(String email, String name, String registrationId) {
        logger.info("Processing OAuth2 user with email: {} from provider: {}", email, registrationId);

        // Normalize email
        email = email.toLowerCase().trim();

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            logger.info("OAuth2: Existing user found with email: {} and ID: {}", email, user.getId());

            // Update name if it has changed and is not null
            if (name != null && !name.equals(user.getUsername())) {
                logger.info("OAuth2: Updating username from '{}' to '{}'", user.getUsername(), name);
                user.setUsername(name);
                user = userRepository.saveAndFlush(user);
            }
        } else {
            user = createNewUser(email, name, registrationId);
        }

        logger.info("OAuth2: Successfully processed user with email: {} and ID: {}", email, user.getId());
        return user;
    }

    private User createNewUser(String email, String name, String registrationId) {
        logger.info("OAuth2: Creating new user with email: {} from provider: {}", email, registrationId);

        Role contractorRole = roleRepository.findByName(ERole.ROLE_CONTRACTOR)
                .orElseThrow(() -> {
                    logger.error("OAuth2: ROLE_CONTRACTOR not found in database");
                    return new OAuth2AuthenticationException("Error: ROLE_CONTRACTOR not found in database. Please initialize roles.");
                });

        Set<Role> roles = new HashSet<>(Collections.singletonList(contractorRole));

        String dummyEncodedPassword = passwordEncoder.encode("OAUTH2_DUMMY_PASSWORD_" + System.currentTimeMillis());

        User user = User.builder()
                .username(name != null ? name : email)
                .email(email)
                .password(dummyEncodedPassword)
                .roles(roles)
                .build();

        user = userRepository.saveAndFlush(user);
        logger.info("OAuth2: Successfully created new user with email: {} and ID: {}", email, user.getId());

        return user;
    }
}