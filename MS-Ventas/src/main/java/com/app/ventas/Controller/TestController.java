package com.app.ventas.Controller;

import com.app.ventas.Dto.ProveedorResponse;
import com.app.ventas.Service.VentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ms-inventario/proveedor")
@RequiredArgsConstructor
public class TestController {

    private final VentaService proveedorService;

    @GetMapping("/{proveedorId}")
    public ResponseEntity<ProveedorResponse> obtenerProveedor(@PathVariable Long proveedorId) {
        ProveedorResponse response = proveedorService.consultarProveedor(proveedorId);
        return ResponseEntity.ok(response);
    }
}
