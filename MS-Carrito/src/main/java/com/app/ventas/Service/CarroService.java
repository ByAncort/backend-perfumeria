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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarroService {

    private final CarroRepository carroRepository;
    private final MicroserviceClient microserviceClient;

    // Métodos auxiliares para comunicación con otros microservicios
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

    private ServiceResult<CouponDto> validarCupon(String codigoCupon) {
        String token = TokenContext.getToken();
        String url = "http://localhost:9022/api/coupons/validate/" + codigoCupon;

        try {
            ResponseEntity<CouponDto> response = microserviceClient.enviarConToken(
                    url,
                    HttpMethod.GET,
                    null,
                    CouponDto.class,
                    token
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return new ServiceResult<>(List.of("Cupón no válido o expirado"));
            }

            return new ServiceResult<>(response.getBody());
        } catch (Exception e) {
            return new ServiceResult<>(List.of("Error al validar el cupón: " + e.getMessage()));
        }
    }

    private BigDecimal aplicarDescuento(BigDecimal total, CouponDto cupon) {
        if (cupon == null || !cupon.isValid()) {
            return total;
        }

        if ("PERCENTAGE".equalsIgnoreCase(cupon.getDiscountType())) {
            return total.multiply(BigDecimal.ONE.subtract(cupon.getDiscountValue()
                    .divide(BigDecimal.valueOf(100))));
        } else if ("FIXED".equalsIgnoreCase(cupon.getDiscountType())) {
            return total.subtract(cupon.getDiscountValue()).max(BigDecimal.ZERO);
        }
        return total;
    }

    // Métodos principales del servicio
    @Transactional
    public ServiceResult<CarroResponse> agregarProductosAlCarro(CarroRequest request) {
        List<String> errores = new ArrayList<>();
        List<DetalleCarro> detalles = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        CouponDto cuponAplicado = null;

        // Validar cupón si viene en la solicitud
        if (request.getCodigoCupon() != null && !request.getCodigoCupon().isEmpty()) {
            ServiceResult<CouponDto> resultadoCupon = validarCupon(request.getCodigoCupon());
            if (resultadoCupon.hasErrors()) {
                errores.addAll(resultadoCupon.getErrors());
            } else {
                cuponAplicado = resultadoCupon.getData();
            }
        }

        // Procesar cada producto del carrito
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

            BigDecimal precio = BigDecimal.valueOf(producto.getProducto().getPrecio());
            BigDecimal cantidad = BigDecimal.valueOf(detalleReq.getCantidad());
            BigDecimal subtotalItem = precio.multiply(cantidad);
            subtotal = subtotal.add(subtotalItem);

            DetalleCarro detalle = DetalleCarro.builder()
                    .productoId(producto.getProducto().getId())
                    .cantidad(detalleReq.getCantidad())
                    .precioUnitario(precio.doubleValue())
                    .subtotal(subtotalItem.doubleValue())
                    .build();

            detalles.add(detalle);
        }

        if (!errores.isEmpty()) {
            return new ServiceResult<>(errores);
        }

        // Calcular total con descuento
        BigDecimal totalConDescuento = aplicarDescuento(subtotal, cuponAplicado);
        BigDecimal descuento = subtotal.subtract(totalConDescuento);

        // Crear y guardar el carrito
        Carro carro = Carro.builder()
                .usuarioId(request.getUsuarioId())
                .fechaCreacion(LocalDateTime.now())
                .subtotal(subtotal.doubleValue())
                .descuento(descuento.doubleValue())
                .total(totalConDescuento.doubleValue())
                .codigoCupon(request.getCodigoCupon())
                .estado("ACTIVO")
                .build();

        Carro finalCarro = carro;
        detalles.forEach(d -> d.setCarro(finalCarro));
        carro.setDetalles(detalles);

        try {
            carro = carroRepository.save(carro);
        } catch (Exception e) {
            errores.add("Error al guardar el carro: " + e.getMessage());
            return new ServiceResult<>(errores);
        }

        return new ServiceResult<>(mapearCarroAResponse(carro));
    }

    @Transactional
    public ServiceResult<?> aplicarCuponACarro(Long carroId, String codigoCupon) {
        List<String> errores = new ArrayList<>();

        // Validar el cupón
        ServiceResult<CouponDto> resultadoCupon = validarCupon(codigoCupon);
        if (resultadoCupon.hasErrors()) {
            return resultadoCupon;
        }
        CouponDto cupon = resultadoCupon.getData();

        // Obtener el carro
        Carro carro = carroRepository.findById(carroId)
                .orElseThrow(() -> {
                    errores.add("Carro no encontrado");
                    return new RuntimeException("Carro no encontrado");
                });

        // Validar estado del carro
        if ("COMPLETADO".equalsIgnoreCase(carro.getEstado())) {
            errores.add("No se puede aplicar cupón a un carro completado");
            return new ServiceResult<>(errores);
        }

        // Calcular nuevos valores
        BigDecimal subtotal = BigDecimal.valueOf(carro.getSubtotal());
        BigDecimal totalConDescuento = aplicarDescuento(subtotal, cupon);
        BigDecimal descuento = subtotal.subtract(totalConDescuento);

        // Actualizar el carro
        carro.setTotal(totalConDescuento.doubleValue());
        carro.setDescuento(descuento.doubleValue());
        carro.setCodigoCupon(codigoCupon);

        try {
            carro = carroRepository.save(carro);
        } catch (Exception e) {
            errores.add("Error al aplicar el cupón: " + e.getMessage());
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
        carro.setSubtotal(0.0);
        carro.setDescuento(0.0);
        carro.setCodigoCupon(null);

        try {
            carroRepository.save(carro);

        } catch (Exception e) {
            errores.add("Error al vaciar el carro");
            return new ServiceResult<>(errores);
        }

        return new ServiceResult<>(null);
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

        // Validar que el carro tenga productos
        if (carro.getDetalles().isEmpty()) {
            errores.add("No se puede confirmar un carro vacío");
            return new ServiceResult<>(errores);
        }

        // Actualizar inventario para cada producto
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

        // Marcar carro como completado
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
                .subtotal(carro.getSubtotal())
                .descuento(carro.getDescuento())
                .total(carro.getTotal())
                .codigoCupon(carro.getCodigoCupon())
                .estado(carro.getEstado())
                .detalles(detalles)
                .build();
    }
}