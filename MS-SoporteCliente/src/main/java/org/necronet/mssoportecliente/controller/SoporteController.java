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
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

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
    public ResponseEntity<?> crearTicket(
            @RequestBody TicketRequest request) {

        ServiceResult<TicketSoporteDto> result = soporteService.crearTicket(request);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result);
        }

        TicketSoporteDto ticket = result.getData();
        EntityModel<TicketSoporteDto> resource = EntityModel.of(ticket);

        // Self link
        resource.add(linkTo(methodOn(SoporteController.class).crearTicket(request)).withSelfRel());
        // Links relacionados
        resource.add(linkTo(methodOn(SoporteController.class)
                .obtenerTicketsPorCliente(ticket.getClienteId())).withRel("tickets-cliente"));
        resource.add(linkTo(methodOn(SoporteController.class)
                .actualizarTicket(ticket.getId(), new TicketUpdateRequest())).withRel("update"));
        resource.add(linkTo(methodOn(SoporteController.class)
                .obtenerTicketsPorEstado(ticket.getEstado())).withRel("tickets-estado"));

        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Obtener tickets por cliente", description = "Devuelve todos los tickets de soporte asociados a un cliente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tickets obtenidos exitosamente",
                    content = @Content(schema = @Schema(implementation = TicketSoporteDto.class))),
            @ApiResponse(responseCode = "400", description = "Error al obtener los tickets",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    })
    @GetMapping("/tickets/cliente/{clienteId}")
    public ResponseEntity<?> obtenerTicketsPorCliente(
            @Parameter(description = "ID del cliente", required = true)
            @PathVariable Long clienteId) {

        ServiceResult<List<TicketSoporteDto>> result = soporteService.obtenerTicketsPorCliente(clienteId);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result);
        }

        List<EntityModel<TicketSoporteDto>> tickets = result.getData().stream()
                .map(ticket -> {
                    EntityModel<TicketSoporteDto> resource = EntityModel.of(ticket);
                    resource.add(linkTo(methodOn(SoporteController.class)
                            .obtenerTicketsPorCliente(clienteId)).withSelfRel());
                    resource.add(linkTo(methodOn(SoporteController.class)
                            .actualizarTicket(ticket.getId(), new TicketUpdateRequest())).withRel("update"));
                    resource.add(linkTo(methodOn(SoporteController.class)
                            .obtenerTicketsPorEstado(ticket.getEstado())).withRel("tickets-estado"));
                    return resource;
                })
                .collect(Collectors.toList());

        Link selfLink = linkTo(methodOn(SoporteController.class)
                .obtenerTicketsPorCliente(clienteId)).withSelfRel();
        Link createLink = linkTo(methodOn(SoporteController.class)
                .crearTicket(new TicketRequest())).withRel("create-ticket");

        CollectionModel<EntityModel<TicketSoporteDto>> resources = CollectionModel.of(tickets, selfLink, createLink);

        return ResponseEntity.ok(resources);
    }

    @Operation(summary = "Actualizar ticket de soporte", description = "Actualiza la información de un ticket de soporte.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = TicketSoporteDto.class))),
            @ApiResponse(responseCode = "400", description = "Error al actualizar el ticket",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    })
    @PutMapping("/tickets/{ticketId}")
    public ResponseEntity<?> actualizarTicket(
            @Parameter(description = "ID del ticket", required = true)
            @PathVariable Long ticketId,
            @RequestBody TicketUpdateRequest request) {

        ServiceResult<TicketSoporteDto> result = soporteService.actualizarTicket(ticketId, request);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result);
        }

        TicketSoporteDto ticket = result.getData();
        EntityModel<TicketSoporteDto> resource = EntityModel.of(ticket);

        // Self link
        resource.add(linkTo(methodOn(SoporteController.class)
                .actualizarTicket(ticketId, request)).withSelfRel());
        // Links relacionados
        resource.add(linkTo(methodOn(SoporteController.class)
                .obtenerTicketsPorCliente(ticket.getClienteId())).withRel("tickets-cliente"));
        resource.add(linkTo(methodOn(SoporteController.class)
                .obtenerTicketsPorEstado(ticket.getEstado())).withRel("tickets-estado"));

        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Obtener tickets por estado", description = "Filtra los tickets de soporte según su estado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tickets filtrados por estado obtenidos exitosamente",
                    content = @Content(schema = @Schema(implementation = TicketSoporteDto.class))),
            @ApiResponse(responseCode = "400", description = "Error al obtener los tickets",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    })
    @GetMapping("/tickets/estado/{estado}")
    public ResponseEntity<?> obtenerTicketsPorEstado(
            @Parameter(description = "Estado del ticket (ej. ABIERTO, EN_PROCESO, CERRADO)", required = true)
            @PathVariable String estado) {

        ServiceResult<List<TicketSoporteDto>> result = soporteService.obtenerTicketsPorEstado(estado);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result);
        }

        List<EntityModel<TicketSoporteDto>> tickets = result.getData().stream()
                .map(ticket -> {
                    EntityModel<TicketSoporteDto> resource = EntityModel.of(ticket);
                    resource.add(linkTo(methodOn(SoporteController.class)
                            .obtenerTicketsPorEstado(estado)).withSelfRel());
                    resource.add(linkTo(methodOn(SoporteController.class)
                            .actualizarTicket(ticket.getId(), new TicketUpdateRequest())).withRel("update"));
                    resource.add(linkTo(methodOn(SoporteController.class)
                            .obtenerTicketsPorCliente(ticket.getClienteId())).withRel("tickets-cliente"));
                    return resource;
                })
                .collect(Collectors.toList());

        Link selfLink = linkTo(methodOn(SoporteController.class)
                .obtenerTicketsPorEstado(estado)).withSelfRel();
        Link createLink = linkTo(methodOn(SoporteController.class)
                .crearTicket(new TicketRequest())).withRel("create-ticket");

        CollectionModel<EntityModel<TicketSoporteDto>> resources = CollectionModel.of(tickets, selfLink, createLink);

        return ResponseEntity.ok(resources);
    }
}