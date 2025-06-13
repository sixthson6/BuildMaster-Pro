package com.tech.security.oauth2;

import com.tech.security.repository.RoleRepository;
import com.tech.security.repository.UserRepository;
import com.tech.security.model.Role;
import com.tech.security.model.User;
import com.tech.security.model.Role.ERole;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder; // Injected from AppConfig

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        logger.info("CustomOAuth2UserService: Starting loadUser for client registration: {}", userRequest.getClientRegistration().getRegistrationId());

        OAuth2User oAuth2User = super.loadUser(userRequest);
        logger.debug("CustomOAuth2UserService: Raw OAuth2User attributes from provider: {}", oAuth2User.getAttributes());

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        if (email == null) {
            email = oAuth2User.getAttribute("login");
            if (email != null && !email.contains("@")) {
                logger.warn("CustomOAuth2UserService: 'email' attribute was null, 'login' attribute '{}' does not contain '@'. Falling back to null email.", email);
                email = null;
            }
            if (email == null) {
                logger.error("CustomOAuth2UserService: Neither 'email' nor 'login' attribute provided a valid email for provider {}. Attributes: {}",
                        registrationId, oAuth2User.getAttributes());
                throw new OAuth2AuthenticationException("Email not found for user from OAuth2 provider: " + registrationId);
            }
        }
        if (name == null) {
            name = oAuth2User.getAttribute("login");
            if (name == null) {
                name = "OAuth2 User";
            }
        }


        logger.info("CustomOAuth2UserService: Processing user with email: {} from provider: {}", email, registrationId);

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            logger.info("CustomOAuth2UserService: User with email {} (ID: {}) FOUND in local DB.", email, user.getId());
            if (name != null && !name.equals(user.getUsername())) {
                user.setUsername(name);
                logger.info("CustomOAuth2UserService: Updating username for existing user {} to {}.", email, name);
                userRepository.save(user);
            }
        } else {
            logger.info("CustomOAuth2UserService: User with email {} NOT FOUND in local DB. Creating new user.", email);

            Role contractorRole = roleRepository.findByName(ERole.ROLE_CONTRACTOR)
                    .orElseThrow(() -> new OAuth2AuthenticationException("Error: ROLE_CONTRACTOR not found in database. Please initialize roles via DataLoader."));
            logger.info("CustomOAuth2UserService: Assigning ROLE_CONTRACTOR to new user {}.", email);

            Set<Role> roles = new HashSet<>(Collections.singletonList(contractorRole));

            String dummyEncodedPassword = passwordEncoder.encode("OAUTH2_DUMMY_PASSWORD_" + System.currentTimeMillis());
            logger.debug("CustomOAuth2UserService: Generated and encoded dummy password for new OAuth2 user.");

            user = User.builder()
                    .username(name)
                    .email(email)
                    .password(dummyEncodedPassword)
                    .roles(roles)
                    .build();
            User savedUser = userRepository.save(user);
            logger.info("CustomOAuth2UserService: New user with email {} (ID: {}) registered successfully.", email, savedUser.getId());
        }

        return oAuth2User;
    }
}
