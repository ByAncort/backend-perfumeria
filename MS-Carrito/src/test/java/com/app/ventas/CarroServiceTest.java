package com.app.ventas;

import com.app.ventas.Dto.*;
import com.app.ventas.Models.Carro;
import com.app.ventas.Models.DetalleCarro;
import com.app.ventas.Repository.CarroRepository;
import com.app.ventas.Service.CarroService;
import com.app.ventas.shared.MicroserviceClient;
import com.app.ventas.shared.TokenContext;
import net.datafaker.Faker;
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
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CarroServiceTest {

    @Mock
    private CarroRepository carroRepository;

    @Mock
    private MicroserviceClient microserviceClient;

    @Mock
    private TokenContext tokenContext;

    @InjectMocks
    private CarroService carroService;

    private Faker faker;
    private CarroRequest carroRequest;
    private InventarioDto inventarioDto;
    private CouponDto couponDto;

    @BeforeEach
    void setUp() {
        faker = new Faker();

        // Configurar datos de prueba comunes
        carroRequest = new CarroRequest();
        carroRequest.setUsuarioId(faker.number().randomNumber());
        carroRequest.setCodigoCupon("DESC20");

        CarroRequest.DetalleCarroRequest detalle = new CarroRequest.DetalleCarroRequest();
        detalle.setInventarioId(faker.number().randomNumber());
        detalle.setCantidad(faker.number().numberBetween(1, 5));
        carroRequest.setDetalles(List.of(detalle));

        inventarioDto = new InventarioDto();
        inventarioDto.setCantidad(10);
        InventarioDto.ProductoDto producto = new InventarioDto.ProductoDto();
        producto.setId(detalle.getInventarioId());
        producto.setNombre(faker.commerce().productName());
        producto.setPrecio(Double.parseDouble(faker.commerce().price().replace(",", ".")));
        inventarioDto.setProducto(producto);

        couponDto = new CouponDto();
        couponDto.setCode("DESC20");
        couponDto.setDiscountType("PERCENTAGE");
        couponDto.setDiscountValue(BigDecimal.valueOf(20));

    }

    @Test
    void agregarProductosAlCarro_Success() {
        // Mockear respuestas de servicios externos
        when(microserviceClient.enviarConToken(anyString(), eq(HttpMethod.GET), isNull(), eq(InventarioDto.class), anyString()))
                .thenReturn(new ResponseEntity<>(inventarioDto, HttpStatus.OK));

        when(microserviceClient.enviarConToken(contains("coupons"), eq(HttpMethod.GET), isNull(), eq(CouponDto.class), anyString()))
                .thenReturn(new ResponseEntity<>(couponDto, HttpStatus.OK));

        when(carroRepository.save(any(Carro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Ejecutar método bajo prueba
        ServiceResult<CarroResponse> result = carroService.agregarProductosAlCarro(carroRequest);

        // Verificaciones
        assertFalse(result.hasErrors());
        assertNotNull(result.getData());
        assertEquals(carroRequest.getUsuarioId(), result.getData().getUsuarioId());
        assertEquals("DESC20", result.getData().getCodigoCupon());
        assertEquals(1, result.getData().getDetalles().size());
        assertTrue(result.getData().getTotal() > 0);
        assertTrue(result.getData().getDescuento() > 0);
    }

    @Test
    void agregarProductosAlCarro_ProductoNoDisponible() {
        // Mockear error al obtener producto
        when(microserviceClient.enviarConToken(anyString(), eq(HttpMethod.GET), isNull(), eq(InventarioDto.class), anyString()))
                .thenThrow(new RuntimeException("Producto no disponible"));

        // Ejecutar método bajo prueba
        ServiceResult<CarroResponse> result = carroService.agregarProductosAlCarro(carroRequest);

        // Verificaciones
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("Producto no disponible"));
    }

    @Test
    void agregarProductosAlCarro_StockInsuficiente() {
        // Configurar producto con stock insuficiente
        inventarioDto.setCantidad(0);

        // Mockear respuestas
        when(microserviceClient.enviarConToken(anyString(), eq(HttpMethod.GET), isNull(), eq(InventarioDto.class), anyString()))
                .thenReturn(new ResponseEntity<>(inventarioDto, HttpStatus.OK));

        // Ejecutar método bajo prueba
        ServiceResult<CarroResponse> result = carroService.agregarProductosAlCarro(carroRequest);

        // Verificaciones
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("Stock insuficiente"));
    }

    @Test
    void aplicarCuponACarro_Success() {
        // Configurar datos de prueba
        Long carroId = faker.number().randomNumber();
        String codigoCupon = "DESC20";
        Carro carroExistente = crearCarroDePrueba(carroId, "ACTIVO");

        // Mockear respuestas
        when(carroRepository.findById(carroId)).thenReturn(Optional.of(carroExistente));
        when(microserviceClient.enviarConToken(contains("coupons"), eq(HttpMethod.GET), isNull(), eq(CouponDto.class), anyString()))
                .thenReturn(new ResponseEntity<>(couponDto, HttpStatus.OK));
        when(carroRepository.save(any(Carro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Ejecutar método bajo prueba
        ServiceResult<?> result = carroService.aplicarCuponACarro(carroId, codigoCupon);

        // Verificaciones
        assertFalse(result.hasErrors());
        assertTrue(result.getData() instanceof CarroResponse);
        CarroResponse response = (CarroResponse) result.getData();
        assertEquals(codigoCupon, response.getCodigoCupon());
        assertTrue(response.getDescuento() > 0);
    }

    @Test
    void aplicarCuponACarro_CarroNoEncontrado() {
        // Configurar datos de prueba
        Long carroId = faker.number().randomNumber();
        String codigoCupon = "DESC20";

        // Mockear respuestas
        when(carroRepository.findById(carroId)).thenReturn(Optional.empty());

        // Ejecutar método bajo prueba
        ServiceResult<?> result = carroService.aplicarCuponACarro(carroId, codigoCupon);

        // Verificaciones
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("Carro no encontrado"));
    }

    @Test
    void aplicarCuponACarro_CarroCompletado() {
        // Configurar datos de prueba
        Long carroId = faker.number().randomNumber();
        String codigoCupon = "DESC20";
        Carro carroExistente = crearCarroDePrueba(carroId, "COMPLETADO");

        // Mockear respuestas
        when(carroRepository.findById(carroId)).thenReturn(Optional.of(carroExistente));

        // Ejecutar método bajo prueba
        ServiceResult<?> result = carroService.aplicarCuponACarro(carroId, codigoCupon);

        // Verificaciones
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("No se puede aplicar cupón a un carro completado"));
    }

    @Test
    void vaciarCarro_Success() {
        // Configurar datos de prueba
        Long carroId = faker.number().randomNumber();
        Carro carroExistente = crearCarroDePrueba(carroId, "ACTIVO");

        // Mockear respuestas
        when(carroRepository.findById(carroId)).thenReturn(Optional.of(carroExistente));
        when(carroRepository.save(any(Carro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Ejecutar método bajo prueba
        ServiceResult<Void> result = carroService.vaciarCarro(carroId);

        // Verificaciones
        assertFalse(result.hasErrors());
        verify(carroRepository, times(1)).save(argThat(carro ->
                "VACIO".equals(carro.getEstado()) &&
                        carro.getDetalles().isEmpty() &&
                        carro.getTotal() == 0.0
        ));
    }

    @Test
    void listarCarrosPorUsuario_Success() {
        // Configurar datos de prueba
        Long usuarioId = faker.number().randomNumber();
        List<Carro> carros = Arrays.asList(
                crearCarroDePrueba(faker.number().randomNumber(), "ACTIVO"),
                crearCarroDePrueba(faker.number().randomNumber(), "COMPLETADO")
        );

        // Mockear respuestas
        when(carroRepository.findByUsuarioId(usuarioId)).thenReturn(carros);

        // Ejecutar método bajo prueba
        ServiceResult<List<CarroResponse>> result = carroService.listarCarrosPorUsuario(usuarioId);

        // Verificaciones
        assertFalse(result.hasErrors());
        assertEquals(2, result.getData().size());
    }

    @Test
    void confirmarCompra_Success() {
        // Configurar datos de prueba
        Long carroId = faker.number().randomNumber();
        Carro carroExistente = crearCarroDePrueba(carroId, "ACTIVO");

        // Mockear respuestas
        when(carroRepository.findById(carroId)).thenReturn(Optional.of(carroExistente));
        when(microserviceClient.enviarConToken(anyString(), eq(HttpMethod.POST), isNull(), eq(InventarioResponse.class), anyString()))
                .thenReturn(new ResponseEntity<>(new InventarioResponse(), HttpStatus.OK));
        when(carroRepository.save(any(Carro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Ejecutar método bajo prueba
        ServiceResult<CarroResponse> result = carroService.confirmarCompra(carroId);

        // Verificaciones
        assertFalse(result.hasErrors());
        assertEquals("COMPLETADO", result.getData().getEstado());
        verify(microserviceClient, times(carroExistente.getDetalles().size()))
                .enviarConToken(anyString(), eq(HttpMethod.POST), isNull(), eq(InventarioResponse.class), anyString());
    }

    @Test
    void confirmarCompra_CarroVacio() {
        // Configurar datos de prueba
        Long carroId = faker.number().randomNumber();
        Carro carroExistente = crearCarroDePrueba(carroId, "ACTIVO");
        carroExistente.getDetalles().clear(); // Vaciar detalles

        // Mockear respuestas
        when(carroRepository.findById(carroId)).thenReturn(Optional.of(carroExistente));

        // Ejecutar método bajo prueba
        ServiceResult<CarroResponse> result = carroService.confirmarCompra(carroId);

        // Verificaciones
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("No se puede confirmar un carro vacío"));
    }

    private Carro crearCarroDePrueba(Long id, String estado) {
        DetalleCarro detalle = new DetalleCarro();
        detalle.setProductoId(faker.number().randomNumber());
        detalle.setCantidad(faker.number().numberBetween(1, 5));
        detalle.setPrecioUnitario(Double.parseDouble(faker.commerce().price().replace(",", ".")));
        detalle.setSubtotal(detalle.getPrecioUnitario() * detalle.getCantidad());

        Carro carro = new Carro();
        carro.setId(id);
        carro.setUsuarioId(faker.number().randomNumber());
        carro.setEstado(estado);
        carro.setDetalles(List.of(detalle));
        carro.setSubtotal(detalle.getSubtotal());
        carro.setTotal(detalle.getSubtotal());
        carro.setFechaCreacion(LocalDateTime.now());

        return carro;
    }
}