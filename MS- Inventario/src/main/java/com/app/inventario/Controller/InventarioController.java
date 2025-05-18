package com.app.inventario.Controller;

import com.app.inventario.Dto.*;
import com.app.inventario.Service.InventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;

    @PostMapping
    public ResponseEntity<InventarioResponse> registrarInventario(@RequestBody InventarioRequest request) {
        return ResponseEntity.ok(inventarioService.registrarInventario(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventarioResponse> obtenerInventario(@PathVariable Long id) {
        return ResponseEntity.ok(inventarioService.obtenerInventarioPorId(id));
    }

    @GetMapping("/sucursal/{sucursalId}")
    public ResponseEntity<List<InventarioResponse>> obtenerPorSucursal(@PathVariable Long sucursalId) {
        return ResponseEntity.ok(inventarioService.obtenerInventarioPorSucursal(sucursalId));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<InventarioResponse> actualizarStock(
            @PathVariable Long id,
            @RequestParam Integer cantidad) {
        return ResponseEntity.ok(inventarioService.actualizarStock(id, cantidad));
    }

    @PostMapping("/transferencia")
    public ResponseEntity<InventarioResponse> transferirStock(
            @RequestParam Long productoId,
            @RequestParam Long origenId,
            @RequestParam Long destinoId,
            @RequestParam Integer cantidad) {
        return ResponseEntity.ok(
                inventarioService.transferirStock(origenId, destinoId, productoId, cantidad)
        );
    }

    @GetMapping("/bajo-stock")
    public ResponseEntity<List<InventarioResponse>> obtenerProductosBajoStock() {
        return ResponseEntity.ok(inventarioService.obtenerProductosBajoStockMinimo());
    }
}