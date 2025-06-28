package org.necronet.mspago.service;

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
import org.necronet.mspago.model.EstadoPago;
import org.necronet.mspago.model.MetodoPago;
import org.necronet.mspago.model.Pago;
import org.necronet.mspago.repository.PagoRepository;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private MicroserviceClient microserviceClient;

    @Mock
    private TokenContext tokenContext;

    @InjectMocks
    private PagoService pagoService;

    private CarritoResponse carritoActivo;
    private CarritoResponse carritoInactivo;
    private Pago pagoCompletado;
    private Pago pagoRechazado;

    @BeforeEach
    void setUp() {
        
        carritoActivo = new CarritoResponse();
        carritoActivo.setId(1L);
        carritoActivo.setUsuarioId(100L);
        carritoActivo.setTotal(1500.0);
        carritoActivo.setEstado("ACTIVO");

        carritoInactivo = new CarritoResponse();
        carritoInactivo.setId(2L);
        carritoInactivo.setUsuarioId(100L);
        carritoInactivo.setTotal(2000.0);
        carritoInactivo.setEstado("INACTIVO");

        pagoCompletado = new Pago();
        pagoCompletado.setId(1L);
        pagoCompletado.setCarritoId(1L);
        pagoCompletado.setUsuarioId(100L);
        pagoCompletado.setMonto(1500.0);
        pagoCompletado.setMetodoPago(MetodoPago.TARJETA_CREDITO);
        pagoCompletado.setEstado(EstadoPago.COMPLETADO);
        pagoCompletado.setUltimosCuatroDigitos("4242");
        pagoCompletado.setNombreTitular("TITULAR EJEMPLO");

        pagoRechazado = new Pago();
        pagoRechazado.setId(2L);
        pagoRechazado.setCarritoId(2L);
        pagoRechazado.setUsuarioId(100L);
        pagoRechazado.setMonto(2000.0);
        pagoRechazado.setMetodoPago(MetodoPago.PAYPAL);
        pagoRechazado.setEstado(EstadoPago.RECHAZADO);
    }

    @Test
    void procesarPago_deberiaProcesarPagoExitosamente() {
        
                tokenContext.setToken("eyJhbGciOiJIUzI1NiJ9.eyJleHBpcmF0aW9uIjoxNzUxMTc3MjE4Mzg4LCJpc3N1ZWRBdCI6MTc1MTA4MDgxODM4OCwic3ViIjoiYWRtaW4iLCJpYXQiOjE3NTEwODA4MTgsImV4cCI6MTc1MTE3NzIxOH0.gbcKLmX16MshzU09Mtx_G8E36iPxU7CgiSGji5Pamjk");
        when(microserviceClient.enviarConToken(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(CarritoResponse.class),
                anyString()))
                .thenReturn(new ResponseEntity<>(carritoActivo, HttpStatus.OK));

        when(pagoRepository.save(any(Pago.class))).thenReturn(pagoCompletado);

        
        ServiceResult<Pago> result = pagoService.procesarPago(1L, "TARJETA_CREDITO");

        
        assertFalse(result.hasErrors());
        assertEquals(EstadoPago.COMPLETADO, result.getData().getEstado());
        verify(microserviceClient, times(1)).enviarConToken(
                anyString(), eq(HttpMethod.POST), isNull(), eq(Void.class), anyString());
    }

    @Test
    void procesarPago_deberiaFallarCuandoCarritoNoEstaActivo() {
        
                tokenContext.setToken("eyJhbGciOiJIUzI1NiJ9.eyJleHBpcmF0aW9uIjoxNzUxMTc3MjE4Mzg4LCJpc3N1ZWRBdCI6MTc1MTA4MDgxODM4OCwic3ViIjoiYWRtaW4iLCJpYXQiOjE3NTEwODA4MTgsImV4cCI6MTc1MTE3NzIxOH0.gbcKLmX16MshzU09Mtx_G8E36iPxU7CgiSGji5Pamjk");
        when(microserviceClient.enviarConToken(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(CarritoResponse.class),
                anyString()))
                .thenReturn(new ResponseEntity<>(carritoInactivo, HttpStatus.OK));

        
        ServiceResult<Pago> result = pagoService.procesarPago(2L, "PAYPAL");

        
        assertTrue(result.hasErrors());
        assertEquals("El carrito no está en estado válido para pagar", result.getErrors().get(0));
        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    void obtenerPagoPorId_deberiaRetornarPagoCuandoExiste() {
        
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pagoCompletado));

        
        ServiceResult<Pago> result = pagoService.obtenerPagoPorId(1L);

        
        assertFalse(result.hasErrors());
        assertEquals(1L, result.getData().getId());
        assertEquals(EstadoPago.COMPLETADO, result.getData().getEstado());
    }

    @Test
    void obtenerPagoPorId_deberiaRetornarErrorCuandoNoExiste() {
        
        when(pagoRepository.findById(99L)).thenReturn(Optional.empty());

        
        ServiceResult<Pago> result = pagoService.obtenerPagoPorId(99L);

        
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().get(0).contains("Pago no encontrado"));
    }

    @Test
    void obtenerPagosPorUsuario_deberiaRetornarListaPagos() {
        
        when(pagoRepository.findByUsuarioId(100L)).thenReturn(List.of(pagoCompletado, pagoRechazado));

        
        ServiceResult<List<Pago>> result = pagoService.obtenerPagosPorUsuario(100L);

        
        assertFalse(result.hasErrors());
        assertEquals(2, result.getData().size());
    }

    @Test
    void reembolsarPago_deberiaReembolsarPagoExitosamente() {
        
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pagoCompletado));
        when(pagoRepository.save(any(Pago.class))).thenReturn(pagoCompletado);

        
        ServiceResult<Pago> result = pagoService.reembolsarPago(1L);

        
        assertFalse(result.hasErrors());
        assertEquals(EstadoPago.REEMBOLSADO, result.getData().getEstado());
    }

    @Test
    void reembolsarPago_deberiaFallarCuandoPagoNoEstaCompletado() {
        
        when(pagoRepository.findById(2L)).thenReturn(Optional.of(pagoRechazado));

        
        ServiceResult<Pago> result = pagoService.reembolsarPago(2L);

        
        assertTrue(result.hasErrors());
        assertEquals("Solo se puede reembolsar un pago completado", result.getErrors().get(0));
        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    void obtenerCarrito_deberiaRetornarCarritoCorrectamente() {
        
                tokenContext.setToken("eyJhbGciOiJIUzI1NiJ9.eyJleHBpcmF0aW9uIjoxNzUxMTc3MjE4Mzg4LCJpc3N1ZWRBdCI6MTc1MTA4MDgxODM4OCwic3ViIjoiYWRtaW4iLCJpYXQiOjE3NTEwODA4MTgsImV4cCI6MTc1MTE3NzIxOH0.gbcKLmX16MshzU09Mtx_G8E36iPxU7CgiSGji5Pamjk");
        when(microserviceClient.enviarConToken(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(CarritoResponse.class),
                anyString()))
                .thenReturn(new ResponseEntity<>(carritoActivo, HttpStatus.OK));

        
        CarritoResponse result = pagoService.obtenerCarrito(1L);

        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("ACTIVO", result.getEstado());
    }

    @Test
    void confirmarCarrito_deberiaLlamarAlMicroservicioCorrectamente() {
        
                tokenContext.setToken("eyJhbGciOiJIUzI1NiJ9.eyJleHBpcmF0aW9uIjoxNzUxMTc3MjE4Mzg4LCJpc3N1ZWRBdCI6MTc1MTA4MDgxODM4OCwic3ViIjoiYWRtaW4iLCJpYXQiOjE3NTEwODA4MTgsImV4cCI6MTc1MTE3NzIxOH0.gbcKLmX16MshzU09Mtx_G8E36iPxU7CgiSGji5Pamjk");
        when(microserviceClient.enviarConToken(
                anyString(),
                eq(HttpMethod.POST),
                isNull(),
                eq(Void.class),
                anyString()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        
        pagoService.confirmarCarrito(1L);

        
        verify(microserviceClient, times(1)).enviarConToken(
                anyString(), eq(HttpMethod.POST), isNull(), eq(Void.class), anyString());
    }
}