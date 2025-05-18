package com.app.ventas.Controller;

import com.app.ventas.Dto.*;
import com.app.ventas.Service.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;
    @PostMapping
    public ResponseEntity<?> crearVenta(@Valid @RequestBody VentaRequest req) {

        ServiceResult<VentaResponse> result = ventaService.crearVenta(req);

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result.getData());
    }
    @PatchMapping("/{id}/anular")
    public ResponseEntity<?> anularVenta(@PathVariable Long id) {
        ServiceResult<Void> result = ventaService.cancelarVentaMetodo(id);

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("listar-ventas")
    public ResponseEntity<List<VentaResponse>> listarVentas() {
        return ResponseEntity.ok(ventaService.listarVentas().getData());
    }
    @GetMapping("/{ventaId}")
    public ResponseEntity<?> obtenerVentaPorId(@PathVariable Long ventaId) {
        ServiceResult<VentaResponse> result = ventaService.obtenerVentaPorId(ventaId);

        if (result.hasErrors()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result.getData());
    }

}