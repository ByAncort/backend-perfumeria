package com.app.producto.Controller;


import com.app.producto.Dto.*;
import com.app.producto.Service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
public class CategoriaController {
    private final CategoriaService categoriaService;

    @PostMapping
    public ResponseEntity<?> createCategoria(@Valid @RequestBody CategoriaDto request) {
        ServiceResult<CategoriaDto> result = categoriaService.crearCategoria(request);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        } else {
            return ResponseEntity.status(HttpStatus.CREATED).body(result.getData());
        }
    }
    @GetMapping
    public ResponseEntity<?> listar(){
        ServiceResult<List<CategoriaDto>> result = categoriaService.listarCategorias();
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        } else {
            return ResponseEntity.status(HttpStatus.CREATED).body(result.getData());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @Valid @RequestBody CategoriaDto request) {
        ServiceResult<CategoriaDto> result = categoriaService.actualizarCategoria(id, request);
        return buildResponse(result, HttpStatus.OK);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        ServiceResult<Void> result = categoriaService.eliminarCategoria(id);
        // Si hay errores → 400; si todo ok → 204 No Content
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }
        return ResponseEntity.noContent().build();
    }

    private <T> ResponseEntity<?> buildResponse(ServiceResult<T> result, HttpStatus successStatus) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }
        return ResponseEntity.status(successStatus).body(result.getData());
    }
}
