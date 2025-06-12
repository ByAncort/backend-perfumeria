package com.app.usuarios.Controller;

import com.app.usuarios.Dto.*;
import com.app.usuarios.Model.Role;
import com.app.usuarios.Service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/roles")
@Tag(name = "Gestión de Roles", description = "API para la administración de roles y permisos")
public class RoleController {
    private final RoleService roleService;

    @Operation(
            summary = "Crear un nuevo rol",
            description = "Endpoint para registrar un nuevo rol en el sistema",
            operationId = "createRole"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Rol creado exitosamente",
                    content = @Content(schema = @Schema(implementation = Role.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class))
            )
    })
    @PostMapping("create")
    public ResponseEntity<?> createRole(
            @Parameter(description = "Datos del rol a crear", required = true,
                    content = @Content(schema = @Schema(implementation = RoleDto.class)))
            @RequestBody RoleDto roleDto) {
        ServiceResult<Role> result = roleService.create(roleDto);

        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result.getErrors());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(result.getData());
    }

    @Operation(
            summary = "Eliminar un rol",
            description = "Endpoint para eliminar un rol existente por su ID",
            operationId = "deleteRole"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Rol eliminado exitosamente",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Rol no encontrado",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class))
            )
    })
    @DeleteMapping("delete-role/{id}")
    public ResponseEntity<?> deleteRole(
            @Parameter(description = "ID del rol a eliminar", required = true, example = "1")
            @PathVariable Long id) {
        ServiceResult<String> result = roleService.deleteById(id);

        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result.getErrors());
        }

        return ResponseEntity.ok(result.getData());
    }

    @Operation(
            summary = "Asignar permiso a rol",
            description = "Endpoint para asignar un permiso existente a un rol específico",
            operationId = "assignPermissionToRole"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Permiso asignado exitosamente",
                    content = @Content(schema = @Schema(implementation = Role.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error en la asignación",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class))
            )
    })
    @PutMapping("/{RoleName}/assign-permission/{permissionName}")
    public ResponseEntity<?> assignPermission(
            @Parameter(description = "Nombre del rol", required = true, example = "ADMIN")
            @PathVariable String RoleName,
            @Parameter(description = "Nombre del permiso", required = true, example = "WRITE")
            @PathVariable String permissionName) {
        ServiceResult<Role> result = roleService.assignPermissionToRole(RoleName, permissionName);

        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result.getErrors());
        }

        return ResponseEntity.ok(result.getData());
    }

    @Operation(
            summary = "Remover permiso de rol",
            description = "Endpoint para remover un permiso de un rol específico",
            operationId = "removePermissionFromRole"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Permiso removido exitosamente",
                    content = @Content(schema = @Schema(implementation = Role.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error al remover el permiso",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class))
            )
    })
    @PutMapping("/{RoleName}/remove-permission/{permissionName}")
    public ResponseEntity<?> removePermission(
            @Parameter(description = "Nombre del rol", required = true, example = "ADMIN")
            @PathVariable String RoleName,
            @Parameter(description = "Nombre del permiso", required = true, example = "WRITE")
            @PathVariable String permissionName) {
        ServiceResult<Role> result = roleService.removePermissionFromRole(RoleName, permissionName);

        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result.getErrors());
        }

        return ResponseEntity.ok(result.getData());
    }

    @Operation(
            summary = "Obtener todos los roles",
            description = "Endpoint para recuperar todos los roles registrados en el sistema",
            operationId = "getAllRoles"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de roles obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = Role[].class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class))
            )
    })
    @GetMapping("/all")
    public ResponseEntity<?> getAllRoles() {
        ServiceResult<?> result = roleService.getAllRoles();

        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result.getErrors());
        }

        return ResponseEntity.ok(result.getData());
    }
}