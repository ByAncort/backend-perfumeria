package com.app.proveedores.Service;

import com.app.proveedores.Dto.ProveedorDto;
import com.app.proveedores.Models.Proveedor;
import com.app.proveedores.Models.ServiceResult;
import com.app.proveedores.Repostory.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public ServiceResult<List<ProveedorDto>> listarTodos() {
        List<Proveedor> proveedores = proveedorRepository.findAll();
        return new ServiceResult<>(
                proveedores.stream()
                        .map(this::convertToDto)
                        .toList()
        );
    }

    public ServiceResult<ProveedorDto> buscarPorId(Long id) {
        return proveedorRepository.findById(id)
                .map(proveedor -> new ServiceResult<>(convertToDto(proveedor)))
                .orElseGet(() -> new ServiceResult<>(List.of("Proveedor no encontrado")));
    }

    public ServiceResult<ProveedorDto> crearProveedor(ProveedorDto proveedorDto) {
        if (proveedorRepository.existsByRuc(proveedorDto.getRuc())) {
            return new ServiceResult<>(List.of("El RUC ya está registrado"));
        }

        Proveedor proveedor = convertToEntity(proveedorDto);
        Proveedor saved = proveedorRepository.save(Proveedor.builder()
                        .ruc(proveedorDto.getRuc())
                        .nombre(proveedorDto.getNombre())
                        .direccion(proveedorDto.getDireccion())
                        .contactoPrincipal(proveedorDto.getContactoPrincipal())
                        .telefono(proveedorDto.getTelefono())
                        .email(proveedorDto.getEmail())
                        .activo(proveedorDto.isActivo())
                .build());
        return new ServiceResult<>(convertToDto(saved));
    }

    public ServiceResult<ProveedorDto> actualizarProveedor(Long id, ProveedorDto proveedorDto) {
        return proveedorRepository.findById(id)
                .map(existing -> {
                    if (!existing.getRuc().equals(proveedorDto.getRuc()) &&
                            proveedorRepository.existsByRuc(proveedorDto.getRuc())) {
                        return new ServiceResult<ProveedorDto>(List.of("El RUC ya está registrado"));
                    }

                    updateEntity(existing, proveedorDto);
                    Proveedor updated = proveedorRepository.save(existing);
                    return new ServiceResult<>(convertToDto(updated));
                })
                .orElseGet(() -> new ServiceResult<ProveedorDto>(List.of("Proveedor no encontrado")));
    }


    public ServiceResult<String> eliminarProveedor(Long id) {
        return proveedorRepository.findById(id)
                .map(proveedor -> {
                    proveedorRepository.delete(proveedor);
                    return new ServiceResult<>("Eliminado");
                })
                .orElseGet(() -> new ServiceResult<String>(List.of("Proveedor no encontrado")));
    }


    private ProveedorDto convertToDto(Proveedor proveedor) {
        return ProveedorDto.builder()
                .id(proveedor.getId())
                .ruc(proveedor.getRuc())
                .nombre(proveedor.getNombre())
                .direccion(proveedor.getDireccion())
                .contactoPrincipal(proveedor.getContactoPrincipal())
                .telefono(proveedor.getTelefono())
                .email(proveedor.getEmail())
                .activo(proveedor.getActivo())
                .build();
    }

    private Proveedor convertToEntity(ProveedorDto dto) {
        return Proveedor.builder()
                .ruc(dto.getRuc())
                .nombre(dto.getNombre())
                .direccion(dto.getDireccion())
                .contactoPrincipal(dto.getContactoPrincipal())
                .telefono(dto.getTelefono())
                .email(dto.getEmail())
                .activo(dto.isActivo())
                .build();
    }

    private void updateEntity(Proveedor proveedor, ProveedorDto dto) {
        proveedor.setNombre(dto.getNombre());
        proveedor.setDireccion(dto.getDireccion());
        proveedor.setContactoPrincipal(dto.getContactoPrincipal());
        proveedor.setTelefono(dto.getTelefono());
        proveedor.setEmail(dto.getEmail());
        proveedor.setActivo(dto.isActivo());
    }
}
