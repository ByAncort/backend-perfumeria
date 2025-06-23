package org.necronet.mssoportecliente.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import org.app.dto.ServiceResult;
import org.necronet.mssoportecliente.dto.*;
import org.necronet.mssoportecliente.service.SoporteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/soporte")
@Tag(name = "Soporte al Cliente", description = "API para gestión de tickets de soporte")
public class SoporteController {

    private final SoporteService soporteService;

    @Operation(summary = "Crear un nuevo ticket de soporte", description = "Registra un nuevo ticket de soporte para un cliente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket creado exitosamente",
                    content = @Content(schema = @Schema(implementation = TicketSoporteDto.class))),
            @ApiResponse(responseCode = "400", description = "Error al crear el ticket",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    })
    @PostMapping("/tickets")
    public ResponseEntity<ServiceResult<TicketSoporteDto>> crearTicket(
            @RequestBody TicketRequest request) {

        ServiceResult<TicketSoporteDto> result = soporteService.crearTicket(request);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Obtener tickets por cliente", description = "Devuelve todos los tickets de soporte asociados a un cliente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tickets obtenidos exitosamente",
                    content = @Content(schema = @Schema(implementation = TicketSoporteDto.class))),
            @ApiResponse(responseCode = "400", description = "Error al obtener los tickets",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    })
    @GetMapping("/tickets/cliente/{clienteId}")
    public ResponseEntity<ServiceResult<List<TicketSoporteDto>>> obtenerTicketsPorCliente(
            @Parameter(description = "ID del cliente", required = true)
            @PathVariable Long clienteId) {

        ServiceResult<List<TicketSoporteDto>> result = soporteService.obtenerTicketsPorCliente(clienteId);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Actualizar ticket de soporte", description = "Actualiza la información de un ticket de soporte.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = TicketSoporteDto.class))),
            @ApiResponse(responseCode = "400", description = "Error al actualizar el ticket",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    })
    @PutMapping("/tickets/{ticketId}")
    public ResponseEntity<ServiceResult<TicketSoporteDto>> actualizarTicket(
            @Parameter(description = "ID del ticket", required = true)
            @PathVariable Long ticketId,
            @RequestBody TicketUpdateRequest request) {

        ServiceResult<TicketSoporteDto> result = soporteService.actualizarTicket(ticketId, request);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Obtener tickets por estado", description = "Filtra los tickets de soporte según su estado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tickets filtrados por estado obtenidos exitosamente",
                    content = @Content(schema = @Schema(implementation = TicketSoporteDto.class))),
            @ApiResponse(responseCode = "400", description = "Error al obtener los tickets",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    })
    @GetMapping("/tickets/estado/{estado}")
    public ResponseEntity<ServiceResult<List<TicketSoporteDto>>> obtenerTicketsPorEstado(
            @Parameter(description = "Estado del ticket (ej. ABIERTO, EN_PROCESO, CERRADO)", required = true)
            @PathVariable String estado) {

        ServiceResult<List<TicketSoporteDto>> result = soporteService.obtenerTicketsPorEstado(estado);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
