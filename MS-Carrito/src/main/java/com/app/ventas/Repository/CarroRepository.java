package com.app.ventas.Repository;

import com.app.ventas.Models.Carro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarroRepository extends JpaRepository<Carro,Long> {
    List<Carro> findByUsuarioId(Long usuarioId);
}
