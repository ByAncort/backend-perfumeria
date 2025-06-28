package org.necronet.msresenasfeedback;

import org.app.dto.ServiceResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.necronet.msresenasfeedback.dto.*;
import org.necronet.msresenasfeedback.model.Resena;
import org.necronet.msresenasfeedback.repository.ResenaRepository;
import org.necronet.msresenasfeedback.service.ResenaService;
import org.necronet.msresenasfeedback.shared.MicroserviceClient;
import org.necronet.msresenasfeedback.shared.TokenContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ResenaServiceTest {
    @Mock
    private ResenaRepository resenaRepository;

    @Mock
    private MicroserviceClient microserviceClient;

    @Mock
    private TokenContext tokenContext;

    @InjectMocks
    private ResenaService resenaService;

    private CrearResenaDto crearResenaDto;
    private ProductoDto productoDto;
    private ClienteDto clienteDto;
    private Resena resena;
    private ResenaDto resenaDto;

    @BeforeEach
    void setUp() {
        // Configuración de datos de prueba
        crearResenaDto = CrearResenaDto.builder()
                .productoId(1L)
                .clienteId(1L)
                .comentario("Excelente producto")
                .calificacion(5)
                .build();

        productoDto = ProductoDto.builder()
                .id(1L)
                .nombre("Laptop")
                .build();

        clienteDto = ClienteDto.builder()
                .id(1L)
                .nombre("Juan Perez")
                .build();

        resena = Resena.builder()
                .id(1L)
                .productoId(1L)
                .clienteId(1L)
                .comentario("Excelente producto")
                .calificacion(5)
                .fechaCreacion(LocalDateTime.now())
                .build();

        resenaDto = ResenaDto.builder()
                .id(1L)
                .productoId(1L)
                .clienteId(1L)
                .comentario("Excelente producto")
                .calificacion(5)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    @Test
    void crearResena_deberiaCrearResenaCorrectamente() {
        // Arrange
        String testToken = "eyJhbGciOiJIUzI1NiJ9.eyJleHBpcmF0aW9uIjoxNzUxMTc3MjE4Mzg4LCJpc3N1ZWRBdCI6MTc1MTA4MDgxODM4OCwic3ViIjoiYWRtaW4iLCJpYXQiOjE3NTEwODA4MTgsImV4cCI6MTc1MTE3NzIxOH0.gbcKLmX16MshzU09Mtx_g8E36iPxU7CgiSGji5Pamjk";
        tokenContext.setToken(testToken);

        // Mock para obtener producto
        when(microserviceClient.enviarConToken(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(ProductoDto.class),
                anyString()))
                .thenReturn(new ResponseEntity<>(productoDto, HttpStatus.OK));

        // Mock para obtener cliente
        when(microserviceClient.enviarConToken(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(ClienteDto.class),
                anyString()))
                .thenReturn(new ResponseEntity<>(clienteDto, HttpStatus.OK));

        when(resenaRepository.save(any(Resena.class))).thenReturn(resena);

        // Act
        ServiceResult<ResenaDto> result = resenaService.crearResena(crearResenaDto);

        // Assert
        assertFalse(result.hasErrors());
        assertNotNull(result.getData());
        assertEquals("Excelente producto", result.getData().getComentario());
        verify(resenaRepository, times(1)).save(any(Resena.class));
    }

    @Test
    void crearResena_deberiaRetornarErrorCuandoProductoNoExiste() {
        // Arrange
        String testToken = "eyJhbGciOiJIUzI1NiJ9.eyJleHBpcmF0aW9uIjoxNzUxMTc3MjE4Mzg4LCJpc3N1ZWRBdCI6MTc1MTA4MDgxODM4OCwic3ViIjoiYWRtaW4iLCJpYXQiOjE3NTEwODA4MTgsImV4cCI6MTc1MTE3NzIxOH0.gbcKLmX16MshzU09Mtx_g8E36iPxU7CgiSGji5Pamjk";
        tokenContext.setToken(testToken);

        // Mock para fallar al obtener producto
        when(microserviceClient.enviarConToken(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(ProductoDto.class),
                anyString()))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.NOT_FOUND));

        // Act
        ServiceResult<ResenaDto> result = resenaService.crearResena(crearResenaDto);

        // Assert
        assertTrue(result.hasErrors());
        assertEquals("Error al obtener los datos del producto", result.getErrors().get(0));
        verify(resenaRepository, never()).save(any(Resena.class));
    }

    @Test
    void crearResena_deberiaRetornarErrorCuandoClienteNoExiste() {
        // Arrange
        String testToken = "eyJhbGciOiJIUzI1NiJ9.eyJleHBpcmF0aW9uIjoxNzUxMTc3MjE4Mzg4LCJpc3N1ZWRBdCI6MTc1MTA4MDgxODM4OCwic3ViIjoiYWRtaW4iLCJpYXQiOjE3NTEwODA4MTgsImV4cCI6MTc1MTE3NzIxOH0.gbcKLmX16MshzU09Mtx_g8E36iPxU7CgiSGji5Pamjk";
        tokenContext.setToken(testToken);

        // Mock para obtener producto exitosamente
        when(microserviceClient.enviarConToken(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(ProductoDto.class),
                anyString()))
                .thenReturn(new ResponseEntity<>(productoDto, HttpStatus.OK));

        // Mock para fallar al obtener cliente
        when(microserviceClient.enviarConToken(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(ClienteDto.class),
                anyString()))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.NOT_FOUND));

        // Act
        ServiceResult<ResenaDto> result = resenaService.crearResena(crearResenaDto);

        // Assert
        assertTrue(result.hasErrors());
        assertEquals("Error al obtener los datos del cliente", result.getErrors().get(0));
        verify(resenaRepository, never()).save(any(Resena.class));
    }

    @Test
    void obtenerResenasPorProducto_deberiaRetornarListaResenas() {
        // Arrange
        List<Resena> resenas = new ArrayList<>();
        resenas.add(resena);

        when(resenaRepository.findByProductoId(anyLong())).thenReturn(resenas);

        // Act
        ServiceResult<List<ResenaProductoDto>> result = resenaService.obtenerResenasPorProducto(1L);

        // Assert
        assertFalse(result.hasErrors());
        assertEquals(1, result.getData().size());
        assertEquals("Excelente producto", result.getData().get(0).getComentario());
    }

    @Test
    void obtenerTodasResenas_deberiaRetornarTodasLasResenas() {
        // Arrange
        List<Resena> resenas = new ArrayList<>();
        resenas.add(resena);

        when(resenaRepository.findAll()).thenReturn(resenas);

        // Act
        ServiceResult<List<ResenaDto>> result = resenaService.obtenerTodasResenas();

        // Assert
        assertFalse(result.hasErrors());
        assertEquals(1, result.getData().size());
        assertEquals("Excelente producto", result.getData().get(0).getComentario());
    }

    @Test
    void obtenerProducto_deberiaRetornarProductoCuandoExiste() {
        // Arrange
        String testToken = "eyJhbGciOiJIUzI1NiJ9.eyJleHBpcmF0aW9uIjoxNzUxMTc3MjE4Mzg4LCJpc3N1ZWRBdCI6MTc1MTA4MDgxODM4OCwic3ViIjoiYWRtaW4iLCJpYXQiOjE3NTEwODA4MTgsImV4cCI6MTc1MTE3NzIxOH0.gbcKLmX16MshzU09Mtx_g8E36iPxU7CgiSGji5Pamjk";
        tokenContext.setToken(testToken);
        when(microserviceClient.enviarConToken(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(ProductoDto.class),
                anyString()))
                .thenReturn(new ResponseEntity<>(productoDto, HttpStatus.OK));

        // Act
        ServiceResult<ProductoDto> result = resenaService.obtenerProducto(1L);

        // Assert
        assertFalse(result.hasErrors());
        assertEquals("Laptop", result.getData().getNombre());
    }

    @Test
    void obtenerCliente_deberiaRetornarClienteCuandoExiste() {
        // Arrange
        String testToken = "eyJhbGciOiJIUzI1NiJ9.eyJleHBpcmF0aW9uIjoxNzUxMTc3MjE4Mzg4LCJpc3N1ZWRBdCI6MTc1MTA4MDgxODM4OCwic3ViIjoiYWRtaW4iLCJpYXQiOjE3NTEwODA4MTgsImV4cCI6MTc1MTE3NzIxOH0.gbcKLmX16MshzU09Mtx_g8E36iPxU7CgiSGji5Pamjk";
        tokenContext.setToken(testToken);
        when(microserviceClient.enviarConToken(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(ClienteDto.class),
                anyString()))
                .thenReturn(new ResponseEntity<>(clienteDto, HttpStatus.OK));

        // Act
        ServiceResult<ClienteDto> result = resenaService.obtenerCliente(1L);

        // Assert
        assertFalse(result.hasErrors());
        assertEquals("Juan Perez", result.getData().getNombre());
    }
}