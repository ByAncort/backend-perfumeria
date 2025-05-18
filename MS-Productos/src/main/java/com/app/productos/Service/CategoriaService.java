package com.app.productos.Service;

import com.app.productos.Models.Categoria;
import com.app.productos.Models.ServiceResult;
import com.app.productos.Repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public ServiceResult<List<Categoria>> listarTodas() {
        return new ServiceResult<>(categoriaRepository.findAll());
    }

    public ServiceResult<Categoria> buscarPorId(Long id) {
        Optional<Categoria> categoria = categoriaRepository.findById(id);
        return categoria
                .map(ServiceResult::new)
                .orElseGet(() -> new ServiceResult<>(List.of("Categoría no encontrada")));
    }

    public ServiceResult<Categoria> crearCategoria(@Valid Categoria categoria) {
        List<String> errores = new ArrayList<>();
        if (categoriaRepository.existsByNombre(categoria.getNombre())) {
            errores.add("La categoría ya existe");
            return new ServiceResult<>(errores);
        }
        Categoria creada = categoriaRepository.save(categoria);
        return new ServiceResult<>(creada);
    }

    public ServiceResult<Categoria> actualizarCategoria(Long id, @Valid Categoria categoriaActualizada) {
        Optional<Categoria> optionalCategoria = categoriaRepository.findById(id);
        if (optionalCategoria.isEmpty()) {
            return new ServiceResult<>(List.of("Categoría no encontrada"));
        }

        Categoria existente = optionalCategoria.get();
        existente.setNombre(categoriaActualizada.getNombre());
        existente.setDescripcion(categoriaActualizada.getDescripcion());

        return new ServiceResult<>(categoriaRepository.save(existente));
    }

    public ServiceResult<Void> eliminarCategoria(Long id) {
        Optional<Categoria> optionalCategoria = categoriaRepository.findById(id);
        if (optionalCategoria.isEmpty()) {
            return new ServiceResult<>(List.of("Categoría no encontrada"));
        }
        categoriaRepository.delete(optionalCategoria.get());
        return new ServiceResult<>((Void) null); // Devuelve éxito sin errores
    }
}
