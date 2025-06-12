package com.tech.security.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final OAuth2UserProcessingService oauth2UserProcessingService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        logger.info("Attempting to load OAuth2 user for registration: {}", userRequest.getClientRegistration().getRegistrationId());

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String email = extractEmail(oAuth2User, registrationId);
        String name = extractName(oAuth2User);

        logger.info("OAuth2: Processing user with email: {} and name: {} from provider: {}", email, name, registrationId);

        // Process user in a separate transaction that commits immediately
        oauth2UserProcessingService.processOAuth2User(email, name, registrationId);

        return oAuth2User;
    }

    private String extractEmail(OAuth2User oAuth2User, String registrationId) {
        String email = oAuth2User.getAttribute("email");

        if (email == null) {
            // Fallback for GitHub and other providers that might use 'login'
            email = oAuth2User.getAttribute("login");
            if (email == null) {
                logger.error("OAuth2: Email attribute not found for user from provider {}. Available attributes: {}",
                        registrationId, oAuth2User.getAttributes().keySet());
                throw new OAuth2AuthenticationException("Email not found for user from OAuth2 provider: " + registrationId);
            }
            logger.info("OAuth2: Using 'login' attribute as email for provider: {}", registrationId);
        }

        return email;
    }

    private String extractName(OAuth2User oAuth2User) {
        String name = oAuth2User.getAttribute("name");
        if (name == null) {
            name = oAuth2User.getAttribute("login");
        }
        return name;
    }
}