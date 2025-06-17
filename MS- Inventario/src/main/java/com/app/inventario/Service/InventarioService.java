package com.app.inventario.Service;

import com.app.inventario.Dto.*;
import com.app.inventario.Models.Inventario;
import com.app.inventario.Repository.InventarioRepository;
import com.app.inventario.shared.MicroserviceClient;
import com.app.inventario.shared.TokenContext;
import lombok.RequiredArgsConstructor;
import org.app.dto.ServiceResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventarioService {

    @Value("${auth.url.provMicro}")
    private String PRO_SERVICE_URL;

    @Value("${auth.url.sucursalMicro}")
    private String SUCURSAL_SERVICE_URL;

    private final MicroserviceClient microserviceClient;
    private final InventarioRepository inventarioRepository;


    public SucursalResponse consultarSucursal(Long id) {
        String token = TokenContext.getToken();
        String url = SUCURSAL_SERVICE_URL + "/api/sucursales/" + id;
        ResponseEntity<SucursalResponse> response = microserviceClient.enviarConToken(
                url,
                HttpMethod.GET,
                null,
                SucursalResponse.class,
                token
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Error al obtener sucursal");
        }

        return response.getBody();
    }

    public ProductoResponse consultarProducto(Long id) {
        String token = TokenContext.getToken();
        String url = PRO_SERVICE_URL + "/api/productos/get/" + id;
        ResponseEntity<ProductoResponse> response = microserviceClient.enviarConToken(
                url,
                HttpMethod.GET,
                null,
                ProductoResponse.class,
                token
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Error al obtener producto");
        }

        return response.getBody();
    }



    @Transactional
    public InventarioResponse registrarInventario(InventarioRequest request) {

        ProductoResponse producto = consultarProducto(request.getProductoId());
        SucursalResponse sucursal = consultarSucursal(request.getSucursalId());

        Inventario inventario = Inventario.builder()
                .productoId(request.getProductoId())
                .sucursalId(request.getSucursalId())
                .cantidad(request.getCantidad())
                .stockMinimo(request.getStockMinimo() != null ? request.getStockMinimo() : 1)
                .build();

        inventario = inventarioRepository.save(inventario);
        return buildInventarioResponse(inventario, producto, sucursal);
    }

    public InventarioResponse obtenerInventarioPorId(Long id) {
        Inventario inventario = inventarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registro de inventario no encontrado"));

        ProductoResponse producto = consultarProducto(inventario.getProductoId());
        SucursalResponse sucursal = consultarSucursal(inventario.getSucursalId());

        return buildInventarioResponse(inventario, producto, sucursal);
    }

    public List<InventarioResponse> obtenerInventarioPorSucursal(Long sucursalId) {

        consultarSucursal(sucursalId);

        return inventarioRepository.findBySucursalId(sucursalId).stream()
                .map(inventario -> {
                    ProductoResponse producto = consultarProducto(inventario.getProductoId());
                    return buildInventarioResponse(inventario, producto, null);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public InventarioResponse actualizarStock(Long id, Integer cantidad) {
        Inventario inventario = inventarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registro de inventario no encontrado"));

        inventario.setCantidad(cantidad);
        inventario.setUltimaActualizacion(LocalDateTime.now());

        inventario = inventarioRepository.save(inventario);

        ProductoResponse producto = consultarProducto(inventario.getProductoId());
        SucursalResponse sucursal = consultarSucursal(inventario.getSucursalId());

        return buildInventarioResponse(inventario, producto, sucursal);
    }

    @Transactional
    public InventarioResponse transferirStock(Long origenId, Long destinoId, Long productoId, Integer cantidad) {

        consultarSucursal(origenId);
        consultarSucursal(destinoId);
        consultarProducto(productoId);


        Inventario inventarioOrigen = inventarioRepository.findByProductoIdAndSucursalId(productoId, origenId)
                .orElseThrow(() -> new RuntimeException("No existe inventario para el producto en la sucursal origen"));


        if (inventarioOrigen.getCantidad() < cantidad) {
            throw new RuntimeException("Stock insuficiente en sucursal origen");
        }


        Inventario inventarioDestino = inventarioRepository.findByProductoIdAndSucursalId(productoId, destinoId)
                .orElse(Inventario.builder()
                        .productoId(productoId)
                        .sucursalId(destinoId)
                        .cantidad(0)
                        .stockMinimo(1)
                        .build());


        inventarioOrigen.setCantidad(inventarioOrigen.getCantidad() - cantidad);
        inventarioDestino.setCantidad(inventarioDestino.getCantidad() + cantidad);

        inventarioRepository.save(inventarioOrigen);
        inventarioRepository.save(inventarioDestino);


        ProductoResponse producto = consultarProducto(productoId);
        SucursalResponse sucursalOrigen = consultarSucursal(origenId);
        SucursalResponse sucursalDestino = consultarSucursal(destinoId);

        return InventarioResponse.builder()
                .id(inventarioDestino.getId())
                .producto(producto)
                .sucursal(sucursalDestino)
                .cantidad(inventarioDestino.getCantidad())
                .stockMinimo(inventarioDestino.getStockMinimo())
                .ultimaActualizacion(inventarioDestino.getUltimaActualizacion())
                .transferencia(TransferenciaResponse.builder()
                        .origen(sucursalOrigen)
                        .cantidadTransferida(cantidad)
                        .build())
                .build();
    }

    public List<InventarioResponse> obtenerProductosBajoStockMinimo() {
        return inventarioRepository.findByCantidadLessThanStockMinimo().stream()
                .map(inventario -> {
                    ProductoResponse producto = consultarProducto(inventario.getProductoId());
                    SucursalResponse sucursal = consultarSucursal(inventario.getSucursalId());
                    return buildInventarioResponse(inventario, producto, sucursal);
                })
                .collect(Collectors.toList());
    }

    private InventarioResponse buildInventarioResponse(Inventario inventario,
                                                       ProductoResponse producto,
                                                       SucursalResponse sucursal) {
        return InventarioResponse.builder()
                .id(inventario.getId())
                .producto(producto)
                .sucursal(sucursal)
                .cantidad(inventario.getCantidad())
                .stockMinimo(inventario.getStockMinimo())
                .ultimaActualizacion(inventario.getUltimaActualizacion())
                .build();
    }
//    @Transactional
//    public InventarioResponse vender(Long inventarioId) {
//        Inventario inventario = inventarioRepository.findById(inventarioId)
//                .orElseThrow(() -> new RuntimeException("Registro de inventario no encontrado"));
//
//        if (inventario.getCantidad() <= 0) {
//            throw new RuntimeException("No hay stock disponible para vender");
//        }
//
//        inventario.setCantidad(inventario.getCantidad() - 1);
//        inventario.setUltimaActualizacion(LocalDateTime.now());
//
//        inventario = inventarioRepository.save(inventario);
//
//        ProductoResponse producto = consultarProducto(inventario.getProductoId());
//        SucursalResponse sucursal = consultarSucursal(inventario.getSucursalId());
//
//        return buildInventarioResponse(inventario, producto, sucursal);
//    }

    @Transactional
    public ServiceResult<InventarioResponse> vender(Long inventarioId) {
        try {
            Inventario inventario = inventarioRepository.findById(inventarioId)
                    .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

            if (inventario.getCantidad() <= 0) {
                return new ServiceResult<>(List.of("Sin stock disponible"));
            }

            // Paso 1: reservar stock
            inventario.setCantidad(inventario.getCantidad() - 1);
            inventario.setUltimaActualizacion(LocalDateTime.now());
            inventario = inventarioRepository.save(inventario);

            // Paso 2: Validar entidades externas
            ProductoResponse producto = consultarProducto(inventario.getProductoId());
            SucursalResponse sucursal = consultarSucursal(inventario.getSucursalId());

            // Paso 3: Confirmar venta
            InventarioResponse response = buildInventarioResponse(inventario, producto, sucursal);
            return new ServiceResult<>(response);

        } catch (Exception e) {
            // Acción compensatoria (rollback local)
            restaurarStock(inventarioId);
            return new ServiceResult<>(List.of("Error al procesar venta: " + e.getMessage()));
        }
    }

    private void restaurarStock(Long inventarioId) {
        inventarioRepository.findById(inventarioId).ifPresent(inv -> {
            inv.setCantidad(inv.getCantidad() + 1);
            inventarioRepository.save(inv);
        });
    }

    @Transactional
    public InventarioResponse canerlarVenta(Long inventarioId) {
        Inventario inventario = inventarioRepository.findById(inventarioId)
                .orElseThrow(() -> new RuntimeException("Registro de inventario no encontrado"));

        if (inventario.getCantidad() <= 0) {
            throw new RuntimeException("No hay stock disponible para vender");
        }

        inventario.setCantidad(inventario.getCantidad() + 1);
        inventario.setUltimaActualizacion(LocalDateTime.now());

        inventario = inventarioRepository.save(inventario);

        ProductoResponse producto = consultarProducto(inventario.getProductoId());
        SucursalResponse sucursal = consultarSucursal(inventario.getSucursalId());

        return buildInventarioResponse(inventario, producto, sucursal);
    }

}