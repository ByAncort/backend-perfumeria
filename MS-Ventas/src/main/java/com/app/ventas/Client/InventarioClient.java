package com.app.ventas.Client;

import com.app.ventas.Dto.ItemVentaDto;
import com.app.ventas.Dto.ProductoInventarioDto;
import com.app.ventas.Dto.ServiceResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "inventario-service", url = "${inventario.service.url}")
public interface InventarioClient {

    @PostMapping("/inventario/validar-stock")
    ServiceResult<Boolean> validarStock(@RequestBody List<ItemVentaDto> items);

    @PostMapping("/api/inventario/venta/{productoId}")
    ServiceResult<Boolean> actualizarStock(@PathVariable String productoId);

    @GetMapping("/productos/{productoId}")
    ServiceResult<ProductoInventarioDto> obtenerProducto(@PathVariable Long productoId);
}