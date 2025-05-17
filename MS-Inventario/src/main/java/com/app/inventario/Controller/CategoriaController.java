package com.app.inventario.Controller;

import com.app.inventario.Dto.CategoriaDto;
import com.app.inventario.Dto.ServiceResult;
import com.app.inventario.Models.Categoria;
import com.app.inventario.Service.CategoriaService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("api/v1/categoria")
@RequiredArgsConstructor
public class CategoriaController {
    private final CategoriaService categoriaService;

    @PostMapping
    public ResponseEntity<?> createCategoria(@Valid @RequestBody CategoriaDto request) { // Add @Valid
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
}
