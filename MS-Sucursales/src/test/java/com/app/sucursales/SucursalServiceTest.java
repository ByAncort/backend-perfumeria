package com.app.sucursales;

import com.app.sucursales.Dto.ServiceResult;
import com.app.sucursales.Dto.SucursalDto;
import com.app.sucursales.Models.Sucursal;
import com.app.sucursales.Repository.SucursalRepository;
import com.app.sucursales.Service.SucursalService;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SucursalServiceTest {

    @Mock
    private SucursalRepository sucursalRepository;

    @InjectMocks
    private SucursalService sucursalService;

    private SucursalDto sucursalDto;
    private Sucursal sucursal;
    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = new Faker();

        sucursalDto = SucursalDto.builder()
                .nombre(faker.company().name())
                .direccion(faker.address().fullAddress())
                .ciudad(faker.address().city())
                .telefono(faker.phoneNumber().cellPhone())
                .horarioApertura(LocalTime.of(9, 0))
                .horarioCierre(LocalTime.of(18, 0))
                .activa(true)
                .build();

        sucursal = Sucursal.builder()
                .id(faker.number().randomNumber())
                .nombre(sucursalDto.getNombre())
                .direccion(sucursalDto.getDireccion())
                .ciudad(sucursalDto.getCiudad())
                .telefono(sucursalDto.getTelefono())
                .horarioApertura(sucursalDto.getHorarioApertura())
                .horarioCierre(sucursalDto.getHorarioCierre())
                .activa(sucursalDto.getActiva())
                .build();
    }

    @Test
    void crearSucursal_ShouldReturnSuccess_WhenValidInput() {
        when(sucursalRepository.save(any(Sucursal.class))).thenReturn(sucursal);

        ServiceResult<SucursalDto> result = sucursalService.crearSucursal(sucursalDto);

        assertFalse(result.hasErrors());
        assertEquals(sucursalDto.getNombre(), result.getData().getNombre());
        verify(sucursalRepository, times(1)).save(any(Sucursal.class));
    }

    @Test
    void crearSucursal_ShouldReturnError_WhenInvalidSchedule() {
        sucursalDto.setHorarioApertura(LocalTime.of(19, 0));

        ServiceResult<SucursalDto> result = sucursalService.crearSucursal(sucursalDto);

        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        verify(sucursalRepository, never()).save(any());
    }

    @Test
    void obtenerSucursalPorId_ShouldReturnSucursal_WhenExists() {
        when(sucursalRepository.findById(anyLong())).thenReturn(Optional.of(sucursal));

        ServiceResult<SucursalDto> result = sucursalService.obtenerSucursalPorId(sucursal.getId());

        assertFalse(result.hasErrors());
        assertEquals(sucursal.getId(), result.getData().getId());
    }

    @Test
    void obtenerSucursalPorId_ShouldReturnError_WhenNotExists() {
        when(sucursalRepository.findById(anyLong())).thenReturn(Optional.empty());

        ServiceResult<SucursalDto> result = sucursalService.obtenerSucursalPorId(faker.number().randomNumber());

        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
    }

    @Test
    void listarTodasLasSucursales_ShouldReturnList() {
        when(sucursalRepository.findAll()).thenReturn(List.of(sucursal));

        ServiceResult<List<SucursalDto>> result = sucursalService.listarTodasLasSucursales();

        assertFalse(result.hasErrors());
        assertEquals(1, result.getData().size());
    }

    @Test
    void actualizarSucursal_ShouldUpdate_WhenValidInput() {
        when(sucursalRepository.findById(anyLong())).thenReturn(Optional.of(sucursal));
        when(sucursalRepository.save(any(Sucursal.class))).thenReturn(sucursal);

        ServiceResult<SucursalDto> result = sucursalService.actualizarSucursal(sucursal.getId(), sucursalDto);

        assertFalse(result.hasErrors());
        verify(sucursalRepository, times(1)).save(any(Sucursal.class));
    }

    @Test
    void actualizarSucursal_ShouldReturnError_WhenInvalidSchedule() {
        sucursalDto.setHorarioApertura(LocalTime.of(19, 0));

        ServiceResult<SucursalDto> result = sucursalService.actualizarSucursal(sucursal.getId(), sucursalDto);

        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
    }

    @Test
    void cambiarEstadoSucursal_ShouldUpdateStatus() {
        when(sucursalRepository.findById(anyLong())).thenReturn(Optional.of(sucursal));

        ServiceResult<SucursalDto> result = sucursalService.cambiarEstadoSucursal(sucursal.getId(), false);

        assertFalse(result.hasErrors());
        verify(sucursalRepository, times(1)).save(any(Sucursal.class));
    }

    @Test
    void buscarSucursalesActivas_ShouldReturnActiveOnly() {
        when(sucursalRepository.findByActivaTrue()).thenReturn(List.of(sucursal));

        ServiceResult<List<SucursalDto>> result = sucursalService.buscarSucursalesActivas();

        assertFalse(result.hasErrors());
        assertEquals(1, result.getData().size());
    }

    @Test
    void convertToDto_ShouldConvertCorrectly() {
        SucursalDto dto = sucursalService.convertToDto(sucursal);

        assertEquals(sucursal.getId(), dto.getId());
        assertEquals(sucursal.getNombre(), dto.getNombre());
        assertEquals(sucursal.getHorarioApertura(), dto.getHorarioApertura());
        assertEquals(sucursal.getActiva(), dto.getActiva());
    }
}