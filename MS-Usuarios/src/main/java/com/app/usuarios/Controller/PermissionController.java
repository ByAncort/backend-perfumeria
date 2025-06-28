package com.app.usuarios.Controller;

import com.app.usuarios.Dto.*;
import com.app.usuarios.Service.PermissionService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@Tag(name = "Permisos", description = "API para la gestión de permisos de usuarios")
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "Crear un nuevo permiso", description = "Endpoint para registrar un nuevo permiso en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permiso creado exitosamente",
                    content = @Content(schema = @Schema(implementation = PermissionDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    })
    @PostMapping("/create")
    public ResponseEntity<?> createPermission(
            @Parameter(description = "Datos del permiso a crear", required = true) @RequestBody PermissionDto dto) {

        ServiceResult<PermissionDto> result = permissionService.create(dto);

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }

        PermissionDto permission = result.getData();
        EntityModel<PermissionDto> resource = EntityModel.of(permission);
        resource.add(linkTo(methodOn(PermissionController.class).createPermission(dto)).withSelfRel());
        resource.add(linkTo(methodOn(PermissionController.class).updatePermission(permission.getId(), dto)).withRel("update"));
        resource.add(linkTo(methodOn(PermissionController.class).deletePermission(permission.getId())).withRel("delete"));
        resource.add(linkTo(methodOn(PermissionController.class).getAllPermissions()).withRel("all-permissions"));

        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Actualizar un permiso existente", description = "Endpoint para modificar los datos de un permiso existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permiso actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = PermissionDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class))),
            @ApiResponse(responseCode = "404", description = "Permiso no encontrado")
    })
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updatePermission(
            @Parameter(description = "ID del permiso a actualizar", required = true, example = "1") @PathVariable Long id,
            @Parameter(description = "Nuevos datos del permiso", required = true) @RequestBody PermissionDto dto) {

        ServiceResult<PermissionDto> result = permissionService.update(id, dto);

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }

        PermissionDto permission = result.getData();
        EntityModel<PermissionDto> resource = EntityModel.of(permission);
        resource.add(linkTo(methodOn(PermissionController.class).updatePermission(id, dto)).withSelfRel());
        resource.add(linkTo(methodOn(PermissionController.class).deletePermission(id)).withRel("delete"));
        resource.add(linkTo(methodOn(PermissionController.class).getAllPermissions()).withRel("all-permissions"));

        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Eliminar un permiso", description = "Endpoint para eliminar un permiso del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permiso eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Permiso no encontrado",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    })
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePermission(
            @Parameter(description = "ID del permiso a eliminar", required = true, example = "1") @PathVariable Long id) {

        ServiceResult<String> result = permissionService.delete(id);

        if (result.hasErrors()) {
            return ResponseEntity.status(404).body(result.getErrors());
        }

        EntityModel<String> resource = EntityModel.of(result.getData());
        resource.add(linkTo(methodOn(PermissionController.class).getAllPermissions()).withRel("all-permissions"));
        resource.add(linkTo(methodOn(PermissionController.class).createPermission(new PermissionDto())).withRel("create-permission"));

        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Obtener todos los permisos", description = "Endpoint para recuperar todos los permisos registrados en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de permisos obtenida exitosamente",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PermissionDto[].class)))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    })
    @GetMapping("/all")
    public ResponseEntity<?> getAllPermissions() {
        ServiceResult<List<PermissionDto>> result = permissionService.getAll();

        if (result.hasErrors()) {
            return ResponseEntity.status(500).body(result.getErrors());
        }

        List<EntityModel<PermissionDto>> permissions = result.getData().stream()
                .map(permission -> {
                    EntityModel<PermissionDto> resource = EntityModel.of(permission);
                    resource.add(linkTo(methodOn(PermissionController.class).getAllPermissions()).withSelfRel());
                    resource.add(linkTo(methodOn(PermissionController.class).updatePermission(permission.getId(), permission)).withRel("update"));
                    resource.add(linkTo(methodOn(PermissionController.class).deletePermission(permission.getId())).withRel("delete"));
                    return resource;
                })
                .collect(Collectors.toList());

        Link selfLink = linkTo(methodOn(PermissionController.class).getAllPermissions()).withSelfRel();
        Link createLink = linkTo(methodOn(PermissionController.class).createPermission(new PermissionDto())).withRel("create-permission");

        CollectionModel<EntityModel<PermissionDto>> resources = CollectionModel.of(permissions, selfLink, createLink);

        return ResponseEntity.ok(resources);
    }
}