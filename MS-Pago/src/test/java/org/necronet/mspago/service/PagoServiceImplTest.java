package org.necronet.mspago.service;


import net.datafaker.Faker;
import org.app.dto.ServiceResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.necronet.mspago.client.MicroserviceClient;
import org.necronet.mspago.client.TokenContext;
import org.necronet.mspago.dto.CarritoResponse;
import org.necronet.mspago.model.*;
import org.necronet.mspago.repository.PagoRepository;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoServiceImplTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private MicroserviceClient microserviceClient;

    @Mock
    private TokenContext tokenContext;

    @InjectMocks
    private PagoServiceImpl pagoService;

    private final Faker faker = new Faker();
    private CarritoResponse carritoResponse;
    private Pago pago;

    @BeforeEach
    void setUp() {

        carritoResponse = new CarritoResponse();
        carritoResponse.setId(faker.number().randomNumber());
        carritoResponse.setUsuarioId(faker.number().randomNumber());
        carritoResponse.setTotal(faker.number().randomDouble(2, 10, 1000));
        carritoResponse.setEstado("ACTIVO");

        pago = new Pago();
        pago.setId(faker.number().randomNumber());
        pago.setCarritoId(carritoResponse.getId());
        pago.setUsuarioId(carritoResponse.getUsuarioId());
        pago.setMonto(carritoResponse.getTotal());
        pago.setMetodoPago(MetodoPago.TARJETA_CREDITO);
        pago.setEstado(EstadoPago.COMPLETADO);
        pago.setUltimosCuatroDigitos("4242");
        pago.setNombreTitular(faker.name().fullName());
    }

    @Test
    void procesarPago_CarritoActivo_DeberiaRetornarPagoCompletado() {
        String metodoPago = "TARJETA_CREDITO";
        Long carritoId = carritoResponse.getId();

        when(microserviceClient.enviarConToken(anyString(), eq(HttpMethod.GET), isNull(), eq(CarritoResponse.class), anyString()))
                .thenReturn(ResponseEntity.ok(carritoResponse));
        when(pagoRepository.save(any(Pago.class))).thenReturn(pago);

        ServiceResult<Pago> resultado = pagoService.procesarPago(carritoId, metodoPago);

        assertFalse(resultado.hasErrors());
        assertNotNull(resultado.getData());
        assertEquals(EstadoPago.COMPLETADO, resultado.getData().getEstado());
        verify(microserviceClient, times(1)).enviarConToken(anyString(), eq(HttpMethod.POST), isNull(), eq(Void.class), anyString());
    }

    @Test
    void procesarPago_CarritoNoActivo_DeberiaRetornarError() {
        String metodoPago = "TARJETA_CREDITO";
        Long carritoId = carritoResponse.getId();
        carritoResponse.setEstado("CERRADO");

        when(microserviceClient.enviarConToken(anyString(), eq(HttpMethod.GET), isNull(), eq(CarritoResponse.class), anyString()))
                .thenReturn(ResponseEntity.ok(carritoResponse));

        ServiceResult<Pago> resultado = pagoService.procesarPago(carritoId, metodoPago);

        assertTrue(resultado.hasErrors());
        assertEquals("El carrito no está en estado válido para pagar", resultado.getErrors().get(0));
        verify(pagoRepository, never()).save(any());
    }

    @Test
    void procesarPago_MetodoPagoInvalido_DeberiaRetornarError() {
        String metodoPago = "METODO_INVALIDO";
        Long carritoId = carritoResponse.getId();

        when(microserviceClient.enviarConToken(anyString(), eq(HttpMethod.GET), isNull(), eq(CarritoResponse.class), anyString()))
                .thenReturn(ResponseEntity.ok(carritoResponse));

        assertThrows(IllegalArgumentException.class, () -> {
            pagoService.procesarPago(carritoId, metodoPago);
        });
    }

    @Test
    void obtenerPagoPorId_PagoExistente_DeberiaRetornarPago() {
        Long pagoId = pago.getId();
        when(pagoRepository.findById(pagoId)).thenReturn(Optional.of(pago));

        ServiceResult<Pago> resultado = pagoService.obtenerPagoPorId(pagoId);

        assertFalse(resultado.hasErrors());
        assertEquals(pago, resultado.getData());
    }

    @Test
    void obtenerPagoPorId_PagoNoExistente_DeberiaRetornarError() {
        Long pagoId = faker.number().randomNumber();
        when(pagoRepository.findById(pagoId)).thenReturn(Optional.empty());

        ServiceResult<Pago> resultado = pagoService.obtenerPagoPorId(pagoId);

        assertTrue(resultado.hasErrors());
        assertTrue(resultado.getErrors().get(0).contains("Pago no encontrado"));
    }

    @Test
    void obtenerPagosPorUsuario_UsuarioConPagos_DeberiaRetornarLista() {
        Long usuarioId = carritoResponse.getUsuarioId();
        List<Pago> pagos = List.of(pago);
        when(pagoRepository.findByUsuarioId(usuarioId)).thenReturn(pagos);

        ServiceResult<List<Pago>> resultado = pagoService.obtenerPagosPorUsuario(usuarioId);

        assertFalse(resultado.hasErrors());
        assertEquals(1, resultado.getData().size());
        assertEquals(pago, resultado.getData().get(0));
    }

    @Test
    void reembolsarPago_PagoCompletado_DeberiaRetornarPagoReembolsado() {
        Long pagoId = pago.getId();
        when(pagoRepository.findById(pagoId)).thenReturn(Optional.of(pago));
        when(pagoRepository.save(any(Pago.class))).thenReturn(pago);

        ServiceResult<Pago> resultado = pagoService.reembolsarPago(pagoId);

        assertFalse(resultado.hasErrors());
        assertEquals(EstadoPago.REEMBOLSADO, resultado.getData().getEstado());
    }

    @Test
    void reembolsarPago_PagoNoCompletado_DeberiaRetornarError() {
        pago.setEstado(EstadoPago.PENDIENTE);
        Long pagoId = pago.getId();
        when(pagoRepository.findById(pagoId)).thenReturn(Optional.of(pago));

        ServiceResult<Pago> resultado = pagoService.reembolsarPago(pagoId);

        assertTrue(resultado.hasErrors());
        assertEquals("Solo se puede reembolsar un pago completado", resultado.getErrors().get(0));
        verify(pagoRepository, never()).save(any());
    }

    @Test
    void reembolsarPago_ReembolsoFallido_DeberiaRetornarError() {
      // No hay logica :<
    }
}
