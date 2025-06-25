package org.necronet.msresenasfeedback.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResenaDto {

    private Long id;
    private Long productoId;
    private Long clienteId;

    private String comentario;
    private Integer calificacion;
    private LocalDateTime fechaCreacion;
}
