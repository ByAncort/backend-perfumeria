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
        
        when(microserviceClient.enviarConToken(anyString(), eq(HttpMethod.GET), isNull(), eq(InventarioDto.class), anyString()))
                .thenReturn(new ResponseEntity<>(inventarioDto, HttpStatus.OK));

        when(microserviceClient.enviarConToken(contains("coupons"), eq(HttpMethod.GET), isNull(), eq(CouponDto.class), anyString()))
                .thenReturn(new ResponseEntity<>(couponDto, HttpStatus.OK));

        when(carroRepository.save(any(Carro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        
        ServiceResult<CarroResponse> result = carroService.agregarProductosAlCarro(carroRequest);

        
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
        
        when(microserviceClient.enviarConToken(anyString(), eq(HttpMethod.GET), isNull(), eq(InventarioDto.class), anyString()))
                .thenThrow(new RuntimeException("Producto no disponible"));

        
        ServiceResult<CarroResponse> result = carroService.agregarProductosAlCarro(carroRequest);

        
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("Producto no disponible"));
    }

    @Test
    void agregarProductosAlCarro_StockInsuficiente() {
        
        inventarioDto.setCantidad(0);

        
        when(microserviceClient.enviarConToken(anyString(), eq(HttpMethod.GET), isNull(), eq(InventarioDto.class), anyString()))
                .thenReturn(new ResponseEntity<>(inventarioDto, HttpStatus.OK));

        
        ServiceResult<CarroResponse> result = carroService.agregarProductosAlCarro(carroRequest);

        
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("Stock insuficiente"));
    }

    @Test
    void aplicarCuponACarro_Success() {
        
        Long carroId = faker.number().randomNumber();
        String codigoCupon = "DESC20";
        Carro carroExistente = crearCarroDePrueba(carroId, "ACTIVO");

        
        when(carroRepository.findById(carroId)).thenReturn(Optional.of(carroExistente));
        when(microserviceClient.enviarConToken(contains("coupons"), eq(HttpMethod.GET), isNull(), eq(CouponDto.class), anyString()))
                .thenReturn(new ResponseEntity<>(couponDto, HttpStatus.OK));
        when(carroRepository.save(any(Carro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        
        ServiceResult<?> result = carroService.aplicarCuponACarro(carroId, codigoCupon);

        
        assertFalse(result.hasErrors());
        assertTrue(result.getData() instanceof CarroResponse);
        CarroResponse response = (CarroResponse) result.getData();
        assertEquals(codigoCupon, response.getCodigoCupon());
        assertTrue(response.getDescuento() > 0);
    }

    @Test
    void aplicarCuponACarro_CarroNoEncontrado() {
        
        Long carroId = faker.number().randomNumber();
        String codigoCupon = "DESC20";

        
        when(carroRepository.findById(carroId)).thenReturn(Optional.empty());

        
        ServiceResult<?> result = carroService.aplicarCuponACarro(carroId, codigoCupon);

        
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("Carro no encontrado"));
    }

    @Test
    void aplicarCuponACarro_CarroCompletado() {
        
        Long carroId = faker.number().randomNumber();
        String codigoCupon = "DESC20";
        Carro carroExistente = crearCarroDePrueba(carroId, "COMPLETADO");

        
        when(carroRepository.findById(carroId)).thenReturn(Optional.of(carroExistente));

        
        ServiceResult<?> result = carroService.aplicarCuponACarro(carroId, codigoCupon);

        
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("No se puede aplicar cupón a un carro completado"));
    }

    @Test
    void vaciarCarro_Success() {
        
        Long carroId = faker.number().randomNumber();
        Carro carroExistente = crearCarroDePrueba(carroId, "ACTIVO");

        
        when(carroRepository.findById(carroId)).thenReturn(Optional.of(carroExistente));
        when(carroRepository.save(any(Carro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        
        ServiceResult<Void> result = carroService.vaciarCarro(carroId);

        
        assertFalse(result.hasErrors());
        verify(carroRepository, times(1)).save(argThat(carro ->
                "VACIO".equals(carro.getEstado()) &&
                        carro.getDetalles().isEmpty() &&
                        carro.getTotal() == 0.0
        ));
    }

    @Test
    void listarCarrosPorUsuario_Success() {
        
        Long usuarioId = faker.number().randomNumber();
        List<Carro> carros = Arrays.asList(
                crearCarroDePrueba(faker.number().randomNumber(), "ACTIVO"),
                crearCarroDePrueba(faker.number().randomNumber(), "COMPLETADO")
        );

        
        when(carroRepository.findByUsuarioId(usuarioId)).thenReturn(carros);

        
        ServiceResult<List<CarroResponse>> result = carroService.listarCarrosPorUsuario(usuarioId);

        
        assertFalse(result.hasErrors());
        assertEquals(2, result.getData().size());
    }

    @Test
    void confirmarCompra_Success() {
        
        Long carroId = faker.number().randomNumber();
        Carro carroExistente = crearCarroDePrueba(carroId, "ACTIVO");

        
        when(carroRepository.findById(carroId)).thenReturn(Optional.of(carroExistente));
        when(microserviceClient.enviarConToken(anyString(), eq(HttpMethod.POST), isNull(), eq(InventarioResponse.class), anyString()))
                .thenReturn(new ResponseEntity<>(new InventarioResponse(), HttpStatus.OK));
        when(carroRepository.save(any(Carro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        
        ServiceResult<CarroResponse> result = carroService.confirmarCompra(carroId);

        
        assertFalse(result.hasErrors());
        assertEquals("COMPLETADO", result.getData().getEstado());
        verify(microserviceClient, times(carroExistente.getDetalles().size()))
                .enviarConToken(anyString(), eq(HttpMethod.POST), isNull(), eq(InventarioResponse.class), anyString());
    }

    @Test
    void confirmarCompra_CarroVacio() {
        
        Long carroId = faker.number().randomNumber();
        Carro carroExistente = crearCarroDePrueba(carroId, "ACTIVO");
        carroExistente.getDetalles().clear(); 

        
        when(carroRepository.findById(carroId)).thenReturn(Optional.of(carroExistente));

        
        ServiceResult<CarroResponse> result = carroService.confirmarCompra(carroId);

        
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