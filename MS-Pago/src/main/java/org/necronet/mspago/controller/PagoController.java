package org.necronet.mspago.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.app.dto.ServiceResult;
import org.necronet.mspago.model.Pago;
import org.necronet.mspago.service.PagoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/pagos")
@Tag(name = "Pagos", description = "API para gestión de pagos y reembolsos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @Operation(
            summary = "Procesar un pago",
            description = "Procesa un pago para un carrito específico con el método de pago seleccionado"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Pago procesado exitosamente",
                    content = @Content(schema = @Schema(implementation = Pago.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error al procesar el pago",
                    content = @Content(schema = @Schema(implementation = List.class))
            )
    })
    @PostMapping("/procesar")
    public ResponseEntity<?> procesarPago(
            @Parameter(description = "ID del carrito a pagar", required = true, example = "123")
            @RequestParam Long carritoId,

            @Parameter(
                    description = "Método de pago",
                    required = true,
                    example = "TARJETA_CREDITO",
                    schema = @Schema(allowableValues = {"TARJETA_CREDITO", "PAYPAL", "TRANSFERENCIA"})
            )
            @RequestParam String metodoPago) {

        ServiceResult<Pago> resultado = pagoService.procesarPago(carritoId, metodoPago);

        if (resultado.hasErrors()) {
            return ResponseEntity.badRequest().body(resultado.getErrors());
        }

        return ResponseEntity.created(URI.create("/api/pagos/" + resultado.getData().getId()))
                .body(resultado.getData());
    }

    @Operation(
            summary = "Obtener un pago por ID",
            description = "Recupera los detalles de un pago específico usando su ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pago encontrado",
                    content = @Content(schema = @Schema(implementation = Pago.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pago no encontrado"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPago(
            @Parameter(description = "ID del pago a buscar", required = true, example = "1")
            @PathVariable Long id) {

        ServiceResult<Pago> resultado = pagoService.obtenerPagoPorId(id);

        if (resultado.hasErrors()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(resultado.getData());
    }

    @Operation(
            summary = "Obtener pagos por usuario",
            description = "Recupera todos los pagos realizados por un usuario específico"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de pagos del usuario",
                    content = @Content(schema = @Schema(implementation = Pago[].class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error al obtener los pagos",
                    content = @Content(schema = @Schema(implementation = List.class))
            )
    })
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> obtenerPagosPorUsuario(
            @Parameter(description = "ID del usuario", required = true, example = "1001")
            @PathVariable Long usuarioId) {

        ServiceResult<List<Pago>> resultado = pagoService.obtenerPagosPorUsuario(usuarioId);

        if (resultado.hasErrors()) {
            return ResponseEntity.badRequest().body(resultado.getErrors());
        }

        return ResponseEntity.ok(resultado.getData());
    }

    @Operation(
            summary = "Reembolsar un pago",
            description = "Inicia el proceso de reembolso para un pago completado"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reembolso procesado exitosamente",
                    content = @Content(schema = @Schema(implementation = Pago.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error al procesar el reembolso",
                    content = @Content(schema = @Schema(implementation = List.class))
            )
    })
    @PostMapping("/{id}/reembolsar")
    public ResponseEntity<?> reembolsarPago(
            @Parameter(description = "ID del pago a reembolsar", required = true, example = "1")
            @PathVariable Long id) {

        ServiceResult<Pago> resultado = pagoService.reembolsarPago(id);

        if (resultado.hasErrors()) {
            return ResponseEntity.badRequest().body(resultado.getErrors());
        }

        return ResponseEntity.ok(resultado.getData());
    }
}