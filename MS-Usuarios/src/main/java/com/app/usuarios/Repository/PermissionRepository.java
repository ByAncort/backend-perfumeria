package com.app.usuarios.Repository;

import com.app.usuarios.Model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    boolean existsByName(String roleName);
    Optional<Permission> findByName(String name);
}
