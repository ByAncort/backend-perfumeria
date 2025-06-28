package com.app.inventario.Controller;

import com.app.inventario.Dto.*;
import com.app.inventario.Service.InventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.app.dto.ServiceResult;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
@Tag(name = "Inventario", description = "API para gestión de inventario")
public class InventarioController {

    private final InventarioService inventarioService;

    @Operation(
            summary = "Registrar nuevo inventario",
            description = "Crea un nuevo registro de inventario para un producto en una sucursal"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Inventario registrado exitosamente",
                    content = @Content(schema = @Schema(implementation = InventarioResponse.class))
            )
    })
    @PostMapping
    public ResponseEntity<EntityModel<InventarioResponse>> registrarInventario(
            @RequestBody InventarioRequest request) {
        InventarioResponse response = inventarioService.registrarInventario(request);

        EntityModel<InventarioResponse> resource = EntityModel.of(response);
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(InventarioController.class).registrarInventario(request)).withSelfRel());
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(InventarioController.class).obtenerInventario(response.getId())).withRel("self"));
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(InventarioController.class).actualizarStock(response.getId(), 0)).withRel("update-stock"));
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(InventarioController.class).obtenerPorSucursal(response.getSucursal().getId())).withRel("by-sucursal"));

        return ResponseEntity.ok(resource);
    }

    @Operation(
            summary = "Obtener inventario por ID",
            description = "Recupera la información de un registro de inventario específico"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Inventario encontrado",
                    content = @Content(schema = @Schema(implementation = InventarioResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Inventario no encontrado",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<InventarioResponse>> obtenerInventario(
            @Parameter(description = "ID del registro de inventario", required = true)
            @PathVariable Long id) {
        InventarioResponse response = inventarioService.obtenerInventarioPorId(id);

        EntityModel<InventarioResponse> resource = EntityModel.of(response);
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(InventarioController.class).obtenerInventario(id)).withSelfRel());
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(InventarioController.class).actualizarStock(id, 0)).withRel("update-stock"));
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(InventarioController.class).vender(id)).withRel("sell"));
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(InventarioController.class).obtenerPorSucursal(response.getSucursal().getId())).withRel("by-sucursal"));
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(InventarioController.class).transferirStock(response.getProducto().getId(), response.getSucursal().getId(), null, 0)).withRel("transfer"));

        return ResponseEntity.ok(resource);
    }

    @Operation(
            summary = "Obtener inventario por sucursal",
            description = "Recupera todos los registros de inventario para una sucursal específica"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de inventarios encontrados",
                    content = @Content(schema = @Schema(implementation = InventarioResponse.class)))
    })
    @GetMapping("/sucursal/{sucursalId}")
    public ResponseEntity<CollectionModel<EntityModel<InventarioResponse>>> obtenerPorSucursal(
            @Parameter(description = "ID de la sucursal", required = true)
            @PathVariable Long sucursalId) {
        List<InventarioResponse> inventarios = inventarioService.obtenerInventarioPorSucursal(sucursalId);

        List<EntityModel<InventarioResponse>> resources = inventarios.stream()
                .map(inventario -> {
                    EntityModel<InventarioResponse> resource = EntityModel.of(inventario);
                    resource.add(WebMvcLinkBuilder.linkTo(methodOn(InventarioController.class).obtenerInventario(inventario.getId())).withSelfRel());
                    resource.add(WebMvcLinkBuilder.linkTo(methodOn(InventarioController.class).actualizarStock(inventario.getId(), 0)).withRel("update-stock"));
                    return resource;
                })
                .collect(Collectors.toList());

        CollectionModel<EntityModel<InventarioResponse>> collection = CollectionModel.of(resources);
        collection.add(WebMvcLinkBuilder.linkTo(methodOn(InventarioController.class).obtenerPorSucursal(sucursalId)).withSelfRel());
        collection.add(WebMvcLinkBuilder.linkTo(methodOn(InventarioController.class).registrarInventario(null)).withRel("register"));

        return ResponseEntity.ok(collection);
    }

    @Operation(
            summary = "Actualizar stock",
            description = "Actualiza la cantidad disponible de un producto en el inventario"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Stock actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = InventarioResponse.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Cantidad inválida",
                    content = @Content)
    })
    @PatchMapping("/{id}/stock")
    public ResponseEntity<EntityModel<InventarioResponse>> actualizarStock(
            @Parameter(description = "ID del registro de inventario", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nueva cantidad de stock", required = true)
            @RequestParam Integer cantidad) {
        InventarioResponse response = inventarioService.actualizarStock(id, cantidad);

        EntityModel<InventarioResponse> resource = EntityModel.of(response);
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(InventarioController.class).actualizarStock(id, cantidad)).withSelfRel());
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(InventarioController.class).obtenerInventario(id)).withRel("inventory"));
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(InventarioController.class).vender(id)).withRel("sell"));

        return ResponseEntity.ok(resource);
    }

    @Operation(
            summary = "Transferir stock",
            description = "Transfiere stock de un producto entre sucursales"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transferencia realizada exitosamente",
                    content = @Content(schema = @Schema(implementation = InventarioResponse.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Stock insuficiente o parámetros inválidos",
                    content = @Content)
    })
    @PostMapping("/transferencia")
    public ResponseEntity<EntityModel<InventarioResponse>> transferirStock(
            @Parameter(description = "ID del producto", required = true)
            @RequestParam Long productoId,
            @Parameter(description = "ID de la sucursal origen", required = true)
            @RequestParam Long origenId,
            @Parameter(description = "ID de la sucursal destino", required = true)
            @RequestParam Long destinoId,
            @Parameter(description = "Cantidad a transferir", required = true)
            @RequestParam Integer cantidad) {
        InventarioResponse response = inventarioService.transferirStock(origenId, destinoId, productoId, cantidad);

        EntityModel<InventarioResponse> resource = EntityModel.of(response);
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(InventarioController.class).transferirStock(productoId, origenId, destinoId, cantidad)).withSelfRel());
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(InventarioController.class).obtenerInventario(response.getId())).withRel("inventory"));
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(InventarioController.class).obtenerPorSucursal(origenId)).withRel("origin-inventory"));
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(InventarioController.class).obtenerPorSucursal(destinoId)).withRel("destination-inventory"));

        return ResponseEntity.ok(resource);
    }

    @Operation(
            summary = "Obtener productos con bajo stock",
            description = "Recupera los productos que están por debajo del stock mínimo configurado"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de productos con bajo stock",
                    content = @Content(schema = @Schema(implementation = InventarioResponse.class)))
    })
    @GetMapping("/bajo-stock")
    public ResponseEntity<CollectionModel<EntityModel<InventarioResponse>>> obtenerProductosBajoStock() {
        List<InventarioResponse> inventarios = inventarioService.obtenerProductosBajoStockMinimo();

        List<EntityModel<InventarioResponse>> resources = inventarios.stream()
                .map(inventario -> {
                    EntityModel<InventarioResponse> resource = EntityModel.of(inventario);
                    resource.add(WebMvcLinkBuilder.linkTo(methodOn(InventarioController.class).obtenerInventario(inventario.getId())).withSelfRel());
                    resource.add(WebMvcLinkBuilder.linkTo(methodOn(InventarioController.class).actualizarStock(inventario.getId(), 0)).withRel("update-stock"));
                    return resource;
                })
                .collect(Collectors.toList());

        CollectionModel<EntityModel<InventarioResponse>> collection = CollectionModel.of(resources);
        collection.add(WebMvcLinkBuilder.linkTo(methodOn(InventarioController.class).obtenerProductosBajoStock()).withSelfRel());

        return ResponseEntity.ok(collection);
    }

    @Operation(
            summary = "Registrar venta",
            description = "Reduce el stock disponible al registrar una venta"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Venta registrada exitosamente",
                    content = @Content(schema = @Schema(implementation = InventarioResponse.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Stock insuficiente",
                    content = @Content)
    })
    @PostMapping("/{id}/vender")
    public ResponseEntity<?> vender(
            @Parameter(description = "ID del registro de inventario", required = true)
            @PathVariable Long id) {
        ServiceResult<InventarioResponse> response = inventarioService.vender(id);
        if (response.hasErrors()) {
            return ResponseEntity.badRequest().body(response.getErrors());
        }

        InventarioResponse data = response.getData();
        EntityModel<InventarioResponse> resource = EntityModel.of(data);
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(InventarioController.class).vender(id)).withSelfRel());
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(InventarioController.class).cancelarventa(id)).withRel("cancel-sale"));
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(InventarioController.class).obtenerInventario(id)).withRel("inventory"));

        return ResponseEntity.ok(resource);
    }

    @Operation(
            summary = "Cancelar venta",
            description = "Cancela una venta y restaura el stock"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Venta cancelada exitosamente",
                    content = @Content(schema = @Schema(implementation = InventarioResponse.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "No se puede cancelar la venta",
                    content = @Content)
    })
    @PostMapping("/{id}/cancelar-venta")
    public ResponseEntity<EntityModel<InventarioResponse>> cancelarventa(
            @Parameter(description = "ID del registro de inventario", required = true)
            @PathVariable Long id) {
        InventarioResponse response = inventarioService.canerlarVenta(id);

        EntityModel<InventarioResponse> resource = EntityModel.of(response);
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(InventarioController.class).cancelarventa(id)).withSelfRel());
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(InventarioController.class).vender(id)).withRel("sell"));
        resource.add(WebMvcLinkBuilder.linkTo(methodOn(InventarioController.class).obtenerInventario(id)).withRel("inventory"));

        return ResponseEntity.ok(resource);
    }
}