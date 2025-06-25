package org.necronet.mspago.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.app.dto.ServiceResult;
import org.necronet.mspago.client.MicroserviceClient;
import org.necronet.mspago.client.TokenContext;
import org.necronet.mspago.dto.CarritoResponse;
import org.necronet.mspago.model.EstadoPago;
import org.necronet.mspago.model.MetodoPago;
import org.necronet.mspago.model.Pago;
import org.necronet.mspago.repository.PagoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;
    @Value("${ms.carrito.url}")
    private String carritoBaseUrl;
    private final MicroserviceClient microserviceClient;

    public CarritoResponse obtenerCarrito(Long id){
        String token = TokenContext.getToken();
        String url = carritoBaseUrl + "/obtener-carrito/" + id;
        ResponseEntity<CarritoResponse> response = microserviceClient.enviarConToken(
                url,
                HttpMethod.GET,
                null,
                CarritoResponse.class,
                token
        );
        return response.getBody();
    }

    public void confirmarCarrito(Long carritoId) {
        String token = TokenContext.getToken();
        String url = carritoBaseUrl + "/" + carritoId + "/confirmar";
        microserviceClient.enviarConToken(
                url,
                HttpMethod.POST,
                null,
                Void.class,
                token
        );
    }

    @Transactional
    public ServiceResult<Pago> procesarPago(Long carritoId, String metodoPagoStr) {
        try {
            CarritoResponse carrito = obtenerCarrito(carritoId);

            if (!"ACTIVO".equals(carrito.getEstado())) {
                return new ServiceResult<>(List.of("El carrito no está en estado válido para pagar"));
            }

            Pago pago = new Pago();
            pago.setCarritoId(carritoId);
            pago.setUsuarioId(carrito.getUsuarioId());
            pago.setMonto(carrito.getTotal());
            pago.setMetodoPago(MetodoPago.valueOf(metodoPagoStr));
            pago.setEstado(EstadoPago.PENDIENTE);

            pago.setUltimosCuatroDigitos("4242");
            pago.setNombreTitular("TITULAR EJEMPLO");


            boolean pagoExitoso = simularProcesamientoPago();

            if (pagoExitoso) {
                pago.setEstado(EstadoPago.COMPLETADO);

                confirmarCarrito( carritoId);
            } else {
                pago.setEstado(EstadoPago.RECHAZADO);
                return new ServiceResult<>(List.of("El pago fue rechazado"));
            }

            return new ServiceResult<>(pagoRepository.save(pago));
        } catch (Exception e) {
            return new ServiceResult<>(List.of("Error al procesar el pago: " + e.getMessage()));
        }
    }

    public ServiceResult<Pago> obtenerPagoPorId(Long id) {
        try {
            Pago pago = pagoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Pago no encontrado"));
            return new ServiceResult<>(pago);
        } catch (Exception e) {
            return new ServiceResult<>(List.of("Error al obtener el pago: " + e.getMessage()));
        }
    }

    public ServiceResult<List<Pago>> obtenerPagosPorUsuario(Long usuarioId) {
        try {
            List<Pago> pagos = pagoRepository.findByUsuarioId(usuarioId);
            return new ServiceResult<>(pagos);
        } catch (Exception e) {
            return new ServiceResult<>(List.of("Error al obtener los pagos del usuario: " + e.getMessage()));
        }
    }

    public ServiceResult<Pago> reembolsarPago(Long pagoId) {
        try {
            ServiceResult<Pago> resultadoPago = obtenerPagoPorId(pagoId);

            if (resultadoPago.hasErrors()) {
                return resultadoPago;
            }

            Pago pago = resultadoPago.getData();

            if (pago.getEstado() != EstadoPago.COMPLETADO) {
                return new ServiceResult<>(List.of("Solo se puede reembolsar un pago completado"));
            }


            boolean reembolsoExitoso = simularProcesamientoReembolso();

            if (reembolsoExitoso) {
                pago.setEstado(EstadoPago.REEMBOLSADO);
                return new ServiceResult<>(pagoRepository.save(pago));
            } else {
                return new ServiceResult<>(List.of("El reembolso falló"));
            }
        } catch (Exception e) {
            return new ServiceResult<>(List.of("Error al procesar el reembolso: " + e.getMessage()));
        }
    }

    private boolean simularProcesamientoPago() {

        return true; // Simula pago exitoso
    }

    private boolean simularProcesamientoReembolso() {

        return true; // Simula reembolso exitoso
    }


}