package com.app.proveedores.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProveedorDto {
    @Schema(description = "RUT del proveedor", example = "12345678-9", required = true)
    private String rut;

    @Schema(description = "Nombre del proveedor", example = "Proveedor Ejemplo S.A.", required = true)
    private String nombre;

    @Schema(description = "Dirección del proveedor", example = "Av. Principal 1234")
    private String direccion;

    @Schema(description = "Teléfono de contacto", example = "+56912345678")
    private String telefono;

    @Schema(description = "Correo electrónico", example = "contacto@proveedor.cl")
    private String email;
    private boolean activo;
}
