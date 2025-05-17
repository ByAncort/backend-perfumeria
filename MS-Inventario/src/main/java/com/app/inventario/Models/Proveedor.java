package com.app.inventario.Models;




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

    @ManyToMany
    @JoinTable(
            name = "productos_proveedores",
            joinColumns = @JoinColumn(name = "id_proveedor"),
            inverseJoinColumns = @JoinColumn(name = "id_producto")
    )
    @ToString.Exclude
    private List<Producto> productos = new ArrayList<>();
}