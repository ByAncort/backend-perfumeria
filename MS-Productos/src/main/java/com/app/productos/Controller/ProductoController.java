package com.app.productos.Controller;

import com.app.productos.Dto.ProductoDto;
import com.app.productos.Models.ServiceResult;
import com.app.productos.Service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    public ResponseEntity<?> listarProductos() {
        ServiceResult<List<ProductoDto>> result = productoService.listarTodos();
        return ResponseEntity.ok(result.getData());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerProducto(@PathVariable Long id) {
        ServiceResult<ProductoDto> result = productoService.buscarPorId(id);
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result.getErrors());
        }
        return ResponseEntity.ok(result.getData());
    }

    @PostMapping
    public ResponseEntity<?> crearProducto(@Valid @RequestBody ProductoDto producto) {
        ServiceResult<ProductoDto> result = productoService.crearProducto(producto);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result.getData());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProducto(
            @PathVariable Long id,
            @Valid @RequestBody ProductoDto producto) {
        ServiceResult<ProductoDto> result = productoService.actualizarProducto(id, producto);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }
        return ResponseEntity.ok(result.getData());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarProducto(@PathVariable Long id) {
        ServiceResult<Void> result = productoService.eliminarProducto(id);
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result.getErrors());
        }
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<?> desactivarProducto(@PathVariable Long id) {
        ServiceResult<ProductoDto> result = productoService.desactivarProducto(id);
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result.getErrors());
        }
        return ResponseEntity.ok(result.getData());
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<?> buscarPorCategoria(@PathVariable Long categoriaId) {
        ServiceResult<List<ProductoDto>> result = productoService.buscarPorCategoria(categoriaId);
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result.getErrors());
        }
        return ResponseEntity.ok(result.getData());
    }

    @PutMapping("/{id}/proveedores")
    public ResponseEntity<?> agregarProveedor(
            @PathVariable Long id,
            @RequestParam Long proveedorId) {
        ServiceResult<ProductoDto> result = productoService.agregarProveedor(id, proveedorId);
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result.getErrors());
        }
        return ResponseEntity.ok(result.getData());
    }

    @DeleteMapping("/{id}/proveedores")
    public ResponseEntity<?> eliminarProveedor(
            @PathVariable Long id,
            @RequestParam Long proveedorId) {
        ServiceResult<ProductoDto> result = productoService.eliminarProveedor(id, proveedorId);
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result.getErrors());
        }
        return ResponseEntity.ok(result.getData());
    }
}
