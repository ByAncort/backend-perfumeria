package org.necronet.mslogistica.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "envios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Envio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long pagoId;

    @Column(nullable = false)
    private Long usuarioId;

    @Column(nullable = false)
    private Long carritoId;

    @Column(nullable = false)
    private String codigoSeguimiento;

    @Column(nullable = false)
    private String estado; // "PREPARANDO", "EN_TRANSITO", "ENTREGADO", "CANCELADO"

    @Column(nullable = false)
    private String direccionEnvio;

    @Column(nullable = false)
    private String ciudad;

    @Column(nullable = false)
    private String provincia;

    @Column(nullable = false)
    private String codigoPostal;

    @Column(nullable = false)
    private String pais;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaActualizacion;

    private LocalDateTime fechaEntrega;

    @Column(nullable = false)
    private String metodoEnvio; // "ESTANDAR", "EXPRESS", "PREMIUM"

    private String notas;
}