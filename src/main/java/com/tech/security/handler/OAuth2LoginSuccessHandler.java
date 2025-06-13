package com.tech.security.handler; // IMPORTANT: Ensure this package matches

import com.tech.security.repository.UserRepository;
import com.tech.security.jwt.JwtUtils;
import com.tech.security.model.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository; // To fetch the locally saved User

    // Frontend redirect URL where the JWT will be sent
    @Value("${tech.app.oauth2SuccessRedirectUrl}")
    private String oauth2SuccessRedirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        logger.info("OAuth2LoginSuccessHandler: Authentication successful. Principal type: {}", authentication.getPrincipal().getClass().getName());

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        logger.debug("OAuth2LoginSuccessHandler: Raw OAuth2User attributes from principal: {}", oAuth2User.getAttributes());

        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            email = oAuth2User.getAttribute("login"); // Fallback for providers like GitHub
            if (email != null && !email.contains("@")) {
                logger.warn("OAuth2LoginSuccessHandler: 'email' attribute was null, 'login' attribute '{}' does not contain '@'. Falling back to null email.", email);
                email = null; // Treat as no email found
            }
            if (email == null) {
                logger.error("OAuth2LoginSuccessHandler: Neither 'email' nor 'login' attribute provided a valid email for current user. Attributes: {}", oAuth2User.getAttributes());
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email not found from OAuth2 provider.");
                return;
            }
        }
        logger.info("OAuth2LoginSuccessHandler: Determined email for JWT: {}", email);

        // Retrieve the local User entity that CustomOAuth2UserService should have already created/updated
        Optional<User> userOptional = userRepository.findByEmail(email);
        User userDetails;

        if (userOptional.isPresent()) {
            userDetails = userOptional.get();
            logger.info("OAuth2LoginSuccessHandler: User with email {} (ID: {}) FOUND in local DB for JWT generation.", email, userDetails.getId());
        } else {
            // This error indicates a failure in CustomOAuth2UserService or a transaction issue.
            logger.error("OAuth2LoginSuccessHandler: OAuth2 authenticated user with email {} NOT FOUND in local DB after CustomOAuth2UserService. This is a critical issue.", email);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Local user account issue after OAuth2 login. User not found in DB.");
            return;
        }

        // Generate JWT token for the authenticated user
        // Pass our local UserDetails object (which is 'userDetails') to generate the token
        // since JwtUtils expects a UserDetails principal (from UserDetailsServiceImpl or User entity)
        // rather than directly the OAuth2AuthenticationToken.
        // Convert Authentication to UsernamePasswordAuthenticationToken for JwtUtils
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken jwtAuthentication =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        userDetails, // Our local UserDetails object
                        null,
                        userDetails.getAuthorities());

        String jwtToken = jwtUtils.generateJwtToken(jwtAuthentication);
        logger.info("OAuth2LoginSuccessHandler: Generated JWT token for user: {}", email);

        // Build the redirect URI including the JWT token
        String redirectUrl = UriComponentsBuilder.fromUriString(oauth2SuccessRedirectUrl)
                .queryParam("token", jwtToken)
                .build().toUriString();
        logger.info("OAuth2LoginSuccessHandler: Redirecting to: {}", redirectUrl);

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
