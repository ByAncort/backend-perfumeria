package org.necronet.mscliente.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.necronet.mscliente.model.Cliente;
import org.necronet.mscliente.model.Compra;
import org.necronet.mscliente.model.Contacto;
import org.necronet.mscliente.repository.ClienteRepository;
import org.necronet.mscliente.repository.CompraRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final CompraRepository compraRepository;

    public Cliente crearCliente(Cliente cliente) {
        return clienteRepository.save(cliente);
    }


    public Cliente obtenerClientePorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
    }


    public Cliente actualizarCliente(Long id, Cliente clienteActualizado) {
        Cliente cliente = obtenerClientePorId(id);
        cliente.setNombre(clienteActualizado.getNombre());
        cliente.setApellido(clienteActualizado.getApellido());
        cliente.setDni(clienteActualizado.getDni());
        cliente.setEmail(clienteActualizado.getEmail());
        cliente.setSegmento(clienteActualizado.getSegmento());
        return clienteRepository.save(cliente);
    }


    public void eliminarCliente(Long id) {
        clienteRepository.deleteById(id);
    }


    public List<Cliente> listarTodosLosClientes() {
        return clienteRepository.findAll();
    }


    public List<Cliente> listarClientesPorSegmento(String segmento) {
        return clienteRepository.findBySegmento(segmento);
    }


    public List<Compra> obtenerHistorialCompras(Long clienteId) {
        return compraRepository.findByClienteId(clienteId);
    }


    @Transactional
    public Cliente agregarContacto(Long clienteId, Contacto contacto) {
        Cliente cliente = obtenerClientePorId(clienteId);
        contacto.setCliente(cliente);
        cliente.getContactos().add(contacto);
        return clienteRepository.save(cliente);
    }


    public Cliente actualizarSegmento(Long clienteId, String segmento) {
        Cliente cliente = obtenerClientePorId(clienteId);
        cliente.setSegmento(segmento);
        return clienteRepository.save(cliente);
    }
}
