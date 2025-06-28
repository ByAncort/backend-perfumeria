package org.necronet.mslogistica;

import org.app.dto.ServiceResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.necronet.mslogistica.dto.*;
import org.necronet.mslogistica.model.Envio;
import org.necronet.mslogistica.model.RutaOptimizada;
import org.necronet.mslogistica.repository.EnvioRepository;
import org.necronet.mslogistica.service.LogisticaService;
import org.necronet.mslogistica.shared.MicroserviceClient;
import org.necronet.mslogistica.shared.TokenContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LogisticaServiceTest {

    @Mock
    private EnvioRepository envioRepository;

    @Mock
    private MicroserviceClient microserviceClient;

    @Mock
    private TokenContext tokenContext;

    @InjectMocks
    private LogisticaService logisticaService;

    private PagoDto pagoDto;
    private Envio envio;
    private EnvioDto envioDto;

    @BeforeEach
    void setUp() {
        
        pagoDto = new PagoDto();
        pagoDto.setId(1L);
        pagoDto.setUsuarioId(100L);
        pagoDto.setCarritoId(50L);
        pagoDto.setEstado("COMPLETADO");

        envio = new Envio();
        envio.setId(1L);
        envio.setPagoId(1L);
        envio.setUsuarioId(100L);
        envio.setCarritoId(50L);
        envio.setCodigoSeguimiento("LOG" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        envio.setEstado("PREPARANDO");
        envio.setDireccionEnvio("Calle Falsa 123");
        envio.setCiudad("Buenos Aires");
        envio.setProvincia("Buenos Aires");
        envio.setCodigoPostal("1234");
        envio.setPais("Argentina");
        envio.setFechaCreacion(LocalDateTime.now());
        envio.setFechaActualizacion(LocalDateTime.now());
        envio.setMetodoEnvio("STANDARD");

        envioDto = new EnvioDto();
        envioDto.setId(1L);
        envioDto.setPagoId(1L);
        envioDto.setUsuarioId(100L);
        envioDto.setCarritoId(50L);
        envioDto.setCodigoSeguimiento(envio.getCodigoSeguimiento());
        envioDto.setEstado("PREPARANDO");
        envioDto.setDireccionEnvio("Calle Falsa 123");
        envioDto.setCiudad("Buenos Aires");
        envioDto.setProvincia("Buenos Aires");
        envioDto.setCodigoPostal("1234");
        envioDto.setPais("Argentina");
        envioDto.setFechaCreacion(envio.getFechaCreacion());
        envioDto.setFechaActualizacion(envio.getFechaActualizacion());
        envioDto.setMetodoEnvio("STANDARD");
    }

    @Test
    void llamarPago_deberiaRetornarPagoCuandoExiste() {
        
  
        String testToken = "eyJhbGciOiJIUzI1NiJ9.eyJleHBpcmF0aW9uIjoxNzUxMTc3MjE4Mzg4LCJpc3N1ZWRBdCI6MTc1MTA4MDgxODM4OCwic3ViIjoiYWRtaW4iLCJpYXQiOjE3NTEwODA4MTgsImV4cCI6MTc1MTE3NzIxOH0.gbcKLmX16MshzU09Mtx_G8E36iPxU7CgiSGji5Pamjk";
        tokenContext.setToken("eyJhbGciOiJIUzI1NiJ9.eyJleHBpcmF0aW9uIjoxNzUxMTc3MjE4Mzg4LCJpc3N1ZWRBdCI6MTc1MTA4MDgxODM4OCwic3ViIjoiYWRtaW4iLCJpYXQiOjE3NTEwODA4MTgsImV4cCI6MTc1MTE3NzIxOH0.gbcKLmX16MshzU09Mtx_G8E36iPxU7CgiSGji5Pamjk");
        tokenContext.setToken(testToken);
        when(microserviceClient.enviarConToken(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(PagoDto.class),
                anyString()))
                .thenReturn(new ResponseEntity<>(pagoDto, HttpStatus.OK));

        
        ServiceResult<PagoDto> result = logisticaService.llamarPago(1L);

        
        assertFalse(result.hasErrors());
        assertEquals("COMPLETADO", result.getData().getEstado());
    }

    @Test
    void llamarPago_deberiaRetornarErrorCuandoPagoNoExiste() {
        
       String testToken = "eyJhbGciOiJIUzI1NiJ9.eyJleHBpcmF0aW9uIjoxNzUxMTc3MjE4Mzg4LCJpc3N1ZWRBdCI6MTc1MTA4MDgxODM4OCwic3ViIjoiYWRtaW4iLCJpYXQiOjE3NTEwODA4MTgsImV4cCI6MTc1MTE3NzIxOH0.gbcKLmX16MshzU09Mtx_G8E36iPxU7CgiSGji5Pamjk";
        tokenContext.setToken(testToken);
        when(microserviceClient.enviarConToken(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(PagoDto.class),
                anyString()))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.NOT_FOUND));

        
        ServiceResult<PagoDto> result = logisticaService.llamarPago(1L);

        
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().get(0).contains("Error al obtener el pago"));
    }

    @Test
    void crearEnvio_deberiaCrearEnvioCorrectamente() {
        
       String testToken = "eyJhbGciOiJIUzI1NiJ9.eyJleHBpcmF0aW9uIjoxNzUxMTc3MjE4Mzg4LCJpc3N1ZWRBdCI6MTc1MTA4MDgxODM4OCwic3ViIjoiYWRtaW4iLCJpYXQiOjE3NTEwODA4MTgsImV4cCI6MTc1MTE3NzIxOH0.gbcKLmX16MshzU09Mtx_G8E36iPxU7CgiSGji5Pamjk";
        tokenContext.setToken(testToken);
        when(microserviceClient.enviarConToken(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(PagoDto.class),
                anyString()))
                .thenReturn(new ResponseEntity<>(pagoDto, HttpStatus.OK));

        when(envioRepository.findByPagoId(anyLong())).thenReturn(Optional.empty());
        when(envioRepository.save(any(Envio.class))).thenReturn(envio);

        
        ServiceResult<EnvioDto> result = logisticaService.crearEnvio(
                1L, "Calle Falsa 123", "Buenos Aires",
                "Buenos Aires", "1234", "Argentina", "STANDARD");

        
        assertFalse(result.hasErrors());
        assertNotNull(result.getData().getCodigoSeguimiento());
        assertEquals("PREPARANDO", result.getData().getEstado());
        verify(envioRepository, times(1)).save(any(Envio.class));
    }

    @Test
    void crearEnvio_deberiaRetornarErrorCuandoYaExisteEnvio() {
        
        when(envioRepository.findByPagoId(anyLong())).thenReturn(Optional.of(envio));

        
        ServiceResult<EnvioDto> result = logisticaService.crearEnvio(
                1L, "Calle Falsa 123", "Buenos Aires",
                "Buenos Aires", "1234", "Argentina", "STANDARD");

        
        assertTrue(result.hasErrors());
        assertEquals("Ya existe un envío registrado para este pago", result.getErrors().get(0));
        verify(envioRepository, never()).save(any(Envio.class));
    }

    @Test
    void crearEnvio_deberiaRetornarErrorCuandoPagoNoEstaCompletado() {
        
        pagoDto.setEstado("PENDIENTE");

       String testToken = "eyJhbGciOiJIUzI1NiJ9.eyJleHBpcmF0aW9uIjoxNzUxMTc3MjE4Mzg4LCJpc3N1ZWRBdCI6MTc1MTA4MDgxODM4OCwic3ViIjoiYWRtaW4iLCJpYXQiOjE3NTEwODA4MTgsImV4cCI6MTc1MTE3NzIxOH0.gbcKLmX16MshzU09Mtx_G8E36iPxU7CgiSGji5Pamjk";
        tokenContext.setToken(testToken);
        when(microserviceClient.enviarConToken(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(PagoDto.class),
                anyString()))
                .thenReturn(new ResponseEntity<>(pagoDto, HttpStatus.OK));

        when(envioRepository.findByPagoId(anyLong())).thenReturn(Optional.empty());

        
        ServiceResult<EnvioDto> result = logisticaService.crearEnvio(
                1L, "Calle Falsa 123", "Buenos Aires",
                "Buenos Aires", "1234", "Argentina", "STANDARD");

        
        assertTrue(result.hasErrors());
        assertEquals("El pago no está aprobado, no se puede crear el envío", result.getErrors().get(0));
        verify(envioRepository, never()).save(any(Envio.class));
    }

    @Test
    void obtenerEstadoEnvio_deberiaRetornarEstadoCorrectamente() {
        
        when(envioRepository.findByCodigoSeguimiento(anyString())).thenReturn(Optional.of(envio));

        
        ServiceResult<SeguimientoResponse> result = logisticaService.obtenerEstadoEnvio(envio.getCodigoSeguimiento());

        
        assertFalse(result.hasErrors());
        assertEquals("PREPARANDO", result.getData().getEstado());
        assertEquals("Almacén central", result.getData().getUbicacionActual());
    }

    @Test
    void obtenerEstadoEnvio_deberiaRetornarErrorCuandoNoExiste() {
        
        when(envioRepository.findByCodigoSeguimiento(anyString())).thenReturn(Optional.empty());

        
        ServiceResult<SeguimientoResponse> result = logisticaService.obtenerEstadoEnvio("CODIGO_INEXISTENTE");

        
        assertTrue(result.hasErrors());
        assertEquals("No se encontró ningún envío con el código proporcionado", result.getErrors().get(0));
    }

    @Test
    void actualizarEstadoEnvio_deberiaActualizarEstadoCorrectamente() {
        
        when(envioRepository.findByCodigoSeguimiento(anyString())).thenReturn(Optional.of(envio));
        when(envioRepository.save(any(Envio.class))).thenReturn(envio);

        
        ServiceResult<EnvioDto> result = logisticaService.actualizarEstadoEnvio(envio.getCodigoSeguimiento(), "EN_TRANSITO");

        
        assertFalse(result.hasErrors());
        assertEquals("EN_TRANSITO", result.getData().getEstado());
        verify(envioRepository, times(1)).save(any(Envio.class));
    }

    @Test
    void actualizarEstadoEnvio_deberiaEstablecerFechaEntregaCuandoEstadoEsEntregado() {
        
        when(envioRepository.findByCodigoSeguimiento(anyString())).thenReturn(Optional.of(envio));
        when(envioRepository.save(any(Envio.class))).thenAnswer(invocation -> {
            Envio envioGuardado = invocation.getArgument(0);
            envio.setFechaEntrega(envioGuardado.getFechaEntrega());
            return envio;
        });

        
        ServiceResult<EnvioDto> result = logisticaService.actualizarEstadoEnvio(envio.getCodigoSeguimiento(), "ENTREGADO");

        
        assertFalse(result.hasErrors());
        assertEquals("ENTREGADO", result.getData().getEstado());
        assertNotNull(result.getData().getFechaEntrega());
    }

    @Test
    void obtenerEnviosPorUsuario_deberiaRetornarListaEnvios() {
        
        when(envioRepository.findByUsuarioId(anyLong())).thenReturn(List.of(envio));

        
        ServiceResult<List<EnvioDto>> result = logisticaService.obtenerEnviosPorUsuario(100L);

        
        assertFalse(result.hasErrors());
        assertEquals(1, result.getData().size());
        assertEquals("Calle Falsa 123", result.getData().get(0).getDireccionEnvio());
    }

    @Test
    void optimizarRutaEnvio_deberiaRetornarRutaOptimizada() {
        
        when(envioRepository.findById(anyLong())).thenReturn(Optional.of(envio));

        
        ServiceResult<RutaOptimizadaDto> result = logisticaService.optimizarRutaEnvio(1L);

        
        assertFalse(result.hasErrors());
        assertEquals("Buenos Aires, Buenos Aires", result.getData().getDestino());
        assertTrue(result.getData().getRutaRecomendada().contains("Panamericana"));
    }





}
