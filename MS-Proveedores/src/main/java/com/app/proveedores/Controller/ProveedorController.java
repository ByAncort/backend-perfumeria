package com.app.proveedores.Controller;

import com.app.proveedores.Dto.ProveedorDto;
import com.app.proveedores.Dto.ServiceResult;
import com.app.proveedores.Models.Proveedor;
import com.app.proveedores.Service.ProveedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/ms-inventario/proveedor/")
@RequiredArgsConstructor
@Tag(name = "Gestión de Proveedores", description = "API para la administración de proveedores en el sistema de inventario")
public class ProveedorController {
    private final ProveedorService proveedorService;

    @Operation(
            summary = "Crear nuevo proveedor",
            description = "Registra un nuevo proveedor en el sistema",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Proveedor creado exitosamente",
                            content = @Content(schema = @Schema(implementation = Proveedor.class))),
                    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PostMapping("create-proveedor")
    public ResponseEntity<?> createProveedor(@RequestBody @Valid ProveedorDto proveedorDto) {
        ServiceResult<Proveedor> result = proveedorService.addProveedor(proveedorDto);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }

        Proveedor proveedor = result.getData();
        EntityModel<Proveedor> resource = EntityModel.of(proveedor);
        resource.add(linkTo(methodOn(ProveedorController.class).getProveedorById(proveedor.getId())).withSelfRel());
        resource.add(linkTo(methodOn(ProveedorController.class).updateProveedor(proveedor.getId(), proveedorDto)).withRel("update"));
        resource.add(linkTo(methodOn(ProveedorController.class).toggleActivoProveedor(proveedor.getId(), true)).withRel("toggle-status"));
        resource.add(linkTo(methodOn(ProveedorController.class).deleteProveedor(proveedor.getId())).withRel("delete"));
        resource.add(linkTo(methodOn(ProveedorController.class).getAllProveedoresActivos()).withRel(IanaLinkRelations.COLLECTION));

        return ResponseEntity
                .created(linkTo(methodOn(ProveedorController.class).getProveedorById(proveedor.getId())).toUri())
                .body(resource);
    }

    @Operation(
            summary = "Listar proveedores activos",
            description = "Obtiene todos los proveedores con estado activo en el sistema",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de proveedores activos",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Proveedor.class)))),
                    @ApiResponse(responseCode = "400", description = "Error al procesar la solicitud",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @GetMapping("all-active")
    public ResponseEntity<?> getAllProveedoresActivos() {
        ServiceResult<List<Proveedor>> result = proveedorService.getAllProveedoresActivos();
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }

        List<EntityModel<Proveedor>> proveedores = result.getData().stream()
                .map(proveedor -> {
                    EntityModel<Proveedor> resource = EntityModel.of(proveedor);
                    resource.add(linkTo(methodOn(ProveedorController.class).getProveedorById(proveedor.getId())).withSelfRel());
                    resource.add(linkTo(methodOn(ProveedorController.class).updateProveedor(proveedor.getId(), new ProveedorDto())).withRel("update"));
                    resource.add(linkTo(methodOn(ProveedorController.class).toggleActivoProveedor(proveedor.getId(), !proveedor.isActivo())).withRel("toggle-status"));
                    return resource;
                })
                .collect(Collectors.toList());

        Link selfLink = linkTo(methodOn(ProveedorController.class).getAllProveedoresActivos()).withSelfRel();
        Link createLink = linkTo(methodOn(ProveedorController.class).createProveedor(new ProveedorDto())).withRel("create-proveedor");

        CollectionModel<EntityModel<Proveedor>> resources = CollectionModel.of(proveedores, selfLink, createLink);
        return ResponseEntity.ok(resources);
    }

    @Operation(
            summary = "Obtener proveedor por ID",
            description = "Recupera un proveedor específico según su ID único",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Proveedor encontrado",
                            content = @Content(schema = @Schema(implementation = Proveedor.class))),
                    @ApiResponse(responseCode = "400", description = "Proveedor no encontrado o ID inválido",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @GetMapping("{id}")
    public ResponseEntity<?> getProveedorById(
            @Parameter(description = "ID único del proveedor", example = "1", required = true)
            @PathVariable Long id) {
        ServiceResult<Proveedor> result = proveedorService.getProveedorById(id);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }

        Proveedor proveedor = result.getData();
        EntityModel<Proveedor> resource = EntityModel.of(proveedor);
        resource.add(linkTo(methodOn(ProveedorController.class).getProveedorById(id)).withSelfRel());
        resource.add(linkTo(methodOn(ProveedorController.class).updateProveedor(id, new ProveedorDto())).withRel("update"));
        resource.add(linkTo(methodOn(ProveedorController.class).toggleActivoProveedor(id, !proveedor.isActivo())).withRel("toggle-status"));
        resource.add(linkTo(methodOn(ProveedorController.class).deleteProveedor(id)).withRel("delete"));
        resource.add(linkTo(methodOn(ProveedorController.class).getAllProveedoresActivos()).withRel("all-proveedores"));

        return ResponseEntity.ok(resource);
    }

    @Operation(
            summary = "Actualizar proveedor",
            description = "Actualiza la información de un proveedor existente",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Proveedor actualizado exitosamente",
                            content = @Content(schema = @Schema(implementation = Proveedor.class))),
                    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o proveedor no encontrado",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PutMapping("update/{id}")
    public ResponseEntity<?> updateProveedor(
            @Parameter(description = "ID único del proveedor", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody @Valid ProveedorDto dto) {
        ServiceResult<Proveedor> result = proveedorService.updateProveedor(id, dto);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }

        Proveedor proveedor = result.getData();
        EntityModel<Proveedor> resource = EntityModel.of(proveedor);
        resource.add(linkTo(methodOn(ProveedorController.class).getProveedorById(id)).withSelfRel());
        resource.add(linkTo(methodOn(ProveedorController.class).updateProveedor(id, dto)).withRel("update"));
        resource.add(linkTo(methodOn(ProveedorController.class).toggleActivoProveedor(id, !proveedor.isActivo())).withRel("toggle-status"));
        resource.add(linkTo(methodOn(ProveedorController.class).deleteProveedor(id)).withRel("delete"));
        resource.add(linkTo(methodOn(ProveedorController.class).getAllProveedoresActivos()).withRel("all-proveedores"));

        return ResponseEntity.ok(resource);
    }

    @Operation(
            summary = "Cambiar estado de proveedor",
            description = "Activa o desactiva un proveedor en el sistema",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Estado del proveedor actualizado",
                            content = @Content(schema = @Schema(implementation = Proveedor.class))),
                    @ApiResponse(responseCode = "400", description = "Error al cambiar el estado",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PatchMapping("toggle-activo/{id}")
    public ResponseEntity<?> toggleActivoProveedor(
            @Parameter(description = "ID único del proveedor", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nuevo estado del proveedor", example = "true", required = true)
            @RequestParam boolean activo) {
        ServiceResult<Proveedor> result = proveedorService.toggleActivoProveedor(id, activo);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }

        Proveedor proveedor = result.getData();
        EntityModel<Proveedor> resource = EntityModel.of(proveedor);
        resource.add(linkTo(methodOn(ProveedorController.class).getProveedorById(id)).withSelfRel());
        resource.add(linkTo(methodOn(ProveedorController.class).updateProveedor(id, new ProveedorDto())).withRel("update"));
        resource.add(linkTo(methodOn(ProveedorController.class).toggleActivoProveedor(id, !proveedor.isActivo())).withRel("toggle-status"));
        resource.add(linkTo(methodOn(ProveedorController.class).deleteProveedor(id)).withRel("delete"));
        resource.add(linkTo(methodOn(ProveedorController.class).getAllProveedoresActivos()).withRel("all-proveedores"));

        return ResponseEntity.ok(resource);
    }

    @Operation(
            summary = "Buscar proveedor por RUT",
            description = "Obtiene un proveedor según su RUT (identificador tributario)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Proveedor encontrado",
                            content = @Content(schema = @Schema(implementation = Proveedor.class))),
                    @ApiResponse(responseCode = "400", description = "Proveedor no encontrado o RUT inválido",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @GetMapping("by-rut/{rut}")
    public ResponseEntity<?> getProveedorByRut(
            @Parameter(description = "RUT del proveedor", example = "12345678-9", required = true)
            @PathVariable String rut) {
        ServiceResult<Proveedor> result = proveedorService.getProveedorByRut(rut);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }

        Proveedor proveedor = result.getData();
        EntityModel<Proveedor> resource = EntityModel.of(proveedor);
        resource.add(linkTo(methodOn(ProveedorController.class).getProveedorByRut(rut)).withSelfRel());
        resource.add(linkTo(methodOn(ProveedorController.class).getProveedorById(proveedor.getId())).withRel("by-id"));
        resource.add(linkTo(methodOn(ProveedorController.class).updateProveedor(proveedor.getId(), new ProveedorDto())).withRel("update"));
        resource.add(linkTo(methodOn(ProveedorController.class).getAllProveedoresActivos()).withRel("all-proveedores"));

        return ResponseEntity.ok(resource);
    }

    @Operation(
            summary = "Eliminar proveedor",
            description = "Elimina un proveedor del sistema (eliminación lógica)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Proveedor eliminado exitosamente",
                            content = @Content(schema = @Schema(implementation = Proveedor.class))),
                    @ApiResponse(responseCode = "400", description = "Error al eliminar el proveedor",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deleteProveedor(
            @Parameter(description = "ID único del proveedor", example = "1", required = true)
            @PathVariable Long id) {
        ServiceResult<Proveedor> result = proveedorService.deleteProveedor(id);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }

        Proveedor proveedor = result.getData();
        EntityModel<Proveedor> resource = EntityModel.of(proveedor);
        resource.add(linkTo(methodOn(ProveedorController.class).getAllProveedoresActivos()).withRel("all-proveedores"));
        resource.add(linkTo(methodOn(ProveedorController.class).createProveedor(new ProveedorDto())).withRel("create-proveedor"));

        return ResponseEntity.ok(resource);
    }
}