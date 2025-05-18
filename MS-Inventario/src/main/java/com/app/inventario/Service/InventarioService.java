package com.app.inventario.Service;
import com.app.inventario.Dto.*;
import com.app.inventario.Models.*;
import com.app.inventario.Repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class InventarioService {
    private final InventarioRepository inventarioRepository;
    private final ProductoRepository productoRepository;
    private final SucursalRepository sucursalRepository;
    public ServiceResult<InventarioResponseDto> crearInventario(Long productoId, Long sucursalId, Integer cantidad) {
        List<String> errors = new ArrayList<>();

        try {
            if (cantidad == null || cantidad < 0) {
                errors.add("La cantidad debe ser mayor o igual a 0");
                return new ServiceResult<>(errors);
            }

            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            Sucursal sucursal = sucursalRepository.findById(sucursalId)
                    .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));

            Optional<Inventario> existente = inventarioRepository.findByProductoAndSucursal(producto, sucursal);
            if (existente.isPresent()) {
                errors.add("Ya existe un registro de inventario para este producto y sucursal");
                return new ServiceResult<>(errors);
            }

            Inventario inventario = new Inventario();
            inventario.setProducto(producto);
            inventario.setSucursal(sucursal);
            inventario.setCantidad(cantidad);
            inventario.setUltimaActualizacion(LocalDateTime.now());

            Inventario inventarioGuardado = inventarioRepository.save(inventario);

            InventarioResponseDto responseDto = InventarioResponseDto.fromEntity(inventarioGuardado);

            return new ServiceResult<>(responseDto);

        } catch (RuntimeException e) {
            errors.add(e.getMessage());
            return new ServiceResult<>(errors);
        } catch (Exception e) {
            errors.add("Error al crear inventario: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
    }




    public ServiceResult<Inventario> actualizarStock(Long productoId, Long sucursalId, Integer cantidad) {
        List<String> errors = new ArrayList<>();
        try {
            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            Sucursal sucursal = sucursalRepository.findById(sucursalId)
                    .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));

            Inventario inventario = inventarioRepository
                    .findByProductoAndSucursal(producto, sucursal)
                    .orElse(new Inventario());

            inventario.setProducto(producto);
            inventario.setSucursal(sucursal);
            inventario.setCantidad(cantidad);
            inventario.setUltimaActualizacion(LocalDateTime.now());

            return new ServiceResult<>(inventarioRepository.save(inventario));

        } catch(Exception e) {
            errors.add("Error: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
    }

    public ServiceResult<List<InventarioResponseDto>> obtenerProductosBajoStock() {
        try {
            List<Inventario> bajoStock = inventarioRepository.findProductosBajoStock();

            // Mapear cada Inventario a InventarioResponseDto
            List<InventarioResponseDto> dtos = bajoStock.stream()
                    .map(InventarioResponseDto::fromEntity)
                    .toList();

            return new ServiceResult<>(dtos);
        } catch (Exception e) {
            return new ServiceResult<>(List.of("Error al obtener productos bajo stock: " + e.getMessage()));
        }
    }


    public ServiceResult<Inventario> transferirStock(Long productoId, Long origenId, Long destinoId, Integer cantidad) {
        List<String> errors = new ArrayList<>();
        try {
            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            Sucursal sucursalOrigen = sucursalRepository.findById(origenId)
                    .orElseThrow(() -> new RuntimeException("Sucursal origen no encontrada"));

            Sucursal sucursalDestino = sucursalRepository.findById(destinoId)
                    .orElseThrow(() -> new RuntimeException("Sucursal destino no encontrada"));

            Inventario inventarioOrigen = inventarioRepository
                    .findByProductoAndSucursal(producto, sucursalOrigen)
                    .orElseThrow(() -> new RuntimeException("No existe stock en sucursal origen"));

            if(inventarioOrigen.getCantidad() < cantidad) {
                throw new RuntimeException("Stock insuficiente en sucursal origen");
            }

            // Restar de origen
            inventarioOrigen.setCantidad(inventarioOrigen.getCantidad() - cantidad);
            inventarioRepository.save(inventarioOrigen);

            // Sumar a destino
            Inventario inventarioDestino = inventarioRepository
                    .findByProductoAndSucursal(producto, sucursalDestino)
                    .orElse(new Inventario());

            inventarioDestino.setProducto(producto);
            inventarioDestino.setSucursal(sucursalDestino);
            inventarioDestino.setCantidad(
                    Optional.ofNullable(inventarioDestino.getCantidad()).orElse(0) + cantidad
            );
            inventarioDestino.setUltimaActualizacion(LocalDateTime.now());

            return new ServiceResult<>(inventarioRepository.save(inventarioDestino));

        } catch (RuntimeException e) {
            errors.add(e.getMessage());
            return new ServiceResult<>(errors);
        } catch (Exception e) {
            errors.add("Error en transferencia: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
    }

    public ServiceResult<Inventario> ajustarStock(Long productoId, Long sucursalId, Integer ajuste) {
        try {
            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            Sucursal sucursal = sucursalRepository.findById(sucursalId)
                    .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));

            Inventario inventario = inventarioRepository
                    .findByProductoAndSucursal(producto, sucursal)
                    .orElse(new Inventario());

            int nuevoStock = Optional.ofNullable(inventario.getCantidad()).orElse(0) + ajuste;
            inventario.setCantidad(Math.max(nuevoStock, 0));  // No permitir valores negativos

            return new ServiceResult<>(inventarioRepository.save(inventario));

        } catch (RuntimeException e) {
            return new ServiceResult<>(List.of(e.getMessage()));
        } catch (Exception e) {
            return new ServiceResult<>(List.of("Error crítico en ajuste: " + e.getMessage()));
        }
    }

    public ServiceResult<List<Inventario>> obtenerStockPorProducto(Long productoId) {
        try {
            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            return new ServiceResult<>(inventarioRepository.findByProducto(producto));
        } catch (Exception e) {
            return new ServiceResult<>(List.of("Error: " + e.getMessage()));
        }
    }

    public ServiceResult<List<Inventario>> obtenerStockPorSucursal(Long sucursalId) {
        try {
            Sucursal sucursal = sucursalRepository.findById(sucursalId)
                    .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));

            return new ServiceResult<>(inventarioRepository.findBySucursal(sucursal));
        } catch (Exception e) {
            return new ServiceResult<>(List.of("Error: " + e.getMessage()));
        }
    }
}