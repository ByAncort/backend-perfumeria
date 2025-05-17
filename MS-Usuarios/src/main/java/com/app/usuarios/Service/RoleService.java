package com.app.usuarios.Service;

import com.app.usuarios.Dto.PermissionDto;
import com.app.usuarios.Dto.RoleDto;
import com.app.usuarios.Dto.ServiceResult;
import com.app.usuarios.Model.Permission;
import com.app.usuarios.Model.Role;
import com.app.usuarios.Repository.PermissionRepository;
import com.app.usuarios.Repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public ServiceResult<Role> create(RoleDto request) {
        try {
            Optional<Role> find = roleRepository.findByName(request.getName());
            if(find.isPresent()){
                new Exception("El rol ya esta Registrado");
            }

            Set<Permission> permissions = request.getPermissions().stream()
                    .map(dto -> permissionRepository.findByName(dto.getName())
                            .orElseThrow(() -> new RuntimeException("Permission not found: " + dto.getName())))
                    .collect(Collectors.toSet());

            Role create = Role.builder()
                    .name(request.getName())
                    .permissions(permissions)
                    .build();

            Role saved = roleRepository.save(create);
            return new ServiceResult<>(saved);

        } catch (Exception e) {
            return new ServiceResult<>(List.of("An error occurred while creating the role: " + e.getMessage()));
        }
    }
    public ServiceResult<String> deleteById(Long id) {
        try {
            Optional<Role> role = roleRepository.findById(id);
            if (role.isEmpty()) {
                return new ServiceResult<>(List.of("Role not found with ID: " + id));
            }

            roleRepository.deleteById(id);
            return new ServiceResult<>("Role deleted successfully.");
        } catch (Exception e) {
            return new ServiceResult<>(List.of("Error deleting role: " + e.getMessage()));
        }
    }
    public ServiceResult<Role> assignPermissionToRole(String RoleName, String permissionName) {
        try {
            Role role = roleRepository.findByName(RoleName)
                    .orElseThrow(() -> new RuntimeException("Role not found with Name: " + RoleName));

            Permission permission = permissionRepository.findByName(permissionName)
                    .orElseThrow(() -> new RuntimeException("Permission not found: " + permissionName));

            role.getPermissions().add(permission);
            Role updated = roleRepository.save(role);
            return new ServiceResult<>(updated);
        } catch (Exception e) {
            return new ServiceResult<>(List.of("Error assigning permission: " + e.getMessage()));
        }
    }

    public ServiceResult<Role> removePermissionFromRole(String roleName, String permissionName) {
        try {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Role not found with Name: " + roleName));

            Permission permission = permissionRepository.findByName(permissionName)
                    .orElseThrow(() -> new RuntimeException("Permission not found: " + permissionName));

            role.getPermissions().remove(permission);
            Role updated = roleRepository.save(role);
            return new ServiceResult<>(updated);
        } catch (Exception e) {
            return new ServiceResult<>(List.of("Error removing permission: " + e.getMessage()));
        }
    }

    public ServiceResult<List<RoleDto>> getAllRoles() {
        try {
            List<RoleDto> roles = roleRepository.findAll().stream()
                    .map(role -> RoleDto.builder()
                            .id(role.getId())
                            .name(role.getName())
                            .permissions(
                                    role.getPermissions().stream()
                                            .map(permission -> PermissionDto.builder()
                                                    .name(permission.getName())
                                                    .build())
                                            .collect(Collectors.toSet())
                            )
                            .build())
                    .collect(Collectors.toList());

            return new ServiceResult<>(roles);
        } catch (Exception e) {
            return new ServiceResult<>(List.of("Error retrieving roles: " + e.getMessage()));
        }
    }


}
