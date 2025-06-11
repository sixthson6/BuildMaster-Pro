package com.tech.security.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role implements GrantedAuthority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, unique = true, nullable = false)
    private ERole name;

    @Override
    public String getAuthority() {
        return name.name();
    }

    public enum ERole {
        ROLE_USER,
        ROLE_CONTRACTOR,
        ROLE_DEVELOPER,
        ROLE_MANAGER,
        ROLE_ADMIN,
        ROLE_MODERATOR
    }
}