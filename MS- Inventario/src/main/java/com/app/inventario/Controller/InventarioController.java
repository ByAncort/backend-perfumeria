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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<InventarioResponse> registrarInventario(
            @RequestBody InventarioRequest request) {


        InventarioResponse response = inventarioService.registrarInventario(request);

        return ResponseEntity.ok(response);
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
    public ResponseEntity<InventarioResponse> obtenerInventario(
            @Parameter(description = "ID del registro de inventario", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(inventarioService.obtenerInventarioPorId(id));
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
    public ResponseEntity<List<InventarioResponse>> obtenerPorSucursal(
            @Parameter(description = "ID de la sucursal", required = true)
            @PathVariable Long sucursalId) {
        return ResponseEntity.ok(inventarioService.obtenerInventarioPorSucursal(sucursalId));
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
    public ResponseEntity<InventarioResponse> actualizarStock(
            @Parameter(description = "ID del registro de inventario", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nueva cantidad de stock", required = true)
            @RequestParam Integer cantidad) {
        return ResponseEntity.ok(inventarioService.actualizarStock(id, cantidad));
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
    public ResponseEntity<InventarioResponse> transferirStock(
            @Parameter(description = "ID del producto", required = true)
            @RequestParam Long productoId,
            @Parameter(description = "ID de la sucursal origen", required = true)
            @RequestParam Long origenId,
            @Parameter(description = "ID de la sucursal destino", required = true)
            @RequestParam Long destinoId,
            @Parameter(description = "Cantidad a transferir", required = true)
            @RequestParam Integer cantidad) {
        return ResponseEntity.ok(
                inventarioService.transferirStock(origenId, destinoId, productoId, cantidad)
        );
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
    public ResponseEntity<List<InventarioResponse>> obtenerProductosBajoStock() {
        return ResponseEntity.ok(inventarioService.obtenerProductosBajoStockMinimo());
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
    public ResponseEntity<InventarioResponse> vender(
            @Parameter(description = "ID del registro de inventario", required = true)
            @PathVariable Long id) {
        InventarioResponse response = inventarioService.vender(id);
        return ResponseEntity.ok(response);
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
    public ResponseEntity<InventarioResponse> cancelarventa(
            @Parameter(description = "ID del registro de inventario", required = true)
            @PathVariable Long id) {
        InventarioResponse response = inventarioService.canerlarVenta(id);
        return ResponseEntity.ok(response);
    }
}