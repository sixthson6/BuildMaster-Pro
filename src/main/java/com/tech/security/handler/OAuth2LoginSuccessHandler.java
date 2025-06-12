package com.tech.security.handler;

import com.tech.security.repository.UserRepository;
import com.tech.security.jwt.JwtUtils;
import com.tech.security.model.User;
import lombok.RequiredArgsConstructor;
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

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    // Frontend redirect URL where the JWT will be sent
    @Value("${tech.app.oauth2SuccessRedirectUrl}")
    private String oauth2SuccessRedirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            email = oAuth2User.getAttribute("login");
            if (email == null) {
                logger.error("Could not retrieve email from OAuth2 provider for current user");
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email not found from OAuth2 provider.");
                return;
            }
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        User userDetails;

        if (userOptional.isPresent()) {
            userDetails = userOptional.get();
        } else {
            logger.error("OAuth2 authenticated user not found in local DB after CustomOAuth2UserService. Email");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Local user account issue after OAuth2 login.");
            return;
        }

        String jwtToken = jwtUtils.generateJwtToken(authentication);

        // Build the redirect URI including the JWT token
        // We'll append the token as a query parameter for the frontend to pick up
        String redirectUrl = UriComponentsBuilder.fromUriString(oauth2SuccessRedirectUrl)
                .queryParam("token", jwtToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}