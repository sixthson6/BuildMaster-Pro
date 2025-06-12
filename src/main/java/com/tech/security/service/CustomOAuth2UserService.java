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
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        logger.info("Attempting to load OAuth2 user for registration: {}", userRequest.getClientRegistration().getRegistrationId());


        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        if (email == null) {
            email = oAuth2User.getAttribute("login");
            if (email == null) {
                logger.error("OAuth2: Email attribute not found for user from provider {}. Attributes: {}",
                        registrationId, oAuth2User.getAttributes());
                throw new OAuth2AuthenticationException("Email not found for user from OAuth2 provider: " + registrationId);
            }
        }
        if (name == null) {
            name = oAuth2User.getAttribute("login");
        }

        logger.info("OAuth2: Processing user with email: {} from provider: {}", email, registrationId);

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            logger.info("OAuth2: User with email {} already exists. Updating if necessary.", email);
            if (name != null && !name.equals(user.getUsername())) {
                user.setUsername(name);
            }
            userRepository.save(user);
        } else {
            logger.info("OAuth2: New user. Registering user with email {} and assigning ROLE_CONTRACTOR.", email);

            Role contractorRole = roleRepository.findByName(ERole.ROLE_CONTRACTOR)
                    .orElseThrow(() -> new OAuth2AuthenticationException("Error: ROLE_CONTRACTOR not found in database. Please initialize roles."));

            Set<Role> roles = new HashSet<>(Collections.singletonList(contractorRole));

            String dummyEncodedPassword = passwordEncoder.encode("OAUTH2_DUMMY_PASSWORD_" + System.currentTimeMillis());

            user = User.builder()
                    .username(name != null ? name : email)
                    .email(email)
                    .password(dummyEncodedPassword)
                    .roles(roles)
                    .build();
            userRepository.save(user);
        }

        return oAuth2User;
    }
}
