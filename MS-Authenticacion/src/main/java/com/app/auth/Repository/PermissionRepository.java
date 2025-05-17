package com.app.auth.Repository;

import com.app.auth.Models.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    boolean existsByName(String roleName);

    Optional<Permission> findByName(String name);

    void delete(Permission permission);

}
