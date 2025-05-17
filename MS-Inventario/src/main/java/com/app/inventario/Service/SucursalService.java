package com.app.inventario.Service;
import com.app.inventario.Dto.*;
import com.app.inventario.Models.*;
import com.app.inventario.Repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SucursalService {
    private final SucursalRepository sucursalRepository;

    public ServiceResult<Sucursal> crearSucursal(SucursalDto dto) {
        List<String> errors = new ArrayList<>();
        try {
            if(dto.getNombre() == null || dto.getNombre().isBlank()) {
                errors.add("El nombre es obligatorio");
            }

            if(!errors.isEmpty()) {
                return new ServiceResult<>(errors);
            }

            Sucursal sucursal = Sucursal.builder()
                    .nombre(dto.getNombre())
                    .direccion(dto.getDireccion())
                    .ciudad(dto.getCiudad())
                    .telefono(dto.getTelefono())
                    .horarioApertura(dto.getHorarioApertura())
                    .horarioCierre(dto.getHorarioCierre())
                    .build();

            return new ServiceResult<>(sucursalRepository.save(sucursal));

        } catch(Exception e) {
            errors.add("Error: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
    }
}