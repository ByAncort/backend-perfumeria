package com.app.ventas.Controller;

import com.app.ventas.Dto.*;
import com.app.ventas.Service.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
@Tag(name = "Gestión de Ventas", description = "API para el manejo de ventas del sistema")
public class VentaController {

    private final VentaService ventaService;

    @Operation(
            summary = "Crear una nueva venta",
            description = "Endpoint para registrar una nueva venta en el sistema",
            operationId = "crearVenta"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Venta creada exitosamente",
                    content = @Content(schema = @Schema(implementation = VentaResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class))
            )
    })
    @PostMapping
    public ResponseEntity<?> crearVenta(
            @Parameter(description = "Datos de la venta a crear", required = true,
                    content = @Content(schema = @Schema(implementation = VentaRequest.class)))
            @Valid @RequestBody VentaRequest req) {

        ServiceResult<VentaResponse> result = ventaService.crearVenta(req);

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result.getData());
    }

    @Operation(
            summary = "Anular una venta",
            description = "Endpoint para anular/cancelar una venta existente",
            operationId = "anularVenta"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Venta anulada exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "No se pudo anular la venta",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class))
            )
    })
    @PatchMapping("/{id}/anular")
    public ResponseEntity<?> anularVenta(
            @Parameter(description = "ID de la venta a anular", required = true, example = "1")
            @PathVariable Long id) {
        ServiceResult<Void> result = ventaService.cancelarVentaMetodo(id);

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Listar todas las ventas",
            description = "Endpoint para obtener un listado completo de todas las ventas",
            operationId = "listarVentas"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de ventas obtenido exitosamente",
                    content = @Content(schema = @Schema(implementation = VentaResponse[].class))
            )
    })
    @GetMapping("listar-ventas")
    public ResponseEntity<List<VentaResponse>> listarVentas() {
        return ResponseEntity.ok(ventaService.listarVentas().getData());
    }

    @Operation(
            summary = "Obtener venta por ID",
            description = "Endpoint para recuperar los detalles de una venta específica",
            operationId = "obtenerVentaPorId"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Venta encontrada",
                    content = @Content(schema = @Schema(implementation = VentaResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Venta no encontrada"
            )
    })
    @GetMapping("/{ventaId}")
    public ResponseEntity<?> obtenerVentaPorId(
            @Parameter(description = "ID de la venta a buscar", required = true, example = "1")
            @PathVariable Long ventaId) {
        ServiceResult<VentaResponse> result = ventaService.obtenerVentaPorId(ventaId);

        if (result.hasErrors()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result.getData());
    }
}
