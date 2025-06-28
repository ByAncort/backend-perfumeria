package org.necronet.msresenasfeedback.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.necronet.msresenasfeedback.dto.CrearResenaDto;
import org.necronet.msresenasfeedback.dto.ResenaDto;
import org.necronet.msresenasfeedback.dto.ResenaProductoDto;
import org.necronet.msresenasfeedback.service.ResenaService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/resenas")
@RequiredArgsConstructor
@Tag(name = "Reseñas", description = "API para gestión de reseñas de productos")
public class ResenaController {

    private final ResenaService resenaService;

    @Operation(summary = "Crear una reseña", description = "Registra una nueva reseña para un producto por parte de un cliente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reseña creada exitosamente", content = @Content(schema = @Schema(implementation = ResenaDto.class))),
            @ApiResponse(responseCode = "400", description = "Error en la creación de la reseña", content = @Content)
    })
    @PostMapping
    public ResponseEntity<?> crearResena(
            @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos para crear la reseña",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CrearResenaDto.class))
            ) CrearResenaDto dto) {

        var result = resenaService.crearResena(dto);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }

        ResenaDto resena = result.getData();
        EntityModel<ResenaDto> model = EntityModel.of(resena);
        model.add(linkTo(methodOn(ResenaController.class).obtenerResenasPorProducto(resena.getProductoId())).withRel("resenas-del-producto"));
        model.add(linkTo(methodOn(ResenaController.class).obtenerTodasResenas()).withRel("todas-las-resenas"));

        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Obtener reseñas por producto", description = "Obtiene todas las reseñas asociadas a un producto específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reseñas obtenidas exitosamente", content = @Content(schema = @Schema(implementation = ResenaDto.class))),
            @ApiResponse(responseCode = "400", description = "Error al obtener las reseñas", content = @Content)
    })
    @GetMapping("/producto/{productoId}")
    public ResponseEntity<?> obtenerResenasPorProducto(
            @Parameter(description = "ID del producto", required = true)
            @PathVariable Long productoId) {

        var result = resenaService.obtenerResenasPorProducto(productoId);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }

        List<EntityModel<ResenaProductoDto>> resenas = result.getData().stream()
                .map(resena -> EntityModel.of(resena,
                        linkTo(methodOn(ResenaController.class)
                                .obtenerResenasPorProducto(resena.getProductoId())).withRel("resenas-del-producto"),
                        linkTo(methodOn(ResenaController.class)
                                .obtenerTodasResenas()).withRel("todas-las-resenas")))
                .collect(Collectors.toList());


        CollectionModel<EntityModel<ResenaProductoDto>> collectionModel = CollectionModel.of(resenas);
        collectionModel.add(linkTo(methodOn(ResenaController.class).obtenerResenasPorProducto(productoId)).withSelfRel());

        return ResponseEntity.ok(collectionModel);
    }

    @Operation(summary = "Obtener todas las reseñas", description = "Lista todas las reseñas registradas en el sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente", content = @Content(schema = @Schema(implementation = ResenaDto.class))),
            @ApiResponse(responseCode = "400", description = "Error al obtener el listado de reseñas", content = @Content)
    })
    @GetMapping
    public ResponseEntity<?> obtenerTodasResenas() {

        var result = resenaService.obtenerTodasResenas();
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        }

        List<EntityModel<ResenaDto>> resenas = result.getData().stream()
                .map(resena -> EntityModel.of(resena,
                        linkTo(methodOn(ResenaController.class).obtenerResenasPorProducto(resena.getProductoId())).withRel("resenas-del-producto"),
                        linkTo(methodOn(ResenaController.class).obtenerTodasResenas()).withSelfRel()))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<ResenaDto>> collectionModel = CollectionModel.of(resenas);
        collectionModel.add(linkTo(methodOn(ResenaController.class).obtenerTodasResenas()).withSelfRel());

        return ResponseEntity.ok(collectionModel);
    }
}
