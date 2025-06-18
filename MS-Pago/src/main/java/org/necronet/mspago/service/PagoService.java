package org.necronet.mspago.service;

import org.app.dto.ServiceResult;
import org.necronet.mspago.model.Pago;

import java.util.List;

public interface PagoService {
    ServiceResult<Pago> procesarPago(Long carritoId, String metodoPago);
    ServiceResult<Pago> obtenerPagoPorId(Long id);
    ServiceResult<List<Pago>> obtenerPagosPorUsuario(Long usuarioId);
    ServiceResult<Pago> reembolsarPago(Long pagoId);
}