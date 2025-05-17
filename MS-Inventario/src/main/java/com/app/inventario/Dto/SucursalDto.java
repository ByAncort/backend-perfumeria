package com.app.inventario.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import lombok.Builder;
import lombok.Data;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SucursalDto {
    @NotBlank(message = "El nombre de la sucursal es obligatorio")
    private String nombre;

    private String direccion;
    private String ciudad;

    @Pattern(regexp = "^\\+?56?\\d{9}$", message = "Formato de teléfono inválido")
    private String telefono;

    @Schema(description = "Horario en formato HH:mm", example = "08:30")
    private LocalTime horarioApertura;

    @Schema(description = "Horario en formato HH:mm", example = "20:00")
    private LocalTime horarioCierre;

    @Builder.Default
    private Boolean activa = true;
}