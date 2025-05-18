package com.app.ventas.Service;

import com.app.ventas.Dto.*;
import com.app.ventas.Models.Venta;
import com.app.ventas.Models.DetalleVenta;
import com.app.ventas.Repository.VentaRepository;
import com.app.ventas.shared.MicroserviceClient;
import com.app.ventas.shared.TokenContext;
import lombok.RequiredArgsConstructor;
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
public class VentaService {

    private final VentaRepository ventaRepository;
    private final MicroserviceClient microserviceClient;

    // Métodos existentes
    public InventarioResponse reducirCantidad(Long id) {
        String token = TokenContext.getToken();
        String url = "http://localhost:9017/api/inventario/"+id+"/vender";
        ResponseEntity<InventarioResponse> response = microserviceClient.enviarConToken(
                url,
                HttpMethod.POST,
                null,
                InventarioResponse.class,
                token
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Error al reducir cantidad en inventario");
        }

        return response.getBody();
    }
    public InventarioResponse cancelarVenta(Long id) {
        String token = TokenContext.getToken();
        String url = "http://localhost:9017/api/inventario/"+id+"/cancelar-venta";
        ResponseEntity<InventarioResponse> response = microserviceClient.enviarConToken(
                url,
                HttpMethod.POST,
                null,
                InventarioResponse.class,
                token
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Error al reducir cantidad en inventario");
        }

        return response.getBody();
    }

    public InventarioDto BuscarporID(Long inventarioId) {
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
            throw new RuntimeException("Error al obtener registro de inventario");
        }

        return response.getBody();
    }

    // Nuevos métodos CRUD

    @Transactional
    public ServiceResult<VentaResponse> crearVenta(VentaRequest request) {

        List<String> errors = new ArrayList<>();
        List<DetalleVenta> detallesEntidad = new ArrayList<>();
        double totalVenta = 0.0;

        // 1. Validar y procesar cada detalle
        for (VentaRequest.DetalleVentaRequest dvReq : request.getDetalles()) {

            InventarioDto inventario = null;
            try {
                inventario = BuscarporID(dvReq.getInventarioId());
            } catch (Exception ex) {
                errors.add("Error al consultar inventario " + dvReq.getInventarioId() + ": " + ex.getMessage());
                continue; // pasa al siguiente ítem, aún queremos acumular errores
            }

            if (inventario == null) {
                errors.add("Inventario " + dvReq.getInventarioId() + " no existe");
                continue;
            }

            if (inventario.getCantidad() < dvReq.getCantidad()) {
                errors.add("Stock insuficiente para producto "
                        + inventario.getProducto().getNombre());
                continue;
            }

            // Reducir inventario
            try {
                reducirCantidad(inventario.getId());
            } catch (Exception ex) {
                errors.add("No se pudo descontar stock del inventario " + inventario.getId()
                        + ": " + ex.getMessage());
                continue;
            }

            // Calcular subtotal
            double precioUnitario = inventario.getProducto().getPrecio();
            double subtotal = precioUnitario * dvReq.getCantidad();
            totalVenta += subtotal;

            // Construir detalle entidad
            DetalleVenta detalle = DetalleVenta.builder()
                    .productoId(inventario.getProducto().getId())
                    .cantidad(dvReq.getCantidad())
                    .precioUnitario(precioUnitario)
                    .subtotal(subtotal)
                    .build();

            detallesEntidad.add(detalle);
        }

        // Si hubo errores que impidan continuar, retornar sin guardar
        if (!errors.isEmpty()) {
            return new ServiceResult<>(errors);
        }

        // 2. Construir y guardar la venta
        Venta venta = Venta.builder()
                .sucursalId(request.getSucursalId())
                .clienteId(request.getClienteId())
                .fechaVenta(LocalDateTime.now())
                .total(totalVenta)
                .estado("COMPLETADA")
                .detalles(detallesEntidad)
                .build();

        detallesEntidad.forEach(d -> d.setVenta(venta));

        Venta ventaGuardada;
        try {
            ventaGuardada = ventaRepository.save(venta);
        } catch (Exception ex) {
            errors.add("Error al guardar la venta en base de datos: " + ex.getMessage());
            return new ServiceResult<>(errors);
        }

        // 3. Construir DTO de respuesta
        List<VentaResponse.DetalleResponse> detalleResponses = ventaGuardada.getDetalles().stream()
                .map(d -> VentaResponse.DetalleResponse.builder()
                        .productoId(d.getProductoId())
                        .cantidad(d.getCantidad())
                        .precioUnitario(d.getPrecioUnitario())
                        .subtotal(d.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        VentaResponse response = VentaResponse.builder()
                .ventaId(ventaGuardada.getId())
                .clienteId(ventaGuardada.getClienteId())
                .sucursalId(ventaGuardada.getSucursalId())
                .fechaVenta(ventaGuardada.getFechaVenta())
                .total(ventaGuardada.getTotal())
                .detalles(detalleResponses)
                .build();

        return new ServiceResult<>(response);
    }



    @Transactional
    public ServiceResult<Void> cancelarVentaMetodo(Long ventaId) {

        List<String> errors = new ArrayList<>();

        Venta venta = ventaRepository.findById(ventaId).orElse(null);
        if (venta == null) {
            errors.add("La venta " + ventaId + " no existe");
            return new ServiceResult<>(errors);
        }

        if ("ANULADA".equalsIgnoreCase(venta.getEstado())) {
            errors.add("La venta " + ventaId + " ya está anulada");
            return new ServiceResult<>(errors);
        }

        for (DetalleVenta det : venta.getDetalles()) {
            try {
                cancelarVenta(det.getProductoId());
            } catch (Exception ex) {
                errors.add("No se pudo reponer stock del producto " + det.getProductoId()
                        + ": " + ex.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            return new ServiceResult<>(errors);
        }

        venta.setEstado("ANULADA");
        try {
            ventaRepository.save(venta);
        } catch (Exception ex) {
            errors.add("Error al actualizar la venta en BD: " + ex.getMessage());
            return new ServiceResult<>(errors);
        }

        return new ServiceResult<>((Void) null);
    }
    @Transactional(readOnly = true)
    public ServiceResult<List<VentaResponse>> listarVentas() {

        List<VentaResponse> respuestas = ventaRepository.findAll().stream()
                .map(v -> VentaResponse.builder()
                        .ventaId(v.getId())
                        .sucursalId(v.getSucursalId())
                        .clienteId(v.getClienteId())
                        .fechaVenta(v.getFechaVenta())
                        .total(v.getTotal())
                        .detalles(
                                v.getDetalles().stream()
                                        .map(d -> VentaResponse.DetalleResponse.builder()
                                                .productoId(d.getProductoId())
                                                .cantidad(d.getCantidad())
                                                .precioUnitario(d.getPrecioUnitario())
                                                .subtotal(d.getSubtotal())
                                                .build())
                                        .collect(Collectors.toList())
                        )
                        .build())
                .collect(Collectors.toList());

        return new ServiceResult<>(respuestas);
    }

    @Transactional(readOnly = true)
    public ServiceResult<VentaResponse> obtenerVentaPorId(Long ventaId) {
        List<String> errors = new ArrayList<>();

        Venta venta = ventaRepository.findById(ventaId).orElse(null);

        if (venta == null) {
            errors.add("Venta con ID " + ventaId + " no encontrada");
            return new ServiceResult<>(errors);
        }

        // Mapear la entidad Venta a VentaResponse
        List<VentaResponse.DetalleResponse> detallesResponse = venta.getDetalles().stream()
                .map(d -> VentaResponse.DetalleResponse.builder()
                        .productoId(d.getProductoId())
                        .cantidad(d.getCantidad())
                        .precioUnitario(d.getPrecioUnitario())
                        .subtotal(d.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        VentaResponse response = VentaResponse.builder()
                .ventaId(venta.getId())
                .clienteId(venta.getClienteId())
                .sucursalId(venta.getSucursalId())
                .fechaVenta(venta.getFechaVenta())
                .total(venta.getTotal())
//                .estado(venta.getEstado())
                .detalles(detallesResponse)
                .build();

        return new ServiceResult<>(response);
    }

}