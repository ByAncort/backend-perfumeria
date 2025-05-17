package com.app.inventario.Service;

import com.app.inventario.Dto.*;
import com.app.inventario.Models.*;
import com.app.inventario.Repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaService {
    private final CategoriaRepository categoriaRepository;

    public ServiceResult<CategoriaDto> crearCategoria(CategoriaDto dto) {
        List<String> errors = new ArrayList<>();
        try {
            if(categoriaRepository.existsByNombre(dto.getNombre())) {
                errors.add("La categoría ya existe");
                return new ServiceResult<>(errors);
            }

            Categoria categoria = Categoria.builder()
                    .nombre(dto.getNombre())
                    .descripcion(dto.getDescripcion())
                    .build();
            categoriaRepository.save(categoria);
            return new ServiceResult<>(dto);

        } catch(Exception e) {
            errors.add("Error: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
    }
    public ServiceResult<List<CategoriaDto>> listarCategorias() {
        List<String> errors = new ArrayList<>();
        try {
            List<Categoria> categorias = categoriaRepository.findAll();

            List<CategoriaDto> dtoList = categorias.stream()
                    .map(cat -> CategoriaDto.builder()
                            .nombre(cat.getNombre())
                            .descripcion(cat.getDescripcion())
                            .build())
                    .collect(Collectors.toList());

            return new ServiceResult<>(dtoList);

        } catch (Exception e) {
            errors.add("Error al listar categorías: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
    }

    // Otros métodos (listar, actualizar, eliminar)
}