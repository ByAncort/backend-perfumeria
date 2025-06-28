package org.necronet.mssoportecliente.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TicketUpdateRequest {
    private String estado;
    private String solucion;
}