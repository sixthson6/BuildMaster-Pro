package com.tech.security.oauth2;

import com.tech.security.jwt.JwtUtils;
import com.tech.security.model.Role;
import com.tech.security.model.User;
import com.tech.security.repository.RoleRepository;
import com.tech.security.repository.UserRepository;
import com.tech.security.service.UserDetailsServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    @Value("${app.oauth2.authorizedRedirectUri:http://localhost:8080/oauth2-success.html}")
    private String authorizedRedirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        if (response.isCommitted()) {
            log.debug("Response has already been committed. Unable to redirect to {}", authorizedRedirectUri);
            return;
        }

        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

            String email = extractEmail(oAuth2User);
            String name = extractName(oAuth2User);
            String provider = extractProvider(oAuth2User);

            if (email == null || email.isEmpty()) {
                log.error("Email not found in OAuth2 user attributes");
                redirectToErrorPage(response, "Email not found in OAuth2 provider response");
                return;
            }

            User user = findOrCreateUser(email, name, provider);

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            org.springframework.security.authentication.UsernamePasswordAuthenticationToken authToken =
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

            String jwtToken = jwtUtils.generateJwtToken(authToken);

            String targetUrl = UriComponentsBuilder.fromUriString(authorizedRedirectUri)
                    .queryParam("token", jwtToken)
                    .queryParam("email", email)
                    .queryParam("name", name)
                    .build().toUriString();

            log.info("OAuth2 authentication successful for user: {}, redirecting to: {}", email, targetUrl);
            getRedirectStrategy().sendRedirect(request, response, targetUrl);

        } catch (Exception e) {
            log.error("Error during OAuth2 authentication success handling", e);
            redirectToErrorPage(response, "Authentication processing failed");
        }
    }

    private String extractEmail(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            email = oAuth2User.getAttribute("login");
            if (email != null && !email.contains("@")) {
                email = null;
            }
        }
        return email;
    }

    private String extractName(OAuth2User oAuth2User) {
        String name = oAuth2User.getAttribute("name");
        if (name == null) {
            name = oAuth2User.getAttribute("login");
        }
        if (name == null) {
            String givenName = oAuth2User.getAttribute("given_name");
            String familyName = oAuth2User.getAttribute("family_name");
            if (givenName != null && familyName != null) {
                name = givenName + " " + familyName;
            } else if (givenName != null) {
                name = givenName;
            }
        }
        return name != null ? name : "OAuth2 User";
    }

    private String extractProvider(OAuth2User oAuth2User) {
        if (oAuth2User.getAttribute("avatar_url") != null &&
                oAuth2User.getAttribute("login") != null) {
            return "github";
        } else if (oAuth2User.getAttribute("picture") != null) {
            return "google";
        }
        return "unknown";
    }

    private User findOrCreateUser(String email, String name, String provider) {
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            log.info("Existing user found for email: {}", email);
            return existingUser.get();
        }

        log.info("Creating new user for OAuth2 email: {} from provider: {}", email, provider);

        Role contractorRole = roleRepository.findByName(Role.ERole.ROLE_CONTRACTOR)
                .orElseThrow(() -> new RuntimeException("Error: Role 'ROLE_CONTRACTOR' is not found."));

        Set<Role> roles = new HashSet<>();
        roles.add(contractorRole);

        String username = generateUsername(email, name);

        User newUser = User.builder()
                .username(username)
                .email(email)
                .password("")
                .roles(roles)
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("New OAuth2 user created with ID: {} and email: {}", savedUser.getId(), savedUser.getEmail());

        return savedUser;
    }

    private String generateUsername(String email, String name) {
        String baseUsername = name != null && !name.trim().isEmpty()
                ? name.trim().replaceAll("\\s+", "_").toLowerCase()
                : email.substring(0, email.indexOf("@"));

        String username = baseUsername;
        int counter = 1;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + "_" + counter;
            counter++;
        }
        if (username.length() > 20) {
            username = username.substring(0, 20);
        }
        if (username.length() < 3) {
            username = username + "_user";
        }

        return username;
    }

    private void redirectToErrorPage(HttpServletResponse response, String error) throws IOException {
        String errorUrl = UriComponentsBuilder.fromUriString(authorizedRedirectUri)
                .queryParam("error", error)
                .build().toUriString();

        response.sendRedirect(errorUrl);
    }
}