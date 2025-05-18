package com.app.ventas.Controller;

import com.app.ventas.Dto.ServiceResult;
import com.app.ventas.Dto.VentaRequestDto;
import com.app.ventas.Dto.VentaResponseDto;
import com.app.ventas.Service.VentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;

    @PostMapping
    public ResponseEntity<?> crearVenta(@RequestBody VentaRequestDto ventaDto) {
        ServiceResult<VentaResponseDto> result = ventaService.realizarVenta(ventaDto);
        if(result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result.getData());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerVenta(@PathVariable Long id) {
        ServiceResult<VentaResponseDto> result = ventaService.obtenerVentaPorId(id);
        if(result.hasErrors()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result.getData());
    }
}