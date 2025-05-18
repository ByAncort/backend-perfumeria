package com.app.sucursales.Controller;

import com.app.sucursales.Dto.ServiceResult;
import com.app.sucursales.Dto.SucursalDto;
import com.app.sucursales.Service.SucursalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sucursales")
@RequiredArgsConstructor
public class SucursalController {

    private final SucursalService sucursalService;

    @PostMapping
    public ResponseEntity<?> crearSucursal(@RequestBody SucursalDto dto) {
        ServiceResult<SucursalDto> result = sucursalService.crearSucursal(dto);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }
        return ResponseEntity.ok(result.getData());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerSucursal(@PathVariable Long id) {
        ServiceResult<SucursalDto> result = sucursalService.obtenerSucursalPorId(id);
        if (result.hasErrors()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result.getData());
    }

    @GetMapping
    public ResponseEntity<?> listarSucursales() {
        ServiceResult<List<SucursalDto>> result = sucursalService.listarTodasLasSucursales();
        if (result.hasErrors()) {
            return ResponseEntity.internalServerError().body(result.getErrors());
        }
        return ResponseEntity.ok(result.getData());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarSucursal(@PathVariable Long id, @RequestBody SucursalDto dto) {
        ServiceResult<SucursalDto> result = sucursalService.actualizarSucursal(id, dto);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }
        return ResponseEntity.ok(result.getData());
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstadoSucursal(@PathVariable Long id, @RequestParam boolean activa) {
        ServiceResult<Void> result = sucursalService.cambiarEstadoSucursal(id, activa);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/activas")
    public ResponseEntity<?> buscarSucursalesActivas() {
        ServiceResult<List<SucursalDto>> result = sucursalService.buscarSucursalesActivas();
        if (result.hasErrors()) {
            return ResponseEntity.internalServerError().body(result.getErrors());
        }
        return ResponseEntity.ok(result.getData());
    }
}

