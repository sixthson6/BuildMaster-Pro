package com.tech.controller;

import com.tech.security.payload.response.JwtResponse; // Corrected import
import com.tech.security.payload.request.LoginRequest; // Corrected import
import com.tech.security.payload.request.SignupRequest; // Corrected import
import com.tech.security.jwt.JwtUtils;
import com.tech.security.model.Role;
import com.tech.security.model.Role.ERole;
import com.tech.security.model.User;
import com.tech.security.repository.RoleRepository;
import com.tech.security.repository.UserRepository;
import com.tech.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final AuditLogService auditLogService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        User userDetails = (User) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        auditLogService.logLoginAction("LOGIN_SUCCESS", loginRequest.getEmail(), "Traditional Login", "Success");

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@Valid @RequestBody SignupRequest signUpRequest) { // Changed return type to String
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Username is already taken!"); // Adjusted message
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Email is already in use!"); // Adjusted message
        }

        User user = User.builder()
                .username(signUpRequest.getUsername())
                .email(signUpRequest.getEmail())
                .password(encoder.encode(signUpRequest.getPassword()))
                .build();

        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: ROLE_USER is not found. Please initialize roles via DataLoader."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: ROLE_ADMIN is not found. Please initialize roles via DataLoader."));
                        roles.add(adminRole);
                        break;
                    case "manager":
                        Role managerRole = roleRepository.findByName(ERole.ROLE_MANAGER)
                                .orElseThrow(() -> new RuntimeException("Error: ROLE_MANAGER is not found. Please initialize roles via DataLoader."));
                        roles.add(managerRole);
                        break;
                    case "dev":
                        Role devRole = roleRepository.findByName(ERole.ROLE_DEVELOPER)
                                .orElseThrow(() -> new RuntimeException("Error: ROLE_DEVELOPER is not found. Please initialize roles via DataLoader."));
                        roles.add(devRole);
                        break;
                    case "contractor":
                        Role contractorRole = roleRepository.findByName(ERole.ROLE_CONTRACTOR)
                                .orElseThrow(() -> new RuntimeException("Error: ROLE_CONTRACTOR is not found. Please initialize roles via DataLoader."));
                        roles.add(contractorRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: ROLE_USER is not found. Please initialize roles via DataLoader."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        auditLogService.logLoginAction("REGISTER_SUCCESS", signUpRequest.getEmail(), "Traditional Registration", "Success");


        return ResponseEntity.ok("User registered successfully!"); // Adjusted message
    }
}
