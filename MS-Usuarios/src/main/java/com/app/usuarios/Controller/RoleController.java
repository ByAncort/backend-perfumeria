package com.app.usuarios.Controller;

import com.app.usuarios.Dto.*;
import com.app.usuarios.Model.Role;
import com.app.usuarios.Service.RoleService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/roles")
@Tag(name = "Gestión de Roles", description = "API para la administración de roles y permisos")
public class RoleController {
    private final RoleService roleService;

    @Operation(summary = "Crear un nuevo rol", description = "Endpoint para registrar un nuevo rol en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Rol creado exitosamente",
                    content = @Content(schema = @Schema(implementation = Role.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    })
    @PostMapping("create")
    public ResponseEntity<?> createRole(
            @Parameter(description = "Datos del rol a crear", required = true) @RequestBody RoleDto roleDto) {

        ServiceResult<Role> result = roleService.create(roleDto);

        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result.getErrors());
        }

        Role role = result.getData();
        EntityModel<Role> resource = EntityModel.of(role);
        resource.add(linkTo(methodOn(RoleController.class).createRole(roleDto)).withSelfRel());
        resource.add(linkTo(methodOn(RoleController.class).deleteRole(role.getId())).withRel("delete"));
        resource.add(linkTo(methodOn(RoleController.class).getAllRoles()).withRel("all-roles"));
        resource.add(linkTo(methodOn(RoleController.class)
                .assignPermission(role.getName(), "permissionName")).withRel("assign-permission"));

        return ResponseEntity
                .created(linkTo(methodOn(RoleController.class).getAllRoles()).toUri())
                .body(resource);
    }

    @Operation(summary = "Eliminar un rol", description = "Endpoint para eliminar un rol existente por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rol eliminado exitosamente",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Rol no encontrado",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    })
    @DeleteMapping("delete-role/{id}")
    public ResponseEntity<?> deleteRole(
            @Parameter(description = "ID del rol a eliminar", required = true, example = "1") @PathVariable Long id) {

        ServiceResult<String> result = roleService.deleteById(id);

        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result.getErrors());
        }

        EntityModel<String> resource = EntityModel.of(result.getData());
        resource.add(linkTo(methodOn(RoleController.class).getAllRoles()).withRel("all-roles"));
        resource.add(linkTo(methodOn(RoleController.class).createRole(new RoleDto())).withRel("create-role"));

        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Asignar permiso a rol", description = "Endpoint para asignar un permiso existente a un rol específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permiso asignado exitosamente",
                    content = @Content(schema = @Schema(implementation = Role.class))),
            @ApiResponse(responseCode = "400", description = "Error en la asignación",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    })
    @PutMapping("/{RoleName}/assign-permission/{permissionName}")
    public ResponseEntity<?> assignPermission(
            @Parameter(description = "Nombre del rol", required = true, example = "ADMIN") @PathVariable String RoleName,
            @Parameter(description = "Nombre del permiso", required = true, example = "WRITE") @PathVariable String permissionName) {

        ServiceResult<Role> result = roleService.assignPermissionToRole(RoleName, permissionName);

        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result.getErrors());
        }

        Role role = result.getData();
        EntityModel<Role> resource = EntityModel.of(role);
        resource.add(linkTo(methodOn(RoleController.class)
                .assignPermission(RoleName, permissionName)).withSelfRel());
        resource.add(linkTo(methodOn(RoleController.class)
                .removePermission(RoleName, permissionName)).withRel("remove-permission"));
        resource.add(linkTo(methodOn(RoleController.class).getAllRoles()).withRel("all-roles"));

        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Remover permiso de rol", description = "Endpoint para remover un permiso de un rol específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permiso removido exitosamente",
                    content = @Content(schema = @Schema(implementation = Role.class))),
            @ApiResponse(responseCode = "400", description = "Error al remover el permiso",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    })
    @PutMapping("/{RoleName}/remove-permission/{permissionName}")
    public ResponseEntity<?> removePermission(
            @Parameter(description = "Nombre del rol", required = true, example = "ADMIN") @PathVariable String RoleName,
            @Parameter(description = "Nombre del permiso", required = true, example = "WRITE") @PathVariable String permissionName) {

        ServiceResult<Role> result = roleService.removePermissionFromRole(RoleName, permissionName);

        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result.getErrors());
        }

        Role role = result.getData();
        EntityModel<Role> resource = EntityModel.of(role);
        resource.add(linkTo(methodOn(RoleController.class)
                .removePermission(RoleName, permissionName)).withSelfRel());
        resource.add(linkTo(methodOn(RoleController.class)
                .assignPermission(RoleName, permissionName)).withRel("assign-permission"));
        resource.add(linkTo(methodOn(RoleController.class).getAllRoles()).withRel("all-roles"));

        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Obtener todos los roles", description = "Endpoint para recuperar todos los roles registrados en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de roles obtenida exitosamente",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Role.class)))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    })
    @GetMapping("/all")
    public ResponseEntity<?> getAllRoles() {
        ServiceResult<List<RoleDto>> result = roleService.getAllRoles();

        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result.getErrors());
        }

        List<EntityModel<RoleDto>> roles = result.getData().stream()
                .map(role -> {
                    EntityModel<RoleDto> resource = EntityModel.of(role);
                    resource.add(linkTo(methodOn(RoleController.class).getAllRoles()).withSelfRel());
                    resource.add(linkTo(methodOn(RoleController.class).deleteRole(role.getId())).withRel("delete"));
                    resource.add(linkTo(methodOn(RoleController.class)
                            .assignPermission(role.getName(), "permissionName")).withRel("assign-permission"));
                    return resource;
                })
                .collect(Collectors.toList());

        Link selfLink = linkTo(methodOn(RoleController.class).getAllRoles()).withSelfRel();
        Link createLink = linkTo(methodOn(RoleController.class).createRole(new RoleDto())).withRel("create-role");

        CollectionModel<EntityModel<RoleDto>> resources = CollectionModel.of(roles, selfLink, createLink);

        return ResponseEntity.ok(resources);
    }

}