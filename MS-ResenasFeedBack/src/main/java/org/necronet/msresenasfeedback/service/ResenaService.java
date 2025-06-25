package org.necronet.msresenasfeedback.service;

import lombok.RequiredArgsConstructor;
import org.app.dto.ServiceResult;


import org.necronet.msresenasfeedback.dto.*;
import org.necronet.msresenasfeedback.model.Resena;
import org.necronet.msresenasfeedback.repository.ResenaRepository;
import org.necronet.msresenasfeedback.shared.MicroserviceClient;
import org.necronet.msresenasfeedback.shared.TokenContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResenaService {

    private final ResenaRepository resenaRepository;
    private final MicroserviceClient microserviceClient;

    @Value("${auth.url.productoMicro}")
    private String PRODUCTOS_SERVICE_URL;
    @Value("${auth.url.clienteMicro}")
    private String CLIENTE_SERVICE_URL;

    private ServiceResult<ProductoDto> obtenerProducto(Long productoId) {
        List<String> errors = new ArrayList<>();
        try {
            String token = TokenContext.getToken();
            String url = PRODUCTOS_SERVICE_URL + "/api/productos/get/" + productoId;
            ResponseEntity<ProductoDto> response = microserviceClient.enviarConToken(
                    url,
                    HttpMethod.GET,
                    null,
                    ProductoDto.class,
                    token
            );
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                errors.add("Error al obtener los datos del producto");
                return new ServiceResult<>(errors);
            }
            return new ServiceResult<>(response.getBody());
        } catch (Exception e) {
            errors.add("Error al obtener los datos del producto");
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
                errors.add("Error al obtener los datos del cliente");
                return new ServiceResult<>(errors);
            }
            return new ServiceResult<>(response.getBody());
        } catch (Exception e) {
            errors.add("Error al obtener los datos del cliente");
            return new ServiceResult<>(errors);
        }
    }

    @Transactional
    public ServiceResult<ResenaDto> crearResena(CrearResenaDto dto) {
        List<String> errors = new ArrayList<>();

        ServiceResult<ProductoDto> productoResult = obtenerProducto(dto.getProductoId());
        if (productoResult.hasErrors()) return new ServiceResult<>(productoResult.getErrors());

        ServiceResult<ClienteDto> clienteResult = obtenerCliente(dto.getClienteId());
        if (clienteResult.hasErrors()) return new ServiceResult<>(clienteResult.getErrors());

        Resena resena = Resena.builder()
                .productoId(dto.getProductoId())
                .clienteId(dto.getClienteId())
                .comentario(dto.getComentario())
                .calificacion(dto.getCalificacion())
                .fechaCreacion(LocalDateTime.now())
                .build();

        Resena guardada = resenaRepository.save(resena);

        ResenaDto resenaDto = ResenaDto.builder()
                .id(guardada.getId())
                .productoId(guardada.getProductoId())
                .clienteId(guardada.getClienteId())
                .comentario(guardada.getComentario())
                .calificacion(guardada.getCalificacion())
                .fechaCreacion(guardada.getFechaCreacion())
                .build();

        return new ServiceResult<>(resenaDto);
    }

    public ServiceResult<List<ResenaProductoDto>> obtenerResenasPorProducto(Long productoId) {
        List<Resena> resenas = resenaRepository.findByProductoId(productoId);
        List<ResenaProductoDto> resultado = resenas.stream()
                .map(r -> ResenaProductoDto.builder()
                        .comentario(r.getComentario())
                        .calificacion(r.getCalificacion())

                        .build())
                .collect(Collectors.toList());

        return new ServiceResult<>(resultado);
    }

    public ServiceResult<List<ResenaDto>> obtenerTodasResenas() {
        List<ResenaDto> resenas = resenaRepository.findAll().stream()
                .map(r -> ResenaDto.builder()
                        .id(r.getId())
                        .productoId(r.getProductoId())
                        .clienteId(r.getClienteId())
                        .comentario(r.getComentario())
                        .calificacion(r.getCalificacion())
                        .fechaCreacion(r.getFechaCreacion())
                        .build())
                .collect(Collectors.toList());

        return new ServiceResult<>(resenas);
    }
}
