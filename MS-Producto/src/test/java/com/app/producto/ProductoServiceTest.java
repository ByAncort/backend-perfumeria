package com.app.producto;

import com.app.producto.Dto.ProductoDto;
import com.app.producto.Dto.ProveedorResponse;
import com.app.producto.Models.Categoria;
import com.app.producto.Models.Producto;
import com.app.producto.Repository.CategoriaRepository;
import com.app.producto.Repository.ProductoRepository;
import com.app.producto.Service.ProductoService;
import com.app.producto.shared.MicroserviceClient;
import com.app.producto.shared.TokenContext;
import org.app.dto.ServiceResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private MicroserviceClient microserviceClient;

    @Mock
    private TokenContext tokenContext;

    @InjectMocks
    private ProductoService productoService;

    private ProductoDto productoDto;
    private Producto producto;
    private Categoria categoria;
    private ProveedorResponse proveedorResponse;

    @BeforeEach
    void setUp() {
        // Configuración de categoría
        categoria = Categoria.builder()
                .id(1L)
                .nombre("Electrónicos")
                .descripcion("Productos electrónicos")
                .build();

        // Configuración de productoDto
        productoDto = ProductoDto.builder()
                .id(1L)
                .codigoSku("SKU123")
                .nombre("Laptop")
                .descripcion("Laptop de última generación")
                .precio(BigDecimal.valueOf(1200.0))
                .costo(BigDecimal.valueOf(900.0))
                .catalogo("true")
                .serial("SER123")
                .proveedorId(1L)
                .categoriaId(1L)
                .build();

        // Configuración de producto
        producto = Producto.builder()
                .id(1L)
                .codigoSku("SKU123")
                .nombre("Laptop")
                .descripcion("Laptop de última generación")
                .precio(BigDecimal.valueOf(1200))
                .costo(BigDecimal.valueOf(900.0))
                .catalogo("true")
                .serial("SER123")
                .proveedoresId(1L)
                .categoria(categoria)
                .build();

        // Configuración de proveedor
        proveedorResponse = ProveedorResponse.builder()
                .id(1L)
                .nombre("Proveedor Tech")
                .email("contacto@tech.com")
                .rut("76.543.210-K")
                .direccion("Calle Falsa 123")
                .telefono("987654321")
                .activo(true)
                .productos(List.of(101L, 102L))
                .build();
    }



    @Test
    void crearProducto_deberiaCrearProductoCorrectamente() {
        // Arrange
        when(productoRepository.existsByCodigoSku(anyString())).thenReturn(false);
        when(productoRepository.existsBySerial(anyString())).thenReturn(false);
        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.of(categoria));

        String testToken = "eyJhbGciOiJIUzI1NiJ9.eyJleHBpcmF0aW9uIjoxNzUxMTc3MjE4Mzg4LCJpc3N1ZWRBdCI6MTc1MTA4MDgxODM4OCwic3ViIjoiYWRtaW4iLCJpYXQiOjE3NTEwODA4MTgsImV4cCI6MTc1MTE3NzIxOH0.gbcKLmX16MshzU09Mtx_G8E36iPxU7CgiSGji5Pamjk";
        tokenContext.setToken(testToken);
        when(microserviceClient.enviarConToken(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(ProveedorResponse.class),
                eq(testToken)))
                .thenReturn(new ResponseEntity<>(proveedorResponse, HttpStatus.OK));

        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        // Act
        ServiceResult<ProductoDto> result = productoService.crearProducto(productoDto);

        // Assert
        assertFalse(result.hasErrors());
        assertNotNull(result.getData());
        assertEquals("Laptop", result.getData().getNombre());

        verify(productoRepository).existsByCodigoSku(anyString());
        verify(productoRepository).existsBySerial(anyString());
        verify(categoriaRepository).findById(anyLong());
        verify(productoRepository).save(any(Producto.class));
    }
    @Test
    void consultarProveedor_deberiaRetornarProveedorCuandoExiste() {
        // Arrange
        Long proveedorId = 1L;
        String testToken = "eyJhbGciOiJIUzI1NiJ9.eyJleHBpcmF0aW9uIjoxNzUxMTc3MjE4Mzg4LCJpc3N1ZWRBdCI6MTc1MTA4MDgxODM4OCwic3ViIjoiYWRtaW4iLCJpYXQiOjE3NTEwODA4MTgsImV4cCI6MTc1MTE3NzIxOH0.gbcKLmX16MshzU09Mtx_G8E36iPxU7CgiSGji5Pamjk";
        tokenContext.setToken(testToken);
        when(microserviceClient.enviarConToken(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(ProveedorResponse.class),
                eq(testToken)))
                .thenReturn(new ResponseEntity<>(proveedorResponse, HttpStatus.OK));

        // Act
        ProveedorResponse response = productoService.consultarProveedor(proveedorId);

        // Assert
        assertNotNull(response);
        assertEquals(proveedorId, response.getId());
        verify(microserviceClient).enviarConToken(anyString(), eq(HttpMethod.GET), isNull(), eq(ProveedorResponse.class), eq(testToken));
    }
    @Test
    void crearProducto_deberiaRetornarErrorCuandoSkuExiste() {
        // Arrange
        when(productoRepository.existsByCodigoSku(anyString())).thenReturn(true);

        // Act
        ServiceResult<ProductoDto> result = productoService.crearProducto(productoDto);

        // Assert
        assertTrue(result.hasErrors());
        assertEquals("El SKU ya existe", result.getErrors().get(0));
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void listarProductos_deberiaRetornarListaDeProductos() {
        // Arrange
        when(productoRepository.findAll()).thenReturn(List.of(producto));

        // Act
        ServiceResult<List<ProductoDto>> result = productoService.listarProductos();

        // Assert
        assertFalse(result.hasErrors());
        assertEquals(1, result.getData().size());
        assertEquals("Laptop", result.getData().get(0).getNombre());
    }

    @Test
    void obtenerProducto_deberiaRetornarProductoCuandoExiste() {
        // Arrange
        Long productoId = 1L;
        when(productoRepository.findById(productoId)).thenReturn(Optional.of(producto));

        // Act
        ServiceResult<ProductoDto> result = productoService.obtenerProducto(productoId);

        // Assert
        assertFalse(result.hasErrors());
        assertEquals("Laptop", result.getData().getNombre());
    }

    @Test
    void obtenerProducto_deberiaRetornarErrorCuandoNoExiste() {
        // Arrange
        Long productoId = 99L;
        when(productoRepository.findById(productoId)).thenReturn(Optional.empty());

        // Act
        ServiceResult<ProductoDto> result = productoService.obtenerProducto(productoId);

        // Assert
        assertTrue(result.hasErrors());
        assertEquals("Producto no encontrado con ID 99", result.getErrors().get(0));
    }





    @Test
    void eliminarProducto_deberiaEliminarProductoCuandoExiste() {
        // Arrange
        Long productoId = 1L;
        when(productoRepository.existsById(productoId)).thenReturn(true);
        doNothing().when(productoRepository).deleteById(productoId);

        // Act
        ServiceResult<Void> result = productoService.eliminarProducto(productoId);

        // Assert
        assertFalse(result.hasErrors());
        verify(productoRepository).deleteById(productoId);
    }

    @Test
    void eliminarProducto_deberiaRetornarErrorCuandoNoExiste() {
        // Arrange
        Long productoId = 99L;
        when(productoRepository.existsById(productoId)).thenReturn(false);

        // Act
        ServiceResult<Void> result = productoService.eliminarProducto(productoId);

        // Assert
        assertTrue(result.hasErrors());
        assertEquals("Producto con ID 99 no existe", result.getErrors().get(0));
        verify(productoRepository, never()).deleteById(anyLong());
    }



    @Test
    void toDto_deberiaConvertirEntityADtoCorrectamente() {
        // Act
        ProductoDto result = productoService.toDto(producto);

        // Assert
        assertNotNull(result);
        assertEquals("Laptop", result.getNombre());
        assertEquals(1L, result.getCategoriaId());
    }
}