package org.necronet.msresenasfeedback.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResenaProductoDto {

    private Long id;
    private Long productoId;
    private Long clienteId;

    private String comentario;
    private Integer calificacion;
    private LocalDateTime fechaCreacion;

    // Campos opcionales si integras con microservicio de cliente/producto
    private String nombreProducto;
    private String nombreCliente;
    private String emailCliente;
}
