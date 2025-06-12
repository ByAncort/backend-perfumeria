package com.app.proveedores.Controller;

import com.app.proveedores.Dto.ProveedorDto;
import com.app.proveedores.Dto.ServiceResult;
import com.app.proveedores.Models.Proveedor;
import com.app.proveedores.Service.ProveedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/ms-inventario/proveedor/")
@RequiredArgsConstructor
@Tag(name = "Gestión de Proveedores", description = "API para la administración de proveedores en el sistema de inventario")
public class ProveedorController {
    private final ProveedorService proveedorService;

    @Operation(
            summary = "Crear nuevo proveedor",
            description = "Registra un nuevo proveedor en el sistema",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Proveedor creado exitosamente",
                            content = @Content(schema = @Schema(implementation = Proveedor.class))),
                    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PostMapping("create-proveedor")
    public ResponseEntity<?> createProveedor(@RequestBody @Valid ProveedorDto proveedorDto) {
        ServiceResult<Proveedor> result = proveedorService.addProveedor(proveedorDto);
        return handleServiceResult(result, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Listar proveedores activos",
            description = "Obtiene todos los proveedores con estado activo en el sistema",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista de proveedores activos",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Proveedor.class)))),
                    @ApiResponse(responseCode = "400", description = "Error al procesar la solicitud",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @GetMapping("all-active")
    public ResponseEntity<?> getAllProveedoresActivos() {
        ServiceResult<List<Proveedor>> result = proveedorService.getAllProveedoresActivos();
        return handleServiceResult(result, HttpStatus.OK);
    }

    @Operation(
            summary = "Obtener proveedor por ID",
            description = "Recupera un proveedor específico según su ID único",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Proveedor encontrado",
                            content = @Content(schema = @Schema(implementation = Proveedor.class))),
                    @ApiResponse(responseCode = "400", description = "Proveedor no encontrado o ID inválido",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @GetMapping("{id}")
    public ResponseEntity<?> getProveedorById(
            @Parameter(description = "ID único del proveedor", example = "1", required = true)
            @PathVariable Long id) {
        ServiceResult<Proveedor> result = proveedorService.getProveedorById(id);
        return handleServiceResult(result, HttpStatus.OK);
    }

    @Operation(
            summary = "Actualizar proveedor",
            description = "Actualiza la información de un proveedor existente",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Proveedor actualizado exitosamente",
                            content = @Content(schema = @Schema(implementation = Proveedor.class))),
                    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o proveedor no encontrado",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PutMapping("update/{id}")
    public ResponseEntity<?> updateProveedor(
            @Parameter(description = "ID único del proveedor", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody @Valid ProveedorDto dto) {
        ServiceResult<Proveedor> result = proveedorService.updateProveedor(id, dto);
        return handleServiceResult(result, HttpStatus.OK);
    }

    @Operation(
            summary = "Cambiar estado de proveedor",
            description = "Activa o desactiva un proveedor en el sistema",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Estado del proveedor actualizado",
                            content = @Content(schema = @Schema(implementation = Proveedor.class))),
                    @ApiResponse(responseCode = "400", description = "Error al cambiar el estado",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PatchMapping("toggle-activo/{id}")
    public ResponseEntity<?> toggleActivoProveedor(
            @Parameter(description = "ID único del proveedor", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nuevo estado del proveedor", example = "true", required = true)
            @RequestParam boolean activo) {
        ServiceResult<Proveedor> result = proveedorService.toggleActivoProveedor(id, activo);
        return handleServiceResult(result, HttpStatus.OK);
    }

    @Operation(
            summary = "Buscar proveedor por RUT",
            description = "Obtiene un proveedor según su RUT (identificador tributario)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Proveedor encontrado",
                            content = @Content(schema = @Schema(implementation = Proveedor.class))),
                    @ApiResponse(responseCode = "400", description = "Proveedor no encontrado o RUT inválido",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @GetMapping("by-rut/{rut}")
    public ResponseEntity<?> getProveedorByRut(
            @Parameter(description = "RUT del proveedor", example = "12345678-9", required = true)
            @PathVariable String rut) {
        ServiceResult<Proveedor> result = proveedorService.getProveedorByRut(rut);
        return handleServiceResult(result, HttpStatus.OK);
    }

    @Operation(
            summary = "Eliminar proveedor",
            description = "Elimina un proveedor del sistema (eliminación lógica)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Proveedor eliminado exitosamente",
                            content = @Content(schema = @Schema(implementation = Proveedor.class))),
                    @ApiResponse(responseCode = "400", description = "Error al eliminar el proveedor",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deleteProveedor(
            @Parameter(description = "ID único del proveedor", example = "1", required = true)
            @PathVariable Long id) {
        ServiceResult<Proveedor> result = proveedorService.deleteProveedor(id);
        return handleServiceResult(result, HttpStatus.OK);
    }

    private ResponseEntity<?> handleServiceResult(ServiceResult<?> result, HttpStatus successStatus) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getErrors());
        } else {
            return ResponseEntity.status(successStatus).body(result.getData());
        }
    }
}