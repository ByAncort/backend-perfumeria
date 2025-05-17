package com.app.inventario.Controller;

import com.app.inventario.Dto.ServiceResult;
import com.app.inventario.Models.Inventario;
import com.app.inventario.Service.InventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class InventarioController {
    private final InventarioService inventarioService;

    @PostMapping("/stock")
    public ResponseEntity<?> actualizarStock(
            @RequestParam Long productoId,
            @RequestParam Long sucursalId,
            @RequestParam Integer cantidad
    ) {
        ServiceResult<Inventario> result = inventarioService
                .actualizarStock(productoId, sucursalId, cantidad);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/bajo-stock")
    public ResponseEntity<?> obtenerProductosBajoStock() {
        ServiceResult<List<Inventario>> result = inventarioService.obtenerProductosBajoStock();
        return handleResult(result, HttpStatus.OK);
    }

    @PostMapping("/transferencia")
    public ResponseEntity<?> transferirStock(
            @RequestParam Long productoId,
            @RequestParam Long origenId,
            @RequestParam Long destinoId,
            @RequestParam Integer cantidad
    ) {
        ServiceResult<Inventario> result = inventarioService
                .transferirStock(productoId, origenId, destinoId, cantidad);
        return handleResult(result, HttpStatus.OK);
    }

    @PutMapping("/ajuste")
    public ResponseEntity<?> ajustarStock(
            @RequestParam Long productoId,
            @RequestParam Long sucursalId,
            @RequestParam Integer ajuste
    ) {
        ServiceResult<Inventario> result = inventarioService
                .ajustarStock(productoId, sucursalId, ajuste);
        return handleResult(result, HttpStatus.OK);
    }

    @GetMapping("/producto/{productoId}")
    public ResponseEntity<?> obtenerStockPorProducto(@PathVariable Long productoId) {
        ServiceResult<List<Inventario>> result = inventarioService
                .obtenerStockPorProducto(productoId);
        return handleResult(result, HttpStatus.OK);
    }

    @GetMapping("/sucursal/{sucursalId}")
    public ResponseEntity<?> obtenerStockPorSucursal(@PathVariable Long sucursalId) {
        ServiceResult<List<Inventario>> result = inventarioService
                .obtenerStockPorSucursal(sucursalId);
        return handleResult(result, HttpStatus.OK);
    }

    // Método helper para manejar todas las respuestas
    private ResponseEntity<?> handleResult(ServiceResult<?> result, HttpStatus successStatus) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }
        return ResponseEntity.status(successStatus).body(result.getData());
    }
}