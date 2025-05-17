package com.app.inventario.Controller;

import com.app.inventario.Dto.ProductoDto;
import com.app.inventario.Dto.ServiceResult;
import com.app.inventario.Models.Producto;
import com.app.inventario.Service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {
    private final ProductoService productoService;

    @PostMapping
    public ResponseEntity<?> crearProducto(@Valid @RequestBody ProductoDto dto) {
        ServiceResult<ProductoDto> result = productoService.crearProducto(dto);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping("/{serial}")
    public ResponseEntity<?> buscarPorSerial(@PathVariable String serial) {
        ServiceResult<ProductoDto> result = productoService.buscarPorSerial(serial);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    @GetMapping
    public ResponseEntity<?> listarTodos() {
        ServiceResult<List<ProductoDto>> result = productoService.listarTodos();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    @PutMapping("/{serial}/stock")
    public ResponseEntity<?> aumentarStock(
            @PathVariable String serial,
            @RequestBody Map<String, Integer> request) {
        ServiceResult<ProductoDto> result = productoService.aumentarStock(serial, request.get("cantidad"));
        return handleResult(result);
    }
    @PutMapping("/{serial}")
    public ResponseEntity<?> actualizarProducto(
            @PathVariable String serial,
            @Valid @RequestBody ProductoDto dto) {
        ServiceResult<ProductoDto> result = productoService.actualizarProducto(serial, dto);
        return handleResult(result);
    }

    private ResponseEntity<?> handleResult(ServiceResult<?> result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }
        return ResponseEntity.ok(result.getData());
    }
}