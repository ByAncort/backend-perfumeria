package com.app.inventario.Service;

import com.app.inventario.Dto.*;
import com.app.inventario.Models.*;
import com.app.inventario.Repository.CategoriaRepository;
import com.app.inventario.Repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoService {
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private static final Logger logger = LoggerFactory.getLogger(ProductoService.class);


    private Producto toEntity(ProductoDto dto) {
        if (dto == null) return null;

        return Producto.builder()
                .codigoSku(dto.getCodigoSku())
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .precio(dto.getPrecio())
                .costo(dto.getCosto())
                .catalogo(dto.getCatalogo())
                .serial(dto.getSerial())
                .build();
    }


    private ProductoDto toDto(Producto producto) {
        if (producto == null) return null;

        return ProductoDto.builder()
                .codigoSku(producto.getCodigoSku())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .precio(producto.getPrecio())
                .costo(producto.getCosto())
                .catalogo(producto.getCatalogo())
                .serial(producto.getSerial())
                .stock(producto.getStock())
                .categoriaId(producto.getCategoria() != null ? producto.getCategoria().getId() : null)
                .build();
    }

    public ServiceResult<ProductoDto> crearProducto(ProductoDto dto) {
        List<String> errors = new ArrayList<>();
        try {
            // Validaciones
            if(dto.getCodigoSku() == null || dto.getCodigoSku().isBlank()) {
                errors.add("El SKU es obligatorio");
            }

            if(productoRepository.existsByCodigoSku(dto.getCodigoSku())) {
                errors.add("El SKU ya existe");
            }

            if(dto.getSerial() != null && productoRepository.existsBySerial(dto.getSerial())) {
                errors.add("El serial ya está registrado");
            }

            Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

            if(!errors.isEmpty()) {
                return new ServiceResult<>(errors);
            }

            Producto producto = toEntity(dto);
            producto.setCategoria(categoria);
            productoRepository.save(producto);
            return new ServiceResult<>(toDto(producto));

        } catch(Exception e) {
            errors.add("Error: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
    }

    public ServiceResult<ProductoDto> buscarPorSerial(String serial) {
        List<String> errors = new ArrayList<>();

        try {
            if (serial == null || serial.isBlank()) {
                errors.add("El serial no puede estar vacío");
                return new ServiceResult<>(errors);
            }

            Producto producto = productoRepository.findBySerial(serial)
                    .orElseThrow(() -> new RuntimeException("Serial no registrado"));

            if (!producto.getActivo()) {
                errors.add("Producto no disponible");
                return new ServiceResult<>(errors);
            }

            return new ServiceResult<>(toDto(producto));

        } catch (RuntimeException e) {
            errors.add(e.getMessage());
            return new ServiceResult<>(errors);
        } catch (Exception e) {
            logger.error("Error crítico al buscar serial {}", serial, e);
            errors.add("Error en el sistema");
            return new ServiceResult<>(errors);
        }
    }

    public ServiceResult<List<ProductoDto>> listarTodos() {
        try {
            List<Producto> productos = productoRepository.findAll();

            List<ProductoDto> dtoList = productos.stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());

            return new ServiceResult<>(dtoList);
        } catch (Exception e) {
            logger.error("Error al listar productos", e);
            return new ServiceResult<>(List.of("Error al recuperar productos: " + e.getMessage()));
        }
    }

    public ServiceResult<ProductoDto> actualizarProducto(String serial, ProductoDto dto) {
        List<String> errors = new ArrayList<>();
        try {
            Producto producto = productoRepository.findBySerial(serial)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            validarActualizacion(producto, dto, errors);
            if (!errors.isEmpty()) return new ServiceResult<>(errors);

            actualizarCampos(producto, dto);
            productoRepository.save(producto);

            return new ServiceResult<>(toDto(producto));

        } catch (RuntimeException e) {
            errors.add(e.getMessage());
            return new ServiceResult<>(errors);
        } catch (Exception e) {
            logger.error("Error actualizando producto", e);
            return new ServiceResult<>(List.of("Error interno al actualizar"));
        }
    }

    private void validarCantidad(Integer cantidad, List<String> errors) {
        if (cantidad == null || cantidad <= 0) {
            errors.add("Cantidad debe ser un número positivo");
        }
    }

    private void validarActualizacion(Producto producto, ProductoDto dto, List<String> errors) {
        if (dto.getCodigoSku() != null &&
                !dto.getCodigoSku().equals(producto.getCodigoSku()) &&
                productoRepository.existsByCodigoSku(dto.getCodigoSku())) {
            errors.add("El nuevo SKU ya está registrado");
        }
    }

    private void actualizarCampos(Producto producto, ProductoDto dto) {
        if (dto.getNombre() != null) producto.setNombre(dto.getNombre());
        if (dto.getDescripcion() != null) producto.setDescripcion(dto.getDescripcion());
        if (dto.getPrecio() != null) producto.setPrecio(dto.getPrecio());
        if (dto.getCosto() != null) producto.setCosto(dto.getCosto());
        if (dto.getCatalogo() != null) producto.setCatalogo(dto.getCatalogo());
        if (dto.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new RuntimeException("Categoría no válida"));
            producto.setCategoria(categoria);
        }
    }

    public ServiceResult<ProductoDto> aumentarStock(String serial, Integer cantidad) {
        List<String> errors = new ArrayList<>();
        try {
            validarCantidad(cantidad, errors);
            if (!errors.isEmpty()) return new ServiceResult<>(errors);

            Producto producto = productoRepository.findBySerial(serial)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            producto.setStock(producto.getStock() + cantidad);
            productoRepository.save(producto);

            return new ServiceResult<>(toDto(producto));

        } catch (RuntimeException e) {
            errors.add(e.getMessage());
            return new ServiceResult<>(errors);
        } catch (Exception e) {
            logger.error("Error crítico al aumentar stock", e);
            return new ServiceResult<>(List.of("Error interno del servidor"));
        }
    }



}