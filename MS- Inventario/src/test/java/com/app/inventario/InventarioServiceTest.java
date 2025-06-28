package com.app.inventario;

import com.app.inventario.Dto.*;
import com.app.inventario.Models.Inventario;
import com.app.inventario.Repository.InventarioRepository;
import com.app.inventario.Service.InventarioService;
import com.app.inventario.shared.MicroserviceClient;
import com.app.inventario.shared.TokenContext;
import org.app.dto.ServiceResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private MicroserviceClient microserviceClient;

    @InjectMocks
    private InventarioService inventarioService;
    @Value("${auth.url.provMicro}")
    private String PRO_SERVICE_URL;

    @Value("${auth.url.sucursalMicro}")
    private String SUCURSAL_SERVICE_URL;
    // Variable global para el token
    private static final String TEST_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJleHBpcmF0aW9uIjoxNzUxMTc3MjE4Mzg4LCJpc3N1ZWRBdCI6MTc1MTA4MDgxODM4OCwic3ViIjoiYWRtaW4iLCJpYXQiOjE3NTEwODA4MTgsImV4cCI6MTc1MTE3NzIxOH0.gbcKLmX16MshzU09Mtx_G8E36iPxU7CgiSGji5Pamjk";

    @BeforeEach
    void setUp() {
        // Configurar el token en el contexto antes de cada prueba
        TokenContext.setToken(TEST_TOKEN);
    }

    @Test
    void consultarSucursal_DeberiaRetornarSucursal() {
        // Arrange
        Long sucursalId = 1L;
        String url = SUCURSAL_SERVICE_URL+"/api/sucursales/" + sucursalId;
        SucursalResponse expectedResponse = new SucursalResponse();

        when(microserviceClient.enviarConToken(
                eq(url),
                eq(HttpMethod.GET),
                isNull(),
                eq(SucursalResponse.class),
                eq(TEST_TOKEN)))
                .thenReturn(new ResponseEntity<>(expectedResponse, HttpStatus.OK));

        // Act
        SucursalResponse result = inventarioService.consultarSucursal(sucursalId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);
    }

    @Test
    void consultarProducto_DeberiaRetornarProducto() {
        // Arrange
        Long productoId = 1L;
        String url = PRO_SERVICE_URL+"/api/productos/get/" + productoId;
        ProductoResponse expectedResponse = new ProductoResponse();

        when(microserviceClient.enviarConToken(
                eq(url),
                eq(HttpMethod.GET),
                isNull(),
                eq(ProductoResponse.class),
                eq(TEST_TOKEN)))
                .thenReturn(new ResponseEntity<>(expectedResponse, HttpStatus.OK));

        // Act
        ProductoResponse result = inventarioService.consultarProducto(productoId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);
    }

    @Test
    void registrarInventario_DeberiaRegistrarCorrectamente() {
        // Arrange
        InventarioRequest request = new InventarioRequest(1L, 1L, 10, 5);

        ProductoResponse productoResponse = new ProductoResponse();
        SucursalResponse sucursalResponse = new SucursalResponse();

        when(microserviceClient.enviarConToken(anyString(), eq(HttpMethod.GET), isNull(), eq(ProductoResponse.class), eq(TEST_TOKEN)))
                .thenReturn(new ResponseEntity<>(productoResponse, HttpStatus.OK));
        when(microserviceClient.enviarConToken(anyString(), eq(HttpMethod.GET), isNull(), eq(SucursalResponse.class), eq(TEST_TOKEN)))
                .thenReturn(new ResponseEntity<>(sucursalResponse, HttpStatus.OK));

        Inventario savedInventario = Inventario.builder()
                .id(1L)
                .productoId(request.getProductoId())
                .sucursalId(request.getSucursalId())
                .cantidad(request.getCantidad())
                .stockMinimo(request.getStockMinimo())
                .build();

        when(inventarioRepository.save(any(Inventario.class))).thenReturn(savedInventario);

        // Act
        InventarioResponse result = inventarioService.registrarInventario(request);

        // Assert
        assertNotNull(result);
        assertEquals(savedInventario.getId(), result.getId());
        assertEquals(productoResponse, result.getProducto());
        assertEquals(sucursalResponse, result.getSucursal());
        assertEquals(request.getCantidad(), result.getCantidad());
    }

    @Test
    void obtenerInventarioPorId_DeberiaRetornarInventario() {
        // Arrange
        Long inventarioId = 1L;
        Inventario inventario = Inventario.builder()
                .id(inventarioId)
                .productoId(1L)
                .sucursalId(1L)
                .cantidad(10)
                .stockMinimo(5)
                .build();

        ProductoResponse productoResponse = new ProductoResponse();
        SucursalResponse sucursalResponse = new SucursalResponse();

        when(inventarioRepository.findById(inventarioId)).thenReturn(Optional.of(inventario));
        when(microserviceClient.enviarConToken(anyString(), eq(HttpMethod.GET), isNull(), eq(ProductoResponse.class), eq(TEST_TOKEN)))
                .thenReturn(new ResponseEntity<>(productoResponse, HttpStatus.OK));
        when(microserviceClient.enviarConToken(anyString(), eq(HttpMethod.GET), isNull(), eq(SucursalResponse.class), eq(TEST_TOKEN)))
                .thenReturn(new ResponseEntity<>(sucursalResponse, HttpStatus.OK));

        // Act
        InventarioResponse result = inventarioService.obtenerInventarioPorId(inventarioId);

        // Assert
        assertNotNull(result);
        assertEquals(inventarioId, result.getId());
        assertEquals(productoResponse, result.getProducto());
        assertEquals(sucursalResponse, result.getSucursal());
    }

    @Test
    void actualizarStock_DeberiaActualizarCorrectamente() {
        // Arrange
        Long inventarioId = 1L;
        Integer nuevaCantidad = 15;

        Inventario inventario = Inventario.builder()
                .id(inventarioId)
                .productoId(1L)
                .sucursalId(1L)
                .cantidad(10)
                .stockMinimo(5)
                .build();

        ProductoResponse productoResponse = new ProductoResponse();
        SucursalResponse sucursalResponse = new SucursalResponse();

        when(inventarioRepository.findById(inventarioId)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventario);
        when(microserviceClient.enviarConToken(anyString(), eq(HttpMethod.GET), isNull(), eq(ProductoResponse.class), eq(TEST_TOKEN)))
                .thenReturn(new ResponseEntity<>(productoResponse, HttpStatus.OK));
        when(microserviceClient.enviarConToken(anyString(), eq(HttpMethod.GET), isNull(), eq(SucursalResponse.class), eq(TEST_TOKEN)))
                .thenReturn(new ResponseEntity<>(sucursalResponse, HttpStatus.OK));

        // Act
        InventarioResponse result = inventarioService.actualizarStock(inventarioId, nuevaCantidad);

        // Assert
        assertNotNull(result);
        assertEquals(nuevaCantidad, result.getCantidad());
        assertNotNull(result.getUltimaActualizacion());
    }

    @Test
    void transferirStock_DeberiaTransferirCorrectamente() {
        // Arrange
        Long origenId = 1L;
        Long destinoId = 2L;
        Long productoId = 1L;
        Integer cantidad = 5;

        Inventario inventarioOrigen = Inventario.builder()
                .id(1L)
                .productoId(productoId)
                .sucursalId(origenId)
                .cantidad(10)
                .stockMinimo(2)
                .build();

        Inventario inventarioDestino = Inventario.builder()
                .id(2L)
                .productoId(productoId)
                .sucursalId(destinoId)
                .cantidad(3)
                .stockMinimo(2)
                .build();

        ProductoResponse productoResponse = new ProductoResponse();
        SucursalResponse sucursalOrigenResponse = new SucursalResponse();
        SucursalResponse sucursalDestinoResponse = new SucursalResponse();

        when(inventarioRepository.findByProductoIdAndSucursalId(productoId, origenId))
                .thenReturn(Optional.of(inventarioOrigen));
        when(inventarioRepository.findByProductoIdAndSucursalId(productoId, destinoId))
                .thenReturn(Optional.of(inventarioDestino));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(microserviceClient.enviarConToken(anyString(), eq(HttpMethod.GET), isNull(), eq(ProductoResponse.class), eq(TEST_TOKEN)))
                .thenReturn(new ResponseEntity<>(productoResponse, HttpStatus.OK));
        when(microserviceClient.enviarConToken(contains("/sucursales/" + origenId), any(), any(), any(), any()))
                .thenReturn(new ResponseEntity<>(sucursalOrigenResponse, HttpStatus.OK));
        when(microserviceClient.enviarConToken(contains("/sucursales/" + destinoId), any(), any(), any(), any()))
                .thenReturn(new ResponseEntity<>(sucursalDestinoResponse, HttpStatus.OK));

        // Act
        InventarioResponse result = inventarioService.transferirStock(origenId, destinoId, productoId, cantidad);

        // Assert
// Assert
        assertNotNull(result);
        assertEquals(inventarioDestino.getId(), result.getId());
        assertEquals(5, inventarioOrigen.getCantidad()); // 10 - 5 = 5
        assertEquals(8, inventarioDestino.getCantidad()); // 3 + 5 = 8
        assertNotNull(result.getTransferencia());
        assertEquals(sucursalOrigenResponse, result.getTransferencia().getOrigen());
        assertEquals(cantidad, result.getTransferencia().getCantidadTransferida());
    }

    @Test
    void vender_DeberiaDisminuirStock() {
        // Arrange
        Long inventarioId = 1L;
        Inventario inventario = Inventario.builder()
                .id(inventarioId)
                .productoId(1L)
                .sucursalId(1L)
                .cantidad(10)
                .stockMinimo(2)
                .build();

        ProductoResponse productoResponse = new ProductoResponse();
        SucursalResponse sucursalResponse = new SucursalResponse();

        when(inventarioRepository.findById(inventarioId)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(microserviceClient.enviarConToken(anyString(), eq(HttpMethod.GET), isNull(), eq(ProductoResponse.class), eq(TEST_TOKEN)))
                .thenReturn(new ResponseEntity<>(productoResponse, HttpStatus.OK));
        when(microserviceClient.enviarConToken(anyString(), eq(HttpMethod.GET), isNull(), eq(SucursalResponse.class), eq(TEST_TOKEN)))
                .thenReturn(new ResponseEntity<>(sucursalResponse, HttpStatus.OK));

        // Act
        ServiceResult<InventarioResponse> result = inventarioService.vender(inventarioId);

        // Assert
        assertNotNull(result.getData());
        assertEquals(9, result.getData().getCantidad());
        assertNotNull(result.getData().getUltimaActualizacion());
    }

    @Test
    void vender_SinStock_DeberiaFallar() {
        // Arrange
        Long inventarioId = 1L;
        Inventario inventario = Inventario.builder()
                .id(inventarioId)
                .productoId(1L)
                .sucursalId(1L)
                .cantidad(0)
                .stockMinimo(2)
                .build();

        when(inventarioRepository.findById(inventarioId)).thenReturn(Optional.of(inventario));

        // Act
        ServiceResult<InventarioResponse> result = inventarioService.vender(inventarioId);

        // Assert
        assertNull(result.getData(), "No debe haber data cuando no hay stock");
        assertNotNull(result.getErrors(), "Debe contener errores");
        assertTrue(result.getErrors().contains("Sin stock disponible"), "Debe contener el error 'Sin stock disponible'");
    }

    @Test
    void cancelarVenta_DeberiaAumentarStock() {
        // Arrange
        Long inventarioId = 1L;
        Inventario inventario = Inventario.builder()
                .id(inventarioId)
                .productoId(1L)
                .sucursalId(1L)
                .cantidad(10)
                .stockMinimo(2)
                .build();

        ProductoResponse productoResponse = new ProductoResponse();
        SucursalResponse sucursalResponse = new SucursalResponse();

        when(inventarioRepository.findById(inventarioId)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(microserviceClient.enviarConToken(anyString(), eq(HttpMethod.GET), isNull(), eq(ProductoResponse.class), eq(TEST_TOKEN)))
                .thenReturn(new ResponseEntity<>(productoResponse, HttpStatus.OK));
        when(microserviceClient.enviarConToken(anyString(), eq(HttpMethod.GET), isNull(), eq(SucursalResponse.class), eq(TEST_TOKEN)))
                .thenReturn(new ResponseEntity<>(sucursalResponse, HttpStatus.OK));

        // Act
        InventarioResponse result = inventarioService.canerlarVenta(inventarioId);

        // Assert
        assertNotNull(result);
        assertEquals(11, result.getCantidad());
        assertNotNull(result.getUltimaActualizacion());
    }

    @Test
    void obtenerProductosBajoStockMinimo_DeberiaRetornarLista() {
        // Arrange
        Inventario inventario1 = Inventario.builder()
                .id(1L)
                .productoId(1L)
                .sucursalId(1L)
                .cantidad(1)
                .stockMinimo(5)
                .build();

        Inventario inventario2 = Inventario.builder()
                .id(2L)
                .productoId(2L)
                .sucursalId(1L)
                .cantidad(0)
                .stockMinimo(2)
                .build();

        ProductoResponse productoResponse1 = new ProductoResponse();
        ProductoResponse productoResponse2 = new ProductoResponse();
        SucursalResponse sucursalResponse = new SucursalResponse();

        when(inventarioRepository.findByCantidadLessThanStockMinimo()).thenReturn(List.of(inventario1, inventario2));
        when(microserviceClient.enviarConToken(contains("/productos/get/1"), any(), any(), any(), any()))
                .thenReturn(new ResponseEntity<>(productoResponse1, HttpStatus.OK));
        when(microserviceClient.enviarConToken(contains("/productos/get/2"), any(), any(), any(), any()))
                .thenReturn(new ResponseEntity<>(productoResponse2, HttpStatus.OK));
        when(microserviceClient.enviarConToken(contains("/sucursales/1"), any(), any(), any(), any()))
                .thenReturn(new ResponseEntity<>(sucursalResponse, HttpStatus.OK));

        // Act
        List<InventarioResponse> result = inventarioService.obtenerProductosBajoStockMinimo();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(r -> r.getCantidad() == 1));
        assertTrue(result.stream().anyMatch(r -> r.getCantidad() == 0));
    }
}