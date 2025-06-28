package org.necronet.mssoportecliente.service;

import lombok.RequiredArgsConstructor;
import org.app.dto.ServiceResult;
import org.necronet.mssoportecliente.dto.*;
import org.necronet.mssoportecliente.model.TicketSoporte;
import org.necronet.mssoportecliente.repository.TicketSoporteRepository;
import org.necronet.mssoportecliente.shared.MicroserviceClient;
import org.necronet.mssoportecliente.shared.TokenContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SoporteService {
    private final TicketSoporteRepository ticketRepository;
    private final MicroserviceClient microserviceClient;

    @Value("${auth.url.clienteMicro}")
    private String CLIENTE_SERVICE_URL;

    @Transactional
    public ServiceResult<TicketSoporteDto> crearTicket(TicketRequest request) {
        List<String> errors = new ArrayList<>();

        try {
    
            ServiceResult<ClienteDto> clienteResult = obtenerCliente(request.getClienteId());
            if (clienteResult.hasErrors()) {
                errors.addAll(clienteResult.getErrors());
                return new ServiceResult<>(errors);
            }


            TicketSoporte ticket = TicketSoporte.builder()
                    .clienteId(request.getClienteId())
                    .tipo(request.getTipo())
                    .estado("ABIERTO")
                    .descripcion(request.getDescripcion())
                    .fechaCreacion(LocalDateTime.now())
                    .fechaActualizacion(LocalDateTime.now())
                    .prioridad(request.getPrioridad())
                    .categoria(request.getCategoria())
                    .build();

            ticket = ticketRepository.save(ticket);

            
            TicketSoporteDto dto = convertToDto(ticket);
            dto.setClienteInfo(clienteResult.getData());

            return new ServiceResult<>(dto);

        } catch (Exception e) {
            errors.add("Error al crear el ticket: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
    }

    public ServiceResult<List<TicketSoporteDto>> obtenerTicketsPorCliente(Long clienteId) {
        List<String> errors = new ArrayList<>();

        try {
            
            ServiceResult<ClienteDto> clienteResult = obtenerCliente(clienteId);
            if (clienteResult.hasErrors()) {
                errors.addAll(clienteResult.getErrors());
                return new ServiceResult<>(errors);
            }

            
            List<TicketSoporte> tickets = ticketRepository.findByClienteId(clienteId);

            
            List<TicketSoporteDto> dtos = tickets.stream()
                    .map(this::convertToDto)
                    .peek(dto -> dto.setClienteInfo(clienteResult.getData()))
                    .collect(Collectors.toList());

            return new ServiceResult<>(dtos);

        } catch (Exception e) {
            errors.add("Error al obtener tickets del cliente: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
    }

    @Transactional
    public ServiceResult<TicketSoporteDto> actualizarTicket(Long ticketId, TicketUpdateRequest request) {
        List<String> errors = new ArrayList<>();

        try {
            
            TicketSoporte ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

            
            if (request.getEstado() != null) {
                ticket.setEstado(request.getEstado());

                if ("CERRADO".equalsIgnoreCase(request.getEstado()) ||
                        "RESUELTO".equalsIgnoreCase(request.getEstado())) {
                    ticket.setFechaCierre(LocalDateTime.now());
                }
            }

            if (request.getSolucion() != null) {
                ticket.setSolucion(request.getSolucion());
            }

            ticket.setFechaActualizacion(LocalDateTime.now());
            ticket = ticketRepository.save(ticket);

            
            ServiceResult<ClienteDto> clienteResult = obtenerCliente(ticket.getClienteId());
            if (clienteResult.hasErrors()) {
                errors.addAll(clienteResult.getErrors());
                return new ServiceResult<>(errors);
            }

            
            TicketSoporteDto dto = convertToDto(ticket);
            dto.setClienteInfo(clienteResult.getData());

            return new ServiceResult<>(dto);

        } catch (Exception e) {
            errors.add("Error al actualizar el ticket: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
    }

    public ServiceResult<List<TicketSoporteDto>> obtenerTicketsPorEstado(String estado) {
        List<String> errors = new ArrayList<>();

        try {
            List<TicketSoporte> tickets = ticketRepository.findByEstado(estado);

            List<TicketSoporteDto> dtos = tickets.stream()
                    .map(ticket -> {
                        TicketSoporteDto dto = convertToDto(ticket);
                        
                        ServiceResult<ClienteDto> clienteResult = obtenerCliente(ticket.getClienteId());
                        if (!clienteResult.hasErrors()) {
                            dto.setClienteInfo(clienteResult.getData());
                        }
                        return dto;
                    })
                    .collect(Collectors.toList());

            return new ServiceResult<>(dtos);

        } catch (Exception e) {
            errors.add("Error al obtener tickets por estado: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
    }

    private ServiceResult<ClienteDto> obtenerCliente(Long clienteId) {
        List<String> errors = new ArrayList<>();
        try {
            String token = TokenContext.getToken();
            String url = CLIENTE_SERVICE_URL + "/api/clientes/" + clienteId;

            ResponseEntity<ClienteDto> response = microserviceClient.enviarConToken(
                    url,
                    HttpMethod.GET,
                    null,
                    ClienteDto.class,
                    token
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                errors.add("Error al obtener el cliente: " + response.getStatusCode());
                return new ServiceResult<>(errors);
            }

            return new ServiceResult<>(response.getBody());

        } catch (Exception e) {
            errors.add("Excepción al llamar al microservicio de clientes: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
    }

    private TicketSoporteDto convertToDto(TicketSoporte ticket) {
        return TicketSoporteDto.builder()
                .id(ticket.getId())
                .clienteId(ticket.getClienteId())
                .tipo(ticket.getTipo())
                .estado(ticket.getEstado())
                .descripcion(ticket.getDescripcion())
                .fechaCreacion(ticket.getFechaCreacion())
                .fechaActualizacion(ticket.getFechaActualizacion())
                .fechaCierre(ticket.getFechaCierre())
                .solucion(ticket.getSolucion())
                .prioridad(ticket.getPrioridad())
                .categoria(ticket.getCategoria())
                .build();
    }
}