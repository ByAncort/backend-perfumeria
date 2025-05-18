package com.app.productos.Service;

import com.app.productos.Dto.ProductoDto;
import com.app.productos.Dto.ProveedorResponse;
import com.app.productos.Models.Categoria;
import com.app.productos.Models.Producto;
import com.app.productos.Models.ServiceResult;
import com.app.productos.Repository.CategoriaRepository;
import com.app.productos.Repository.ProductoRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final RestTemplate restTemplate;

    public ServiceResult<List<ProductoDto>> listarTodos() {
        List<ProductoDto> productos = productoRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return new ServiceResult<>(productos);
    }

    public ServiceResult<ProductoDto> buscarPorId(Long id) {
        Optional<Producto> producto = productoRepository.findById(id);
        if (producto.isEmpty()) {
            return new ServiceResult<>(List.of("Producto no encontrado"));
        }
        return new ServiceResult<>(mapToDto(producto.get()));
    }

    public ServiceResult<ProductoDto> crearProducto(@Valid ProductoDto dto) {
        List<String> errors = new ArrayList<>();

        if (productoRepository.existsByCodigoSku(dto.getCodigoSku())) {
            errors.add("El SKU ya está registrado");
        }

        Optional<Categoria> categoria = categoriaRepository.findByNombre(dto.getCategoriaNombre());
        if (categoria.isEmpty()) {
            errors.add("Categoría no encontrada");
        }

        if (!errors.isEmpty()) {
            return new ServiceResult<>(errors);
        }

        Producto producto = mapToEntity(dto);
        producto.setCategoria(categoria.get());
        return new ServiceResult<>(mapToDto(productoRepository.save(producto)));
    }

    public ServiceResult<ProductoDto> actualizarProducto(Long id, @Valid ProductoDto dto) {
        List<String> errors = new ArrayList<>();

        Optional<Producto> productoExistente = productoRepository.findById(id);
        if (productoExistente.isEmpty()) {
            errors.add("Producto no encontrado");
        }

        Optional<Categoria> categoria = categoriaRepository.findByNombre(dto.getCategoriaNombre());
        if (categoria.isEmpty()) {
            errors.add("Categoría no encontrada");
        }

        if (!errors.isEmpty()) {
            return new ServiceResult<>(errors);
        }

        Producto p = productoExistente.get();
        p.setNombre(dto.getNombre());
        p.setDescripcion(dto.getDescripcion());
        p.setPrecio(dto.getPrecio());
        p.setCosto(dto.getCosto());

        p.setCategoria(categoria.get());

        return new ServiceResult<>(mapToDto(productoRepository.save(p)));
    }

    public ServiceResult<Void> eliminarProducto(Long id) {
        Optional<Producto> producto = productoRepository.findById(id);
        if (producto.isEmpty()) {
            return new ServiceResult<>(List.of("Producto no encontrado"));
        }
        productoRepository.delete(producto.get());
        return new ServiceResult<>((Void) null);
    }

    public ServiceResult<ProductoDto> desactivarProducto(Long id) {
        Optional<Producto> producto = productoRepository.findById(id);
        if (producto.isEmpty()) {
            return new ServiceResult<>(List.of("Producto no encontrado"));
        }
        Producto p = producto.get();
        p.setActivo(false);
        return new ServiceResult<>(mapToDto(productoRepository.save(p)));
    }

    public ServiceResult<List<ProductoDto>> buscarPorCategoria(Long categoriaId) {
        Optional<Categoria> categoria = categoriaRepository.findById(categoriaId);
        if (categoria.isEmpty()) {
            return new ServiceResult<>(List.of("Categoría no encontrada"));
        }
        List<ProductoDto> productos = productoRepository.findByCategoria(categoria.get())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return new ServiceResult<>(productos);
    }

    public ServiceResult<ProductoDto> agregarProveedor(Long productoId, Long proveedorId) {
        List<String> errors = new ArrayList<>();

        Optional<Producto> producto = productoRepository.findById(productoId);
        if (producto.isEmpty()) {
            errors.add("Producto no encontrado");
            return new ServiceResult<>(errors);
        }

        // Verificar existencia del proveedor
        String proveedorUrl = "http://localhost:9013/api/v1/proveedores/" + proveedorId;
        try {
            ResponseEntity<ProveedorResponse> response = restTemplate.getForEntity(
                    proveedorUrl,
                    ProveedorResponse.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                errors.add("Proveedor no encontrado");
            }
        } catch (RestClientException e) {
            errors.add("Error al validar proveedor: " + e.getMessage());
        }

        if (!errors.isEmpty()) {
            return new ServiceResult<>(errors);
        }

        Producto p = producto.get();
        if (!p.getProveedorIds().contains(proveedorId)) {
            p.getProveedorIds().add(proveedorId);
        } else {
            errors.add("El proveedor ya está asociado al producto");
            return new ServiceResult<>(errors);
        }

        return new ServiceResult<>(mapToDto(productoRepository.save(p)));
    }


    public ServiceResult<ProductoDto> eliminarProveedor(Long productoId, Long proveedorId) {
        Optional<Producto> producto = productoRepository.findById(productoId);
        if (producto.isEmpty()) {
            return new ServiceResult<>(List.of("Producto no encontrado"));
        }
        Producto p = producto.get();
        p.getProveedorIds().remove(proveedorId);
        return new ServiceResult<>(mapToDto(productoRepository.save(p)));
    }

    private ProductoDto mapToDto(Producto p) {
        return ProductoDto.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .precio(p.getPrecio())
                .costo(p.getCosto())
                .serial(p.getSerial())
                .catalogo(p.getCatalogo())
                .codigoSku(p.getCodigoSku())
                .activo(true)
                .categoriaNombre(p.getCategoria() != null ? p.getCategoria().getNombre() : null)
                .proveedorIds(p.getProveedorIds())
                .build();
    }

    private Producto mapToEntity(ProductoDto dto) {
        return Producto.builder()
                .id(dto.getId())
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .precio(dto.getPrecio())
                .costo(dto.getCosto())
                .serial(dto.getSerial())
                .catalogo(dto.getCatalogo())
                .codigoSku(dto.getCodigoSku())
                .activo(dto.isActivo())
                .proveedorIds(dto.getProveedorIds())
                .build();
    }
}
