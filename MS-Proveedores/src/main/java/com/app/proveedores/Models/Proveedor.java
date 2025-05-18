package com.app.proveedores.Models;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "proveedores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proveedor")
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(unique = true, length = 12)
    private String rut;

    private String direccion;
    private String telefono;
    private String email;

    @Builder.Default
    private Boolean activo = true;

    public boolean isActivo() {
        return activo != null ? activo : false;
    }

}

