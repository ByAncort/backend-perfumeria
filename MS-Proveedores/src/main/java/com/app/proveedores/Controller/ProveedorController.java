package com.app.proveedores.Controller;

import com.app.proveedores.Dto.ProveedorDto;
import com.app.proveedores.Models.ServiceResult;
import com.app.proveedores.Service.ProveedorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorService proveedorService;

    @GetMapping
    public ResponseEntity<?> listarProveedores() {
        ServiceResult<List<ProveedorDto>> result = proveedorService.listarTodos();
        return ResponseEntity.ok(result.getData());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerProveedor(@PathVariable Long id) {
        ServiceResult<ProveedorDto> result = proveedorService.buscarPorId(id);
        return handleServiceResult(result);
    }

    @PostMapping
    public ResponseEntity<?> crearProveedor(@Valid @RequestBody ProveedorDto proveedorDto) {
        ServiceResult<ProveedorDto> result = proveedorService.crearProveedor(proveedorDto);
        return result.hasErrors() ?
                ResponseEntity.badRequest().body(result.getErrors()) :
                ResponseEntity.status(HttpStatus.CREATED).body(result.getData());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProveedor(
            @PathVariable Long id,
            @Valid @RequestBody ProveedorDto proveedorDto) {
        ServiceResult<ProveedorDto> result = proveedorService.actualizarProveedor(id, proveedorDto);
        return handleServiceResult(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarProveedor(@PathVariable Long id) {
        ServiceResult<String> result = proveedorService.eliminarProveedor(id);
        return result.hasErrors() ?
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(result.getErrors()) :
                ResponseEntity.noContent().build();
    }

//    @PostMapping("/buscar-por-ids")
//    public ResponseEntity<?> buscarProveedoresPorIds(@RequestBody List<Long> ids) {
//        ServiceResult<List<ProveedorDto>> result = proveedorService.buscarPorIds(ids);
//        return ResponseEntity.ok(result.getData());
//    }

    private ResponseEntity<?> handleServiceResult(ServiceResult<?> result) {
        return result.hasErrors() ?
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(result.getErrors()) :
                ResponseEntity.ok(result.getData());
    }
}
