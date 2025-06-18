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
@RequestMapping("/api/carrito")
@RequiredArgsConstructor
@Tag(name = "Gestión de Carrito", description = "API para el manejo del carrito de compras")
public class CarroController {

    private final CarroService carroService;

    @Operation(
            summary = "Agregar productos al carrito",
            description = "Añade uno o más productos al carrito de compras del usuario",
            operationId = "agregarAlCarrito"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Productos agregados al carrito exitosamente",
                    content = @Content(schema = @Schema(implementation = CarroResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error en los datos o productos no disponibles",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class))
            )
    })
    @PostMapping
    public ResponseEntity<?> agregarProductos(
            @Parameter(description = "Datos de los productos a agregar", required = true)
            @Valid @RequestBody CarroRequest request) {

        ServiceResult<CarroResponse> result = carroService.agregarProductosAlCarro(request);

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result.getData());
    }

    @Operation(
            summary = "Vaciar carrito",
            description = "Elimina todos los productos del carrito sin completar la compra",
            operationId = "vaciarCarrito"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carrito vaciado exitosamente"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error al vaciar el carrito",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class))
            )
    })
    @DeleteMapping("/{carroId}")
    public ResponseEntity<?> vaciarCarrito(
            @Parameter(description = "ID del carrito a vaciar", required = true)
            @PathVariable Long carroId) {

        ServiceResult<Void> result = carroService.vaciarCarro(carroId);

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Listar carritos del usuario",
            description = "Obtiene todos los carritos activos de un usuario",
            operationId = "listarCarritosUsuario"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de carritos obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = CarroResponse[].class))
            )
    })
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<CarroResponse>> listarPorUsuario(
            @Parameter(description = "ID del usuario", required = true)
            @PathVariable Long usuarioId) {

        return ResponseEntity.ok(carroService.listarCarrosPorUsuario(usuarioId).getData());
    }

    @Operation(
            summary = "Obtener carrito por ID",
            description = "Recupera los detalles de un carrito específico",
            operationId = "obtenerCarrito"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Carrito encontrado",
                    content = @Content(schema = @Schema(implementation = CarroResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Carrito no encontrado"
            )
    })
    @GetMapping("/{carroId}")
    public ResponseEntity<?> obtenerCarrito(
            @Parameter(description = "ID del carrito", required = true)
            @PathVariable Long carroId) {

        ServiceResult<CarroResponse> result = carroService.obtenerCarroPorId(carroId);

        if (result.hasErrors()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result.getData());
    }

    @Operation(
            summary = "Confirmar compra",
            description = "Finaliza la compra y actualiza el inventario",
            operationId = "confirmarCompra"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Compra confirmada exitosamente",
                    content = @Content(schema = @Schema(implementation = CarroResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error al confirmar la compra",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class))
            )
    })
    @PostMapping("/{carroId}/confirmar")
    public ResponseEntity<?> confirmarCompra(
            @Parameter(description = "ID del carrito a confirmar", required = true)
            @PathVariable Long carroId) {

        ServiceResult<CarroResponse> result = carroService.confirmarCompra(carroId);

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }
        return ResponseEntity.ok(result.getData());
    }
}