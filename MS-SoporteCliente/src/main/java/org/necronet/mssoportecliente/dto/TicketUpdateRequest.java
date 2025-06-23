package org.necronet.mssoportecliente.dto;

import lombok.Data;

@Data
public class TicketUpdateRequest {
    private String estado;
    private String solucion;
}