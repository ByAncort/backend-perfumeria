package com.app.producto.Controller;

import com.app.producto.Dto.ProductoDto;
import com.app.producto.Dto.ServiceResult;
import com.app.producto.Service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @PostMapping("create")
    public ResponseEntity<?> crearProducto(@Valid @RequestBody ProductoDto dto) {
        ServiceResult<ProductoDto> result = productoService.crearProducto(dto);
        return handleResult(result, HttpStatus.CREATED);
    }

    @GetMapping("list")
    public ResponseEntity<?> listar() {
        ServiceResult<List<ProductoDto>> result = productoService.listarProductos();
        return handleResult(result, HttpStatus.OK);
    }

    @GetMapping("get/{id}")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        ServiceResult<ProductoDto> result = productoService.obtenerProducto(id);
        return handleResult(result, HttpStatus.OK);
    }

    @PutMapping("update/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @Valid @RequestBody ProductoDto request) {
        ServiceResult<ProductoDto> result = productoService.actualizarProducto(id, request);
        return handleResult(result, HttpStatus.OK);
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        ServiceResult<Void> result = productoService.eliminarProducto(id);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<?> handleResult(ServiceResult<?> result, HttpStatus successStatus) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }
        return ResponseEntity.status(successStatus).body(result.getData());
    }
}
