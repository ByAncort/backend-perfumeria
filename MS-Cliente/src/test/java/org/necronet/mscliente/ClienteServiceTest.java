package org.necronet.mscliente;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.necronet.mscliente.Service.ClienteService;
import org.necronet.mscliente.model.Cliente;
import org.necronet.mscliente.model.Compra;
import org.necronet.mscliente.model.Contacto;
import org.necronet.mscliente.repository.ClienteRepository;
import org.necronet.mscliente.repository.CompraRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private CompraRepository compraRepository;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente cliente;
    private Contacto contacto;
    private Compra compra;

    @BeforeEach
    void setUp() {
        
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Juan");
        cliente.setApellido("Perez");
        cliente.setDni("12345678");
        cliente.setEmail("juan@example.com");
        cliente.setSegmento("PREMIUM");

        contacto = new Contacto();
        contacto.setId(1L);


        compra = new Compra();
        compra.setId(1L);

        compra.setMonto(1000.0);
    }

    @Test
    void crearCliente_deberiaRetornarClienteCreado() {
        
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        
        Cliente resultado = clienteService.crearCliente(cliente);

        
        assertNotNull(resultado);
        assertEquals("Juan", resultado.getNombre());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    void obtenerClientePorId_deberiaRetornarClienteCuandoExiste() {
        
        when(clienteRepository.findById(anyLong())).thenReturn(Optional.of(cliente));

        
        Cliente resultado = clienteService.obtenerClientePorId(1L);

        
        assertNotNull(resultado);
        assertEquals("Perez", resultado.getApellido());
    }

    @Test
    void obtenerClientePorId_deberiaLanzarExcepcionCuandoNoExiste() {
        
        when(clienteRepository.findById(anyLong())).thenReturn(Optional.empty());

        
        assertThrows(RuntimeException.class, () -> clienteService.obtenerClientePorId(99L));
    }

    @Test
    void actualizarCliente_deberiaActualizarDatosCorrectamente() {
        
        Cliente clienteActualizado = new Cliente();
        clienteActualizado.setNombre("Juan Carlos");
        clienteActualizado.setApellido("Perez Gomez");
        clienteActualizado.setDni("87654321");
        clienteActualizado.setEmail("juan.carlos@example.com");
        clienteActualizado.setSegmento("VIP");

        when(clienteRepository.findById(anyLong())).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteActualizado);

        
        Cliente resultado = clienteService.actualizarCliente(1L, clienteActualizado);

        
        assertNotNull(resultado);
        assertEquals("Juan Carlos", resultado.getNombre());
        assertEquals("VIP", resultado.getSegmento());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    void eliminarCliente_deberiaEliminarClienteExistente() {
        
        doNothing().when(clienteRepository).deleteById(anyLong());

        
        clienteService.eliminarCliente(1L);

        
        verify(clienteRepository, times(1)).deleteById(1L);
    }

    @Test
    void listarTodosLosClientes_deberiaRetornarListaClientes() {
        
        when(clienteRepository.findAll()).thenReturn(Arrays.asList(cliente));

        
        List<Cliente> resultado = clienteService.listarTodosLosClientes();

        
        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
        assertEquals("Perez", resultado.get(0).getApellido());
    }

    @Test
    void listarClientesPorSegmento_deberiaRetornarClientesFiltrados() {
        
        when(clienteRepository.findBySegmento(anyString())).thenReturn(Arrays.asList(cliente));

        
        List<Cliente> resultado = clienteService.listarClientesPorSegmento("PREMIUM");

        
        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
        assertEquals("PREMIUM", resultado.get(0).getSegmento());
    }

    @Test
    void obtenerHistorialCompras_deberiaRetornarComprasDelCliente() {
        
        when(compraRepository.findByClienteId(anyLong())).thenReturn(Arrays.asList(compra));

        
        List<Compra> resultado = clienteService.obtenerHistorialCompras(1L);

        
        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
        assertEquals(1000.0, resultado.get(0).getMonto());
    }

    @Test
    void agregarContacto_deberiaAgregarContactoAlCliente() {
        
        when(clienteRepository.findById(anyLong())).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        
        Cliente resultado = clienteService.agregarContacto(1L, contacto);

        
        assertNotNull(resultado);
        assertFalse(resultado.getContactos().isEmpty());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    void actualizarSegmento_deberiaActualizarSegmentoCorrectamente() {
        
        when(clienteRepository.findById(anyLong())).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        
        Cliente resultado = clienteService.actualizarSegmento(1L, "VIP");

        
        assertNotNull(resultado);
        assertEquals("VIP", resultado.getSegmento());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }
}