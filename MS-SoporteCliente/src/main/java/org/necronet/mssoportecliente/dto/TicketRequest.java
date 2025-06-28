package org.necronet.mssoportecliente.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketRequest {
    private Long clienteId;
    private String tipo;
    private String descripcion;
    private String prioridad;
    private String categoria;
}