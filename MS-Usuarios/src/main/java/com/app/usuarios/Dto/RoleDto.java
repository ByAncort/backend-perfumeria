package com.app.usuarios.Dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class RoleDto {
    private Long id;
    private String name;
    private Set<PermissionDto> permissions;

}
