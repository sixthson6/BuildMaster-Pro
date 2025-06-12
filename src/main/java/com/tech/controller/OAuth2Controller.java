package com.tech.controller;

import com.tech.security.jwt.JwtUtils;
import com.tech.security.payload.response.JwtResponse;
import com.tech.security.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/oauth2")
@RequiredArgsConstructor
@Slf4j
public class OAuth2Controller {

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.badRequest().body("User not authenticated");
        }

        try {
            String email;
            if (authentication.getPrincipal() instanceof OAuth2User) {
                OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                email = oAuth2User.getAttribute("email");
                if (email == null) {
                    email = oAuth2User.getAttribute("login"); // GitHub fallback
                }
            } else if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                email = userDetails.getUsername();
            } else {
                return ResponseEntity.badRequest().body("Invalid authentication principal");
            }

            if (email == null) {
                return ResponseEntity.badRequest().body("Email not found in authentication");
            }

            // Load user details and generate JWT
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // Create authentication token for JWT generation
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken authToken =
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

            String jwtToken = jwtUtils.generateJwtToken(authToken);

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            com.tech.security.model.User user = (com.tech.security.model.User) userDetails;

            return ResponseEntity.ok(new JwtResponse(
                    jwtToken,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    roles
            ));

        } catch (Exception e) {
            log.error("Error processing OAuth2 user", e);
            return ResponseEntity.badRequest().body("Error processing OAuth2 authentication: " + e.getMessage());
        }
    }

}