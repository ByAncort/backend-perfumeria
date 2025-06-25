package com.app.ventas.Models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carritos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Carro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private Double subtotal;

    @Column(nullable = false)
    private Double descuento;

    @Column(nullable = false)
    private Double total;

    @Column
    private String codigoCupon;

    @Column(nullable = false)
    @Builder.Default
    private String estado = "ACTIVO"; // ACTIVO, VACIO, COMPLETADO, ABANDONADO

    @OneToMany(mappedBy = "carro", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetalleCarro> detalles = new ArrayList<>();

    @Version
    private Long version; // Para control de concurrencia

    // Método helper para agregar detalles
    public void agregarDetalle(DetalleCarro detalle) {
        detalle.setCarro(this);
        this.detalles.add(detalle);
        this.calcularTotales();
    }

    // Método helper para calcular subtotal y total
    public void calcularTotales() {
        this.subtotal = this.detalles.stream()
                .mapToDouble(DetalleCarro::getSubtotal)
                .sum();

        // Si hay un descuento aplicado, mantenerlo
        this.total = this.subtotal - this.descuento;
    }

    // Método para limpiar el carro
    public void vaciarCarro() {
        this.detalles.clear();
        this.subtotal = 0.0;
        this.descuento = 0.0;
        this.total = 0.0;
        this.codigoCupon = null;
        this.estado = "VACIO";
    }
}