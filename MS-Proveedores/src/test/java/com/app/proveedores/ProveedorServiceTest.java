package com.app.proveedores;

import com.app.proveedores.Dto.ProveedorDto;
import com.app.proveedores.Dto.ServiceResult;
import com.app.proveedores.Models.Proveedor;
import com.app.proveedores.Repository.ProveedorRepository;
import com.app.proveedores.Service.ProveedorService;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProveedorServiceTest {

    @Mock
    private ProveedorRepository proveedorRepository;

    @InjectMocks
    private ProveedorService proveedorService;

    private Faker faker;
    private ProveedorDto proveedorDtoValido;
    private Proveedor proveedorValido;

    @BeforeEach
    void setUp() {
        faker = new Faker();

        
        proveedorDtoValido = new ProveedorDto();
        proveedorDtoValido.setNombre(faker.company().name());
        proveedorDtoValido.setRut(generarRutValido());
        proveedorDtoValido.setDireccion(faker.address().fullAddress());
        proveedorDtoValido.setEmail(faker.internet().emailAddress());
        proveedorDtoValido.setTelefono("+569" + faker.number().digits(8));

        
        proveedorValido = new Proveedor();
        proveedorValido.setId(1L);
        proveedorValido.setNombre(proveedorDtoValido.getNombre());
        proveedorValido.setRut(proveedorService.normalizarRut(proveedorDtoValido.getRut()));
        proveedorValido.setDireccion(proveedorDtoValido.getDireccion());
        proveedorValido.setEmail(proveedorDtoValido.getEmail());
        proveedorValido.setTelefono(proveedorDtoValido.getTelefono());
        proveedorValido.setActivo(true);
    }

    private String generarRutValido() {
        int num = faker.number().numberBetween(1000000, 25000000);
        String cuerpo = String.valueOf(num);
        char[] digito = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'K'};

        int suma = 0;
        int multiplicador = 2;

        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * multiplicador;
            multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
        }

        int resto = suma % 11;
        int dvCalculado = 11 - resto;
        char dv = dvCalculado == 11 ? '0' : dvCalculado == 10 ? 'K' : digito[dvCalculado];

        return cuerpo + "-" + dv;
    }

    @Test
    void addProveedor_ConDatosValidos_RetornaProveedorGuardado() {
        
        when(proveedorRepository.existsByRut(anyString())).thenReturn(false);
        when(proveedorRepository.save(any(Proveedor.class))).thenReturn(proveedorValido);

        
        ServiceResult<Proveedor> resultado = proveedorService.addProveedor(proveedorDtoValido);

        
        assertTrue(resultado.getErrors().isEmpty());
        assertNotNull(resultado.getData());
        assertEquals(proveedorValido.getNombre(), resultado.getData().getNombre());
        verify(proveedorRepository, times(1)).save(any(Proveedor.class));
    }

    @Test
    void addProveedor_ConRutInvalido_RetornaError() {
        
        proveedorDtoValido.setRut("12345678-0"); 

        
        ServiceResult<Proveedor> resultado = proveedorService.addProveedor(proveedorDtoValido);

        
        assertFalse(resultado.getErrors().isEmpty());
        assertTrue(resultado.getErrors().get(0).contains("RUT no es válido"));
        verify(proveedorRepository, never()).save(any(Proveedor.class));
    }

    @Test
    void addProveedor_ConRutExistente_RetornaError() {
        
        when(proveedorRepository.existsByRut(anyString())).thenReturn(true);

        
        ServiceResult<Proveedor> resultado = proveedorService.addProveedor(proveedorDtoValido);

        
        assertFalse(resultado.getErrors().isEmpty());
        assertTrue(resultado.getErrors().get(0).contains("RUT ya está registrado"));
        verify(proveedorRepository, never()).save(any(Proveedor.class));
    }

    @Test
    void addProveedor_ConEmailInvalido_RetornaError() {
        
        proveedorDtoValido.setEmail("emailinvalido");

        
        ServiceResult<Proveedor> resultado = proveedorService.addProveedor(proveedorDtoValido);

        
        assertFalse(resultado.getErrors().isEmpty());
        assertTrue(resultado.getErrors().get(0).contains("formato del email es inválido"));
        verify(proveedorRepository, never()).save(any(Proveedor.class));
    }

    @Test
    void addProveedor_ConTelefonoInvalido_RetornaError() {
        
        proveedorDtoValido.setTelefono("123456789");

        
        ServiceResult<Proveedor> resultado = proveedorService.addProveedor(proveedorDtoValido);

        
        assertFalse(resultado.getErrors().isEmpty());
        assertTrue(resultado.getErrors().get(0).contains("formato del teléfono es inválido"));
        verify(proveedorRepository, never()).save(any(Proveedor.class));
    }

    @Test
    void getAllProveedoresActivos_RetornaListaProveedores() {
        
        List<Proveedor> proveedores = List.of(proveedorValido);
        when(proveedorRepository.findByActivoTrue()).thenReturn(proveedores);

        
        ServiceResult<List<Proveedor>> resultado = proveedorService.getAllProveedoresActivos();

        
        assertTrue(resultado.getErrors().isEmpty());
        assertEquals(1, resultado.getData().size());
        verify(proveedorRepository, times(1)).findByActivoTrue();
    }

    @Test
    void getProveedorById_ConIdExistente_RetornaProveedor() {
        
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedorValido));

        
        ServiceResult<Proveedor> resultado = proveedorService.getProveedorById(1L);

        
        assertTrue(resultado.getErrors().isEmpty());
        assertEquals(proveedorValido.getId(), resultado.getData().getId());
        verify(proveedorRepository, times(1)).findById(1L);
    }

    @Test
    void getProveedorById_ConIdInexistente_RetornaError() {
        
        when(proveedorRepository.findById(99L)).thenReturn(Optional.empty());

        
        ServiceResult<Proveedor> resultado = proveedorService.getProveedorById(99L);

        
        assertFalse(resultado.getErrors().isEmpty());
        assertTrue(resultado.getErrors().get(0).contains("no encontrado"));
        verify(proveedorRepository, times(1)).findById(99L);
    }

    @Test
    void updateProveedor_ConDatosValidos_RetornaProveedorActualizado() {
        
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedorValido));
        when(proveedorRepository.save(any(Proveedor.class))).thenReturn(proveedorValido);

        
        proveedorDtoValido.setNombre("Nuevo Nombre");
        proveedorDtoValido.setDireccion("Nueva Dirección");

        
        ServiceResult<Proveedor> resultado = proveedorService.updateProveedor(1L, proveedorDtoValido);

        
        assertTrue(resultado.getErrors().isEmpty());
        assertEquals("Nuevo Nombre", resultado.getData().getNombre());
        assertEquals("Nueva Dirección", resultado.getData().getDireccion());
        verify(proveedorRepository, times(1)).save(any(Proveedor.class));
    }

    @Test
    void updateProveedor_ConRutModificado_RetornaError() {
        
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedorValido));
        proveedorDtoValido.setRut(generarRutValido()); 

        
        ServiceResult<Proveedor> resultado = proveedorService.updateProveedor(1L, proveedorDtoValido);

        
        assertFalse(resultado.getErrors().isEmpty());
        assertTrue(resultado.getErrors().get(0).contains("No se puede modificar el RUT"));
        verify(proveedorRepository, never()).save(any(Proveedor.class));
    }

    @Test
    void toggleActivoProveedor_ConIdExistente_RetornaProveedorActualizado() {
        
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedorValido));
        when(proveedorRepository.save(any(Proveedor.class))).thenReturn(proveedorValido);

        
        ServiceResult<Proveedor> resultado = proveedorService.toggleActivoProveedor(1L, false);

        
        assertTrue(resultado.getErrors().isEmpty());
        assertFalse(resultado.getData().isActivo());
        verify(proveedorRepository, times(1)).save(any(Proveedor.class));
    }

    @Test
    void getProveedorByRut_ConRutValido_RetornaProveedor() {
        
        String rutNormalizado = proveedorService.normalizarRut(proveedorDtoValido.getRut());
        when(proveedorRepository.findByRut(rutNormalizado)).thenReturn(Optional.of(proveedorValido));

        
        ServiceResult<Proveedor> resultado = proveedorService.getProveedorByRut(proveedorDtoValido.getRut());

        
        assertTrue(resultado.getErrors().isEmpty());
        assertEquals(proveedorValido.getId(), resultado.getData().getId());
        verify(proveedorRepository, times(1)).findByRut(rutNormalizado);
    }

    @Test
    void deleteProveedor_ConIdExistente_RetornaProveedorEliminado() {
        
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedorValido));
        doNothing().when(proveedorRepository).delete(proveedorValido);

        
        ServiceResult<Proveedor> resultado = proveedorService.deleteProveedor(1L);

        
        assertTrue(resultado.getErrors().isEmpty());
        assertEquals(proveedorValido.getId(), resultado.getData().getId());
        verify(proveedorRepository, times(1)).delete(proveedorValido);
    }

    @Test
    void deleteProveedor_ConIdInexistente_RetornaError() {
        
        when(proveedorRepository.findById(99L)).thenReturn(Optional.empty());

        
        ServiceResult<Proveedor> resultado = proveedorService.deleteProveedor(99L);

        
        assertFalse(resultado.getErrors().isEmpty());
        assertTrue(resultado.getErrors().get(0).contains("no encontrado"));
        verify(proveedorRepository, never()).delete(any(Proveedor.class));
    }

    @Test
    void validarRutChileno_ConRutValido_RetornaTrue() {
        
        String rutValido = generarRutValido();

        
        boolean resultado = proveedorService.validarRutChileno(rutValido);

        
        assertTrue(resultado);
    }

    @Test
    void normalizarRut_ConRutConPuntosYGuion_RetornaRutLimpio() {
        
        String rutConFormato = "12.345.678-5";
        String rutEsperado = "12345678-5";

        String resultado = proveedorService.normalizarRut(rutConFormato);

        
        assertEquals(rutEsperado, resultado);
    }
}