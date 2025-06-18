package com.app.ventas.Service;

import com.app.ventas.Dto.*;
import com.app.ventas.Models.Carro;
import com.app.ventas.Models.DetalleCarro;
import com.app.ventas.Repository.CarroRepository;
import com.app.ventas.shared.MicroserviceClient;
import com.app.ventas.shared.TokenContext;
import lombok.RequiredArgsConstructor;
import org.app.dto.ServiceResult;
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
public class CarroService {

    private final CarroRepository carroRepository;
    private final MicroserviceClient microserviceClient;

    private InventarioResponse actualizarInventario(Long id, String accion) {
        String token = TokenContext.getToken();
        String url = String.format("http://localhost:9017/api/inventario/%d/%s", id, accion);
        ResponseEntity<InventarioResponse> response = microserviceClient.enviarConToken(
                url,
                HttpMethod.POST,
                null,
                InventarioResponse.class,
                token
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Error al actualizar inventario");
        }

        return response.getBody();
    }

    private InventarioDto obtenerProductoInventario(Long inventarioId) {
        String token = TokenContext.getToken();
        String url = "http://localhost:9017/api/inventario/" + inventarioId;
        ResponseEntity<InventarioDto> response = microserviceClient.enviarConToken(
                url,
                HttpMethod.GET,
                null,
                InventarioDto.class,
                token
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Error al obtener producto de inventario");
        }

        return response.getBody();
    }

    @Transactional
    public ServiceResult<CarroResponse> agregarProductosAlCarro(CarroRequest request) {
        List<String> errores = new ArrayList<>();
        List<DetalleCarro> detalles = new ArrayList<>();
        double total = 0.0;

        for (CarroRequest.DetalleCarroRequest detalleReq : request.getDetalles()) {
            InventarioDto producto;

            try {
                producto = obtenerProductoInventario(detalleReq.getInventarioId());
            } catch (Exception e) {
                errores.add("Producto no disponible: " + detalleReq.getInventarioId());
                continue;
            }

            if (producto.getCantidad() < detalleReq.getCantidad()) {
                errores.add("Stock insuficiente para: " + producto.getProducto().getNombre());
                continue;
            }

            double precio = producto.getProducto().getPrecio();
            double subtotal = precio * detalleReq.getCantidad();
            total += subtotal;

            DetalleCarro detalle = DetalleCarro.builder()
                    .productoId(producto.getProducto().getId())
                    .cantidad(detalleReq.getCantidad())
                    .precioUnitario(precio)
                    .subtotal(subtotal)
                    .build();

            detalles.add(detalle);
        }

        if (!errores.isEmpty()) {
            return new ServiceResult<>(errores);
        }

        Carro carro = Carro.builder()
                .usuarioId(request.getUsuarioId())
                .fechaCreacion(LocalDateTime.now())
                .total(total)
                .estado("ACTIVO")
                .build();
        for (DetalleCarro d : detalles) {
            d.setCarro(carro);
        }
        carro.setDetalles(detalles);
        carro = carroRepository.save(carro);
        final Carro carroFinal = carro;

        detalles.forEach(d -> d.setCarro(carroFinal));

        try {
            carro = carroRepository.save(carro);
        } catch (Exception e) {
            errores.add("Error al guardar el carro");
            return new ServiceResult<>(errores);
        }

        return new ServiceResult<>(mapearCarroAResponse(carro));
    }

    @Transactional
    public ServiceResult<Void> vaciarCarro(Long carroId) {
        List<String> errores = new ArrayList<>();

        Carro carro = carroRepository.findById(carroId).orElse(null);
        if (carro == null) {
            errores.add("Carro no encontrado");
            return new ServiceResult<>(errores);
        }

        if ("VACIO".equalsIgnoreCase(carro.getEstado())) {
            errores.add("El carro ya está vacío");
            return new ServiceResult<>(errores);
        }

        carro.setEstado("VACIO");
        carro.getDetalles().clear();
        carro.setTotal(0.0);

        try {
            carroRepository.save(carro);
        } catch (Exception e) {
            errores.add("Error al vaciar el carro");
            return new ServiceResult<>(errores);
        }

        return new ServiceResult<>((Void) null);
    }

    @Transactional(readOnly = true)
    public ServiceResult<List<CarroResponse>> listarCarrosPorUsuario(Long usuarioId) {
        List<Carro> carros = carroRepository.findByUsuarioId(usuarioId);
        List<CarroResponse> responses = carros.stream()
                .map(this::mapearCarroAResponse)
                .collect(Collectors.toList());

        return new ServiceResult<>(responses);
    }

    @Transactional(readOnly = true)
    public ServiceResult<CarroResponse> obtenerCarroPorId(Long carroId) {
        List<String> errores = new ArrayList<>();
        Carro carro = carroRepository.findById(carroId).orElse(null);

        if (carro == null) {
            errores.add("Carro no encontrado");
            return new ServiceResult<>(errores);
        }

        return new ServiceResult<>(mapearCarroAResponse(carro));
    }

    @Transactional
    public ServiceResult<CarroResponse> confirmarCompra(Long carroId) {
        List<String> errores = new ArrayList<>();
        Carro carro = carroRepository.findById(carroId).orElse(null);

        if (carro == null) {
            errores.add("Carro no encontrado");
            return new ServiceResult<>(errores);
        }

        for (DetalleCarro detalle : carro.getDetalles()) {
            try {
                actualizarInventario(detalle.getProductoId(), "vender");
            } catch (Exception e) {
                errores.add("Error al confirmar producto: " + detalle.getProductoId());
            }
        }

        if (!errores.isEmpty()) {
            return new ServiceResult<>(errores);
        }

        carro.setEstado("COMPLETADO");
        carro = carroRepository.save(carro);

        return new ServiceResult<>(mapearCarroAResponse(carro));
    }

    private CarroResponse mapearCarroAResponse(Carro carro) {
        List<CarroResponse.DetalleResponse> detalles = carro.getDetalles().stream()
                .map(d -> CarroResponse.DetalleResponse.builder()
                        .productoId(d.getProductoId())
                        .cantidad(d.getCantidad())
                        .precioUnitario(d.getPrecioUnitario())
                        .subtotal(d.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return CarroResponse.builder()
                .carroId(carro.getId())
                .usuarioId(carro.getUsuarioId())
                .fechaCreacion(carro.getFechaCreacion())
                .total(carro.getTotal())
                .estado(carro.getEstado())
                .detalles(detalles)
                .build();
    }
}