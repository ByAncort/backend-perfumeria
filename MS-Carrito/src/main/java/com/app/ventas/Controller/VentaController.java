package com.app.ventas.Controller;

import com.app.ventas.Dto.*;
import com.app.ventas.Service.CarroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.app.dto.ServiceResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
@Tag(name = "Gestión de Carrito", description = "API para el manejo del carrito de compras del sistema")
public class VentaController {

    private final CarroService carroService;

    @Operation(
            summary = "Agregar producto al carrito",
            description = "Endpoint para agregar un nuevo producto al carrito de compras",
            operationId = "agregarAlCarrito"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Producto agregado al carrito exitosamente",
                    content = @Content(schema = @Schema(implementation = CarroResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos o producto no disponible",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class))
            )
    })
    @PostMapping
    public ResponseEntity<?> agregarAlCarrito(
            @Parameter(description = "Datos del producto a agregar al carrito", required = true,
                    content = @Content(schema = @Schema(implementation = CarroRequest.class)))
            @Valid @RequestBody CarroRequest req) {

        ServiceResult<CarroResponse> result = carroService.crearVenta(req);

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result.getData());
    }

    @Operation(
            summary = "Eliminar producto del carrito",
            description = "Endpoint para eliminar un producto del carrito de compras",
            operationId = "eliminarDelCarrito"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Producto eliminado del carrito exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "No se pudo eliminar el producto del carrito",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class))
            )
    })
    @PatchMapping("/{id}/anular")
    public ResponseEntity<?> eliminarDelCarrito(
            @Parameter(description = "ID del producto en el carrito a eliminar", required = true, example = "1")
            @PathVariable Long id) {
        ServiceResult<Void> result = carroService.cancelarVentaMetodo(id);

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Listar contenido del carrito",
            description = "Endpoint para obtener todos los productos en el carrito de compras",
            operationId = "listarCarrito"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Contenido del carrito obtenido exitosamente",
                    content = @Content(schema = @Schema(implementation = CarroResponse[].class))
            )
    })
    @GetMapping("listar-ventas")
    public ResponseEntity<List<CarroResponse>> listarCarrito() {
        return ResponseEntity.ok(carroService.listarVentas().getData());
    }

    @Operation(
            summary = "Obtener detalle de producto en carrito",
            description = "Endpoint para recuperar los detalles de un producto específico en el carrito",
            operationId = "obtenerItemCarrito"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Producto encontrado en el carrito",
                    content = @Content(schema = @Schema(implementation = CarroResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Producto no encontrado en el carrito"
            )
    })
    @GetMapping("/{ventaId}")
    public ResponseEntity<?> obtenerItemCarrito(
            @Parameter(description = "ID del producto en el carrito", required = true, example = "1")
            @PathVariable Long ventaId) {
        ServiceResult<CarroResponse> result = carroService.obtenerVentaPorId(ventaId);

        if (result.hasErrors()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result.getData());
    }
}