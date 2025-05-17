package com.app.inventario.Controller;

import com.app.inventario.Dto.ProveedorDto;
import com.app.inventario.Dto.ServiceResult;
import com.app.inventario.Models.Proveedor;
import com.app.inventario.Service.ProveedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ms-inventario/proveedor/")
@RequiredArgsConstructor
public class ProveedorController {
    private final ProveedorService proveedorService;

    @PostMapping("create-proveedor")
    public ResponseEntity<?> createProveedor(@RequestBody ProveedorDto proveedorDto) {
        ServiceResult<Proveedor> result = proveedorService.addProveedor(proveedorDto);
        return handleServiceResult(result, HttpStatus.CREATED);
    }

    @GetMapping("all-active")
    public ResponseEntity<?> getAllProveedoresActivos() {
        ServiceResult<List<Proveedor>> result = proveedorService.getAllProveedoresActivos();
        return handleServiceResult(result, HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getProveedorById(@PathVariable Long id) {
        ServiceResult<Proveedor> result = proveedorService.getProveedorById(id);
        return handleServiceResult(result, HttpStatus.OK);
    }

    @PutMapping("update/{id}")
    public ResponseEntity<?> updateProveedor(
            @PathVariable Long id,
            @RequestBody ProveedorDto dto
    ) {
        ServiceResult<Proveedor> result = proveedorService.updateProveedor(id, dto);
        return handleServiceResult(result, HttpStatus.OK);
    }

    @PatchMapping("toggle-activo/{id}")
    public ResponseEntity<?> toggleActivoProveedor(
            @PathVariable Long id,
            @RequestParam boolean activo
    ) {
        ServiceResult<Proveedor> result = proveedorService.toggleActivoProveedor(id, activo);
        return handleServiceResult(result, HttpStatus.OK);
    }

    @GetMapping("by-rut/{rut}")
    public ResponseEntity<?> getProveedorByRut(@PathVariable String rut) {
        ServiceResult<Proveedor> result = proveedorService.getProveedorByRut(rut);
        return handleServiceResult(result, HttpStatus.OK);
    }

    // Método helper para manejar las respuestas
    private ResponseEntity<?> handleServiceResult(ServiceResult<?> result, HttpStatus successStatus) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        } else {
            return ResponseEntity.status(successStatus).body(result.getData());
        }
    }
}
