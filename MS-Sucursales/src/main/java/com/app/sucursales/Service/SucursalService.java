package com.app.sucursales.Service;

import com.app.sucursales.Dto.ServiceResult;
import com.app.sucursales.Dto.SucursalDto;
import com.app.sucursales.Models.Sucursal;
import com.app.sucursales.Repository.SucursalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SucursalService {

    private final SucursalRepository sucursalRepository;

    public ServiceResult<SucursalDto> crearSucursal(SucursalDto dto) {
        try {

            if (dto.getHorarioApertura() != null && dto.getHorarioCierre() != null
                    && dto.getHorarioApertura().isAfter(dto.getHorarioCierre())) {
                return new ServiceResult<>(List.of("El horario de apertura no puede ser después del horario de cierre"));
            }

            Sucursal sucursal = Sucursal.builder()
                    .nombre(dto.getNombre())
                    .direccion(dto.getDireccion())
                    .ciudad(dto.getCiudad())
                    .telefono(dto.getTelefono())
                    .horarioApertura(dto.getHorarioApertura())
                    .horarioCierre(dto.getHorarioCierre())
                    .activa(dto.getActiva() != null ? dto.getActiva() : true)
                    .build();

            sucursal = sucursalRepository.save(sucursal);
            return new ServiceResult<>(convertToDto(sucursal));
        } catch (Exception e) {
            return new ServiceResult<>(List.of("Error al crear la sucursal: " + e.getMessage()));
        }
    }

    public ServiceResult<SucursalDto> obtenerSucursalPorId(Long id) {
        try {
            Sucursal sucursal = sucursalRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));
            return new ServiceResult<>(convertToDto(sucursal));
        } catch (Exception e) {
            return new ServiceResult<>(List.of(e.getMessage()));
        }
    }

    public ServiceResult<List<SucursalDto>> listarTodasLasSucursales() {
        try {
            List<Sucursal> sucursales = sucursalRepository.findAll();
            List<SucursalDto> dtos = sucursales.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return new ServiceResult<>(dtos);
        } catch (Exception e) {
            return new ServiceResult<>(List.of("Error al listar las sucursales: " + e.getMessage()));
        }
    }

    public ServiceResult<SucursalDto> actualizarSucursal(Long id, SucursalDto dto) {
        try {
            Sucursal sucursal = sucursalRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));

            if (dto.getHorarioApertura() != null && dto.getHorarioCierre() != null
                    && dto.getHorarioApertura().isAfter(dto.getHorarioCierre())) {
                return new ServiceResult<>(List.of("El horario de apertura no puede ser después del horario de cierre"));
            }

            sucursal.setNombre(dto.getNombre() != null ? dto.getNombre() : sucursal.getNombre());
            sucursal.setDireccion(dto.getDireccion() != null ? dto.getDireccion() : sucursal.getDireccion());
            sucursal.setCiudad(dto.getCiudad() != null ? dto.getCiudad() : sucursal.getCiudad());
            sucursal.setTelefono(dto.getTelefono() != null ? dto.getTelefono() : sucursal.getTelefono());
            sucursal.setHorarioApertura(dto.getHorarioApertura() != null ? dto.getHorarioApertura() : sucursal.getHorarioApertura());
            sucursal.setHorarioCierre(dto.getHorarioCierre() != null ? dto.getHorarioCierre() : sucursal.getHorarioCierre());
            sucursal.setActiva(dto.getActiva() != null ? dto.getActiva() : sucursal.getActiva());

            sucursal = sucursalRepository.save(sucursal);
            return new ServiceResult<>(convertToDto(sucursal));
        } catch (Exception e) {
            return new ServiceResult<>(List.of("Error al actualizar la sucursal: " + e.getMessage()));
        }
    }

    public ServiceResult<SucursalDto> cambiarEstadoSucursal(Long id, boolean activa) {
        try {
            Sucursal sucursal = sucursalRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));
            sucursal.setActiva(activa);
            sucursalRepository.save(sucursal);
            return new ServiceResult<>(convertToDto(sucursal));
        } catch (Exception e) {
            return new ServiceResult<>(List.of("Error al cambiar el estado de la sucursal: " + e.getMessage()));
        }
    }

    public ServiceResult<List<SucursalDto>> buscarSucursalesActivas() {
        try {
            List<Sucursal> sucursales = sucursalRepository.findByActivaTrue();
            List<SucursalDto> dtos = sucursales.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return new ServiceResult<>(dtos);
        } catch (Exception e) {
            return new ServiceResult<>(List.of("Error al buscar sucursales activas: " + e.getMessage()));
        }
    }

    public SucursalDto convertToDto(Sucursal sucursal) {
        return SucursalDto.builder()
                .id(sucursal.getId())
                .nombre(sucursal.getNombre())
                .direccion(sucursal.getDireccion())
                .ciudad(sucursal.getCiudad())
                .telefono(sucursal.getTelefono())
                .horarioApertura(sucursal.getHorarioApertura())
                .horarioCierre(sucursal.getHorarioCierre())
                .activa(sucursal.getActiva())
                .build();
    }
}