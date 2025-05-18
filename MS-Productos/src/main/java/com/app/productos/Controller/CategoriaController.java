package com.app.productos.Controller;

import com.app.productos.Models.Categoria;
import com.app.productos.Models.ServiceResult;
import com.app.productos.Service.CategoriaService;
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

    @GetMapping
    public ResponseEntity<?> listarCategorias() {
        ServiceResult<List<Categoria>> result = categoriaService.listarTodas();
        return ResponseEntity.ok(result.getData());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerCategoria(@PathVariable Long id) {
        ServiceResult<Categoria> result = categoriaService.buscarPorId(id);
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result.getErrors());
        }
        return ResponseEntity.ok(result.getData());
    }

    @PostMapping
    public ResponseEntity<?> crearCategoria(@Valid @RequestBody Categoria categoria) {
        ServiceResult<Categoria> result = categoriaService.crearCategoria(categoria);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result.getData());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarCategoria(
            @PathVariable Long id,
            @Valid @RequestBody Categoria categoria) {
        ServiceResult<Categoria> result = categoriaService.actualizarCategoria(id, categoria);
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result.getErrors());
        }
        return ResponseEntity.ok(result.getData());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCategoria(@PathVariable Long id) {
        ServiceResult<Void> result = categoriaService.eliminarCategoria(id);
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result.getErrors());
        }
        return ResponseEntity.noContent().build();
    }
}
