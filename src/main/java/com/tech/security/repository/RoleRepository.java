package com.tech.security.repository;

import com.tech.security.model.Role.ERole;
import com.tech.security.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.lang.ScopedValue;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(Role.ERole name);

//    <T> ScopedValue<T> findByName(Role.ERole eRole);
}
