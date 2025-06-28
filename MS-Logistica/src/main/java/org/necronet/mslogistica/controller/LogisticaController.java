package org.necronet.mslogistica.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.app.dto.ServiceResult;
import org.necronet.mslogistica.dto.EnvioDto;
import org.necronet.mslogistica.dto.RutaOptimizadaDto;
import org.necronet.mslogistica.dto.SeguimientoResponse;
import org.necronet.mslogistica.service.LogisticaService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/logistica")
@Tag(name = "Gestión Logística", description = "API para la gestión de envíos, seguimiento y optimización de rutas")
public class LogisticaController {

    private final LogisticaService logisticaService;

    @Operation(summary = "Crear un nuevo envío", description = "Crea un nuevo envío asociado a un pago confirmado")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Envío creado exitosamente",
                    content = @Content(schema = @Schema(implementation = EnvioDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error en la solicitud",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class))
            )
    })
    @PostMapping("/envios")
    public ResponseEntity<EntityModel<ServiceResult<EnvioDto>>> crearEnvio(
            @Parameter(description = "ID del pago asociado", required = true) @RequestParam Long pagoId,
            @Parameter(description = "Dirección de envío completa", required = true) @RequestParam String direccionEnvio,
            @Parameter(description = "Ciudad de destino", required = true) @RequestParam String ciudad,
            @Parameter(description = "Provincia/Estado de destino", required = true) @RequestParam String provincia,
            @Parameter(description = "Código postal", required = true) @RequestParam String codigoPostal,
            @Parameter(description = "País de destino", required = true) @RequestParam String pais,
            @Parameter(description = "Método de envío (ESTANDAR, EXPRESS, PREMIUM)", required = true) @RequestParam String metodoEnvio) {

        ServiceResult<EnvioDto> result = logisticaService.crearEnvio(pagoId, direccionEnvio, ciudad, provincia, codigoPostal, pais, metodoEnvio);

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(EntityModel.of(result));
        }

        EntityModel<ServiceResult<EnvioDto>> resource = EntityModel.of(result);
        String codigo = result.getData().getCodigoSeguimiento();
        if (codigo != null) {
            resource.add(linkTo(methodOn(LogisticaController.class).obtenerEstadoEnvio(codigo)).withRel("seguimiento"));
            resource.add(linkTo(methodOn(LogisticaController.class).actualizarEstadoEnvio(codigo, null)).withRel("actualizar-estado"));
        }
        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Obtener estado de envío", description = "Consulta el estado actual de un envío mediante su código de seguimiento")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado del envío obtenido exitosamente",
                    content = @Content(schema = @Schema(implementation = SeguimientoResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Código de seguimiento inválido",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class))
            )
    })
    @GetMapping("/envios/seguimiento/{codigoSeguimiento}")
    public ResponseEntity<EntityModel<ServiceResult<SeguimientoResponse>>> obtenerEstadoEnvio(
            @Parameter(description = "Código único de seguimiento del envío", required = true)
            @PathVariable String codigoSeguimiento) {

        ServiceResult<SeguimientoResponse> result = logisticaService.obtenerEstadoEnvio(codigoSeguimiento);

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(EntityModel.of(result));
        }

        EntityModel<ServiceResult<SeguimientoResponse>> resource = EntityModel.of(result);
        resource.add(linkTo(methodOn(LogisticaController.class).actualizarEstadoEnvio(codigoSeguimiento, null)).withRel("actualizar-estado"));

        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Actualizar estado de envío", description = "Actualiza el estado de un envío (PREPARANDO, EN_TRANSITO, ENTREGADO, CANCELADO)")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = EnvioDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error al actualizar el estado",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class))
            )
    })
    @PutMapping("/envios/{codigoSeguimiento}/estado")
    public ResponseEntity<EntityModel<ServiceResult<EnvioDto>>> actualizarEstadoEnvio(
            @Parameter(description = "Código de seguimiento del envío", required = true)
            @PathVariable String codigoSeguimiento,

            @Parameter(
                    description = "Nuevo estado del envío",
                    required = true,
                    schema = @Schema(allowableValues = {"PREPARANDO", "EN_TRANSITO", "ENTREGADO", "CANCELADO"})
            )
            @RequestParam(required = false) String nuevoEstado) { // Nota: null para enlace HATEOAS sin estado

        ServiceResult<EnvioDto> result = logisticaService.actualizarEstadoEnvio(codigoSeguimiento, nuevoEstado);

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(EntityModel.of(result));
        }

        EntityModel<ServiceResult<EnvioDto>> resource = EntityModel.of(result);
        String codigo = result.getData().getCodigoSeguimiento();
        if (codigo != null) {
            resource.add(linkTo(methodOn(LogisticaController.class).obtenerEstadoEnvio(codigo)).withRel("seguimiento"));
        }

        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Obtener envíos por usuario", description = "Lista todos los envíos asociados a un usuario específico")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de envíos obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = EnvioDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error al obtener los envíos",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class))
            )
    })
    @GetMapping("/envios/usuario/{usuarioId}")
    public ResponseEntity<CollectionModel<EntityModel<EnvioDto>>> obtenerEnviosPorUsuario(
            @Parameter(description = "ID del usuario", required = true)
            @PathVariable Long usuarioId) {

        ServiceResult<List<EnvioDto>> result = logisticaService.obtenerEnviosPorUsuario(usuarioId);

        if (result.hasErrors()) {
            // Para errores, no devolvemos colección sino directamente el error (podrías crear un EntityModel para error si quieres)
            return ResponseEntity.badRequest().build();
        }

        List<EntityModel<EnvioDto>> envios = result.getData().stream()
                .map(envio -> {
                    EntityModel<EnvioDto> envioResource = EntityModel.of(envio);
                    String codigo = envio.getCodigoSeguimiento();
                    if (codigo != null) {
                        envioResource.add(linkTo(methodOn(LogisticaController.class).obtenerEstadoEnvio(codigo)).withRel("seguimiento"));
                        envioResource.add(linkTo(methodOn(LogisticaController.class).actualizarEstadoEnvio(codigo, null)).withRel("actualizar-estado"));
                    }
                    return envioResource;
                })
                .collect(Collectors.toList());


        CollectionModel<EntityModel<EnvioDto>> collectionModel = CollectionModel.of(envios);
        collectionModel.add(linkTo(methodOn(LogisticaController.class).obtenerEnviosPorUsuario(usuarioId)).withSelfRel());

        return ResponseEntity.ok(collectionModel);
    }

    @Operation(summary = "Optimizar ruta de envío", description = "Calcula la ruta optimizada para un envío específico")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Ruta optimizada calculada exitosamente",
                    content = @Content(schema = @Schema(implementation = RutaOptimizadaDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error al calcular la ruta",
                    content = @Content(schema = @Schema(implementation = ServiceResult.class))
            )
    })
    @GetMapping("/envios/{envioId}/ruta")
    public ResponseEntity<EntityModel<ServiceResult<RutaOptimizadaDto>>> optimizarRutaEnvio(
            @Parameter(description = "ID del envío", required = true)
            @PathVariable Long envioId) {

        ServiceResult<RutaOptimizadaDto> result = logisticaService.optimizarRutaEnvio(envioId);

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(EntityModel.of(result));
        }

        EntityModel<ServiceResult<RutaOptimizadaDto>> resource = EntityModel.of(result);
        // Link para obtener info del envío relacionado (si fuera posible agregarlo)
        String codigo = "sec.00";
        if (codigo != null) {
            resource.add(linkTo(methodOn(LogisticaController.class).obtenerEstadoEnvio(codigo)).withRel("seguimiento"));
        }
        return ResponseEntity.ok(resource);
    }
}
