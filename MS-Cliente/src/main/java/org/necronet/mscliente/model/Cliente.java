package org.necronet.mscliente.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String apellido;
    private String dni;
    private String email;
    private String segmento; 

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private List<Contacto> contactos = new ArrayList<>();

    @OneToMany(mappedBy = "cliente")
    private List<Compra> compras;
}