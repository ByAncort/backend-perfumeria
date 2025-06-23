package org.necronet.mssoportecliente.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets_soporte")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketSoporte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long clienteId;

    @Column(nullable = false)
    private String tipo; // CONSULTA, RECLAMO, DEVOLUCION

    @Column(nullable = false)
    private String estado; // ABIERTO, EN_PROCESO, RESUELTO, CERRADO

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaActualizacion;

    private LocalDateTime fechaCierre;

    private String solucion;

    @Column(nullable = false)
    private String prioridad; // BAJA, MEDIA, ALTA, CRITICA

    private String categoria; // PRODUCTO, FACTURACION, ENTREGA, etc.
}