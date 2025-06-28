package com.app.sucursales.Controller;

import com.app.sucursales.Dto.ServiceResult;
import com.app.sucursales.Dto.SucursalDto;
import com.app.sucursales.Service.SucursalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/sucursales")
@RequiredArgsConstructor
@Tag(name = "Sucursales", description = "API para la gestión de sucursales")
public class SucursalController {

    private final SucursalService sucursalService;

    @Operation(
            summary = "Crear una nueva sucursal",
            description = "Endpoint para registrar una nueva sucursal en el sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sucursal creada exitosamente",
                    content = @Content(schema = @Schema(implementation = SucursalDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = List.class)))
    })
    @PostMapping
    public ResponseEntity<?> crearSucursal(
            @Parameter(description = "Datos de la sucursal a crear", required = true)
            @RequestBody SucursalDto dto) {
        ServiceResult<SucursalDto> result = sucursalService.crearSucursal(dto);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }

        SucursalDto sucursal = result.getData();
        EntityModel<SucursalDto> resource = EntityModel.of(sucursal);

        // Self link
        resource.add(linkTo(methodOn(SucursalController.class).crearSucursal(dto)).withSelfRel());
        // Links relacionados
        resource.add(linkTo(methodOn(SucursalController.class).obtenerSucursal(sucursal.getId())).withRel("self"));
        resource.add(linkTo(methodOn(SucursalController.class).actualizarSucursal(sucursal.getId(), dto)).withRel("update"));
        resource.add(linkTo(methodOn(SucursalController.class).cambiarEstadoSucursal(sucursal.getId(), true)).withRel("toggle-status"));
        resource.add(linkTo(methodOn(SucursalController.class).listarSucursales()).withRel("all-sucursales"));
        resource.add(linkTo(methodOn(SucursalController.class).buscarSucursalesActivas()).withRel("active-sucursales"));

        return ResponseEntity.ok(resource);
    }

    @Operation(
            summary = "Obtener una sucursal por ID",
            description = "Endpoint para recuperar los detalles de una sucursal específica"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sucursal encontrada",
                    content = @Content(schema = @Schema(implementation = SucursalDto.class))),
            @ApiResponse(responseCode = "404", description = "Sucursal no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerSucursal(
            @Parameter(description = "ID de la sucursal a buscar", required = true)
            @PathVariable Long id) {
        ServiceResult<SucursalDto> result = sucursalService.obtenerSucursalPorId(id);
        if (result.hasErrors()) {
            return ResponseEntity.notFound().build();
        }

        SucursalDto sucursal = result.getData();
        EntityModel<SucursalDto> resource = EntityModel.of(sucursal);

        resource.add(linkTo(methodOn(SucursalController.class).obtenerSucursal(id)).withSelfRel());
        resource.add(linkTo(methodOn(SucursalController.class).actualizarSucursal(id, sucursal)).withRel("update"));
        resource.add(linkTo(methodOn(SucursalController.class).cambiarEstadoSucursal(id, !sucursal.getActiva())).withRel("toggle-status"));
        resource.add(linkTo(methodOn(SucursalController.class).listarSucursales()).withRel("all-sucursales"));
        resource.add(linkTo(methodOn(SucursalController.class).buscarSucursalesActivas()).withRel("active-sucursales"));

        return ResponseEntity.ok(resource);
    }

    @Operation(
            summary = "Listar todas las sucursales",
            description = "Endpoint para obtener un listado completo de todas las sucursales"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de sucursales obtenido",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = List.class)))
    })
    @GetMapping
    public ResponseEntity<?> listarSucursales() {
        ServiceResult<List<SucursalDto>> result = sucursalService.listarTodasLasSucursales();
        if (result.hasErrors()) {
            return ResponseEntity.internalServerError().body(result.getErrors());
        }

        List<EntityModel<SucursalDto>> sucursales = result.getData().stream()
                .map(sucursal -> {
                    EntityModel<SucursalDto> resource = EntityModel.of(sucursal);
                    resource.add(linkTo(methodOn(SucursalController.class).obtenerSucursal(sucursal.getId())).withRel("self"));
                    resource.add(linkTo(methodOn(SucursalController.class).actualizarSucursal(sucursal.getId(), sucursal)).withRel("update"));
                    resource.add(linkTo(methodOn(SucursalController.class).cambiarEstadoSucursal(sucursal.getId(), !sucursal.getActiva())).withRel("toggle-status"));
                    return resource;
                })
                .collect(Collectors.toList());

        Link selfLink = linkTo(methodOn(SucursalController.class).listarSucursales()).withSelfRel();
        Link createLink = linkTo(methodOn(SucursalController.class).crearSucursal(new SucursalDto())).withRel("create-sucursal");
        Link activeLink = linkTo(methodOn(SucursalController.class).buscarSucursalesActivas()).withRel("active-sucursales");

        CollectionModel<EntityModel<SucursalDto>> resources = CollectionModel.of(sucursales, selfLink, createLink, activeLink);

        return ResponseEntity.ok(resources);
    }

    @Operation(
            summary = "Actualizar una sucursal",
            description = "Endpoint para modificar los datos de una sucursal existente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sucursal actualizada exitosamente",
                    content = @Content(schema = @Schema(implementation = SucursalDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "404", description = "Sucursal no encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarSucursal(
            @Parameter(description = "ID de la sucursal a actualizar", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nuevos datos de la sucursal", required = true)
            @RequestBody SucursalDto dto) {
        ServiceResult<SucursalDto> result = sucursalService.actualizarSucursal(id, dto);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }

        SucursalDto sucursal = result.getData();
        EntityModel<SucursalDto> resource = EntityModel.of(sucursal);

        resource.add(linkTo(methodOn(SucursalController.class).actualizarSucursal(id, dto)).withSelfRel());
        resource.add(linkTo(methodOn(SucursalController.class).obtenerSucursal(id)).withRel("self"));
        resource.add(linkTo(methodOn(SucursalController.class).cambiarEstadoSucursal(id, sucursal.getActiva())).withRel("toggle-status"));
        resource.add(linkTo(methodOn(SucursalController.class).listarSucursales()).withRel("all-sucursales"));

        return ResponseEntity.ok(resource);
    }

    @Operation(
            summary = "Cambiar estado de una sucursal",
            description = "Endpoint para activar o desactivar una sucursal"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Estado cambiado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida",
                    content = @Content(schema = @Schema(implementation = List.class)))
    })
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstadoSucursal(
            @Parameter(description = "ID de la sucursal a modificar", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nuevo estado de la sucursal (true=activa, false=inactiva)", required = true)
            @RequestParam boolean activa) {
        ServiceResult<SucursalDto> result = sucursalService.cambiarEstadoSucursal(id, activa);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Listar sucursales activas",
            description = "Endpoint para obtener un listado de todas las sucursales activas"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de sucursales activas obtenido",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = List.class)))
    })
    @GetMapping("/activas")
    public ResponseEntity<?> buscarSucursalesActivas() {
        ServiceResult<List<SucursalDto>> result = sucursalService.buscarSucursalesActivas();
        if (result.hasErrors()) {
            return ResponseEntity.internalServerError().body(result.getErrors());
        }

        List<EntityModel<SucursalDto>> sucursales = result.getData().stream()
                .map(sucursal -> {
                    EntityModel<SucursalDto> resource = EntityModel.of(sucursal);
                    resource.add(linkTo(methodOn(SucursalController.class).obtenerSucursal(sucursal.getId())).withRel("self"));
                    resource.add(linkTo(methodOn(SucursalController.class).cambiarEstadoSucursal(sucursal.getId(), false)).withRel("deactivate"));
                    return resource;
                })
                .collect(Collectors.toList());

        Link selfLink = linkTo(methodOn(SucursalController.class).buscarSucursalesActivas()).withSelfRel();
        Link allLink = linkTo(methodOn(SucursalController.class).listarSucursales()).withRel("all-sucursales");

        CollectionModel<EntityModel<SucursalDto>> resources = CollectionModel.of(sucursales, selfLink, allLink);

        return ResponseEntity.ok(resources);
    }
}