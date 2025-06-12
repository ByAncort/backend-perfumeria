package com.app.usuarios.Controller;

import com.app.usuarios.Dto.PermissionDto;
import com.app.usuarios.Dto.ServiceResult;
import com.app.usuarios.Service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@Tag(name = "Permisos", description = "API para la gestión de permisos de usuarios")
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(
            summary = "Crear un nuevo permiso",
            description = "Endpoint para registrar un nuevo permiso en el sistema",
            operationId = "createPermission"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permiso creado exitosamente",
                    content = @Content(schema = @Schema(implementation = PermissionDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    })
    @PostMapping("/create")
    public ResponseEntity<?> createPermission(
            @Parameter(description = "Datos del permiso a crear", required = true,
                    content = @Content(schema = @Schema(implementation = PermissionDto.class)))
            @RequestBody PermissionDto dto) {
        ServiceResult<?> result = permissionService.create(dto);
        return result.hasErrors()
                ? ResponseEntity.badRequest().body(result.getErrors())
                : ResponseEntity.ok(result.getData());
    }

    @Operation(
            summary = "Actualizar un permiso existente",
            description = "Endpoint para modificar los datos de un permiso existente",
            operationId = "updatePermission"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permiso actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = PermissionDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class))),
            @ApiResponse(responseCode = "404", description = "Permiso no encontrado")
    })
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updatePermission(
            @Parameter(description = "ID del permiso a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Nuevos datos del permiso", required = true,
                    content = @Content(schema = @Schema(implementation = PermissionDto.class)))
            @RequestBody PermissionDto dto) {
        ServiceResult<?> result = permissionService.update(id, dto);
        return result.hasErrors()
                ? ResponseEntity.badRequest().body(result.getErrors())
                : ResponseEntity.ok(result.getData());
    }

    @Operation(
            summary = "Eliminar un permiso",
            description = "Endpoint para eliminar un permiso del sistema",
            operationId = "deletePermission"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permiso eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Permiso no encontrado",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    })
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePermission(
            @Parameter(description = "ID del permiso a eliminar", required = true, example = "1")
            @PathVariable Long id) {
        ServiceResult<?> result = permissionService.delete(id);
        return result.hasErrors()
                ? ResponseEntity.status(404).body(result.getErrors())
                : ResponseEntity.ok(result.getData());
    }

    @Operation(
            summary = "Obtener todos los permisos",
            description = "Endpoint para recuperar todos los permisos registrados en el sistema",
            operationId = "getAllPermissions"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de permisos obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = PermissionDto[].class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    })
    @GetMapping("/all")
    public ResponseEntity<?> getAllPermissions() {
        ServiceResult<?> result = permissionService.getAll();
        return result.hasErrors()
                ? ResponseEntity.status(500).body(result.getErrors())
                : ResponseEntity.ok(result.getData());
    }
}