package com.app.auth.Repository;

import com.app.auth.Models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Long> {
    boolean existsByName(String roleName);

    Optional<Role> findByName(String role_user);
}
