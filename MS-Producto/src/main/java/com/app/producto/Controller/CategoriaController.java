package com.app.producto.Controller;

import com.app.producto.Dto.*;
import com.app.producto.Service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
@Tag(name = "Categorías", description = "API para gestión de categorías de productos")
public class CategoriaController {
    private final CategoriaService categoriaService;

    @Operation(
            summary = "Crear categoría",
            description = "Registra una nueva categoría de productos en el sistema"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Categoría creada exitosamente",
                    content = @Content(schema = @Schema(implementation = CategoriaDto.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    })
    @PostMapping
    public ResponseEntity<?> createCategoria(
            @Parameter(description = "Datos de la categoría a crear", required = true)
            @Valid @RequestBody CategoriaDto request) {
        ServiceResult<CategoriaDto> result = categoriaService.crearCategoria(request);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        } else {
            return ResponseEntity.status(HttpStatus.CREATED).body(result.getData());
        }
    }

    @Operation(
            summary = "Listar categorías",
            description = "Obtiene todas las categorías de productos registradas"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de categorías obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = CategoriaDto[].class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error al obtener las categorías",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    })
    @GetMapping
    public ResponseEntity<?> listar() {
        ServiceResult<List<CategoriaDto>> result = categoriaService.listarCategorias();
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        } else {
            return ResponseEntity.ok().body(result.getData());
        }
    }

    @Operation(
            summary = "Actualizar categoría",
            description = "Actualiza los datos de una categoría existente"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoría actualizada exitosamente",
                    content = @Content(schema = @Schema(implementation = CategoriaDto.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Categoría no encontrada",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @Parameter(description = "ID de la categoría a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Datos actualizados de la categoría", required = true)
            @Valid @RequestBody CategoriaDto request) {
        ServiceResult<CategoriaDto> result = categoriaService.actualizarCategoria(id, request);
        return buildResponse(result, HttpStatus.OK);
    }

    @Operation(
            summary = "Eliminar categoría",
            description = "Elimina una categoría del sistema"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Categoría eliminada exitosamente"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error al eliminar la categoría",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Categoría no encontrada",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @Parameter(description = "ID de la categoría a eliminar", required = true, example = "1")
            @PathVariable Long id) {
        ServiceResult<Void> result = categoriaService.eliminarCategoria(id);
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