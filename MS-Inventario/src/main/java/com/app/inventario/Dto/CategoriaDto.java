package com.app.inventario.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaDto {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    private String descripcion;
}