package org.necronet.mssoportecliente.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketSoporteDto {
    private Long id;
    private Long clienteId;
    private String tipo;
    private String estado;
    private String descripcion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private LocalDateTime fechaCierre;
    private String solucion;
    private String prioridad;
    private String categoria;
    private ClienteDto clienteInfo;
}