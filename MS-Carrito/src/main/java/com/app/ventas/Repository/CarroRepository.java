package com.app.ventas.Repository;

import com.app.ventas.Models.Carro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarroRepository extends JpaRepository<Carro,Long> {
}
