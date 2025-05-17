package com.app.usuarios.Service;

import com.app.usuarios.Dto.PermissionDto;
import com.app.usuarios.Dto.ServiceResult;
import com.app.usuarios.Model.Permission;
import com.app.usuarios.Repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public ServiceResult<PermissionDto> create(PermissionDto dto) {
        try {
            Permission permission = Permission.builder()
                    .name(dto.getName())
                    .build();

            Permission saved = permissionRepository.save(permission);
            return new ServiceResult<>(toDto(saved));
        } catch (Exception e) {
            return new ServiceResult<>(List.of("Error creating permission: " + e.getMessage()));
        }
    }

    public ServiceResult<PermissionDto> update(Long id, PermissionDto dto) {
        try {
            Permission existing = permissionRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Permission not found with ID: " + id));

            existing.setName(dto.getName());
            Permission updated = permissionRepository.save(existing);

            return new ServiceResult<>(toDto(updated));
        } catch (Exception e) {
            return new ServiceResult<>(List.of("Error updating permission: " + e.getMessage()));
        }
    }

    public ServiceResult<String> delete(Long id) {
        try {
            if (!permissionRepository.existsById(id)) {
                return new ServiceResult<>(List.of("Permission not found with ID: " + id));
            }

            permissionRepository.deleteById(id);
            return new ServiceResult<>("Permission deleted successfully.");
        } catch (Exception e) {
            return new ServiceResult<>(List.of("Error deleting permission: " + e.getMessage()));
        }
    }

    public ServiceResult<List<PermissionDto>> getAll() {
        try {
            List<PermissionDto> permissions = permissionRepository.findAll().stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());

            return new ServiceResult<>(permissions);
        } catch (Exception e) {
            return new ServiceResult<>(List.of("Error retrieving permissions: " + e.getMessage()));
        }
    }

    private PermissionDto toDto(Permission permission) {
        return PermissionDto.builder()
                .name(permission.getName())
                .build();
    }
}