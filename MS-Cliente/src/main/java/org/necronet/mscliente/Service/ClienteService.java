package org.necronet.mscliente.Service;
import org.necronet.mscliente.model.Cliente;
import org.necronet.mscliente.model.Compra;
import org.necronet.mscliente.model.Contacto;

import java.util.List;

public interface ClienteService {
    Cliente crearCliente(Cliente cliente);
    Cliente obtenerClientePorId(Long id);
    Cliente actualizarCliente(Long id, Cliente cliente);
    void eliminarCliente(Long id);
    List<Cliente> listarTodosLosClientes();
    List<Cliente> listarClientesPorSegmento(String segmento);
    List<Compra> obtenerHistorialCompras(Long clienteId);
    Cliente agregarContacto(Long clienteId, Contacto contacto);
    Cliente actualizarSegmento(Long clienteId, String segmento);
}
