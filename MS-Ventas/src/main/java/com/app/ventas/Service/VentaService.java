package com.app.ventas.Service;

import com.app.ventas.Client.InventarioClient;
import com.app.ventas.Dto.*;
import com.app.ventas.Models.DetalleVenta;
import com.app.ventas.Models.Venta;
import com.app.ventas.Repository.VentaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VentaService {

    private final VentaRepository ventaRepository;
    private final InventarioClient inventarioClient;


}