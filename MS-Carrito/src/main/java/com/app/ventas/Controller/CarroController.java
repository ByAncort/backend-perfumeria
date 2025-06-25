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
@RequestMapping("/api/carrito/")
@RequiredArgsConstructor
@Tag(name = "Gestión de Carrito", description = "API para el manejo del carrito de compras con cupones")
public class CarroController {

    private final CarroService carroService;

    @Operation(
            summary = "Agregar productos al carrito",
            description = "Añade uno o más productos al carrito de compras del usuario con opción de aplicar cupón",
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
                    description = "Error en los datos, productos no disponibles o cupón inválido",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class))
            )
    })
    @PostMapping("agregar-productos")
    public ResponseEntity<?> agregarProductos(
            @Parameter(description = "Datos de los productos a agregar incluyendo código de cupón opcional", required = true)
            @Valid @RequestBody CarroRequest request) {

        ServiceResult<CarroResponse> result = carroService.agregarProductosAlCarro(request);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(result);
    }

    @Operation(
            summary = "Aplicar cupón al carrito",
            description = "Aplica un cupón de descuento al carrito de compras",
            operationId = "aplicarCupon"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cupón aplicado exitosamente",
                    content = @Content(schema = @Schema(implementation = CarroResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error al aplicar cupón (cupón inválido, expirado o carrito no válido)",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Carrito no encontrado"
            )
    })
    @PutMapping("/{carroId}/aplicar-cupon/{codigoCupon}")
    public ResponseEntity<?> aplicarCupon(
            @Parameter(description = "ID del carrito", required = true) @PathVariable Long carroId,
            @Parameter(description = "Código del cupón a aplicar", required = true) @PathVariable String codigoCupon) {

        ServiceResult<?> result = carroService.aplicarCuponACarro(carroId, codigoCupon);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(result);
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
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Carrito no encontrado"
            )
    })
    @DeleteMapping("/{carroId}")
    public ResponseEntity<?> vaciarCarrito(
            @Parameter(description = "ID del carrito a vaciar", required = true)
            @PathVariable Long carroId) {

        ServiceResult<Void> result = carroService.vaciarCarro(carroId);

        if (result.hasErrors()) {
            return ResponseEntity.status(result.getErrors().contains("Carro no encontrado") ?
                            HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST)
                    .body(result);
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
                    content = @Content(schema = @Schema(implementation = CarroResponse.class))
            )
    })
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<ServiceResult<List<CarroResponse>>> listarPorUsuario(
            @Parameter(description = "ID del usuario", required = true)
            @PathVariable Long usuarioId) {

        ServiceResult<List<CarroResponse>> result = carroService.listarCarrosPorUsuario(usuarioId);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Obtener carrito por ID",
            description = "Recupera los detalles de un carrito específico incluyendo descuentos aplicados",
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
    @GetMapping("/obtener-carrito/{carroId}")
    public ResponseEntity<?> obtenerCarrito(
            @Parameter(description = "ID del carrito", required = true)
            @PathVariable Long carroId) {

        ServiceResult<CarroResponse> result = carroService.obtenerCarroPorId(carroId);

        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Confirmar compra",
            description = "Finaliza la compra, actualiza el inventario y aplica descuentos por cupón",
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
                    description = "Error al confirmar la compra (inventario insuficiente, carrito vacío)",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Carrito no encontrado"
            )
    })
    @PostMapping("/{carroId}/confirmar")
    public ResponseEntity<?> confirmarCompra(
            @Parameter(description = "ID del carrito a confirmar", required = true)
            @PathVariable Long carroId) {

        ServiceResult<CarroResponse> result = carroService.confirmarCompra(carroId);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(result);
    }
}