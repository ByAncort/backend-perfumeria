package org.necronet.mssoportecliente;
import net.datafaker.Faker;
import org.app.dto.ServiceResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.necronet.mssoportecliente.dto.*;
import org.necronet.mssoportecliente.model.TicketSoporte;
import org.necronet.mssoportecliente.repository.TicketSoporteRepository;
import org.necronet.mssoportecliente.service.SoporteService;
import org.necronet.mssoportecliente.shared.MicroserviceClient;
import org.necronet.mssoportecliente.shared.TokenContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class SoporteServiceTest {
    @Mock
    private TicketSoporteRepository ticketRepository;
    @Mock
    private MicroserviceClient microserviceClient;

    @InjectMocks
    private SoporteService soporteService;

    private final Faker faker = new Faker();

    private ClienteDto clienteDto;
    private Long clienteId;
    private TicketRequest  ticketRequest;
    private TicketSoporte  ticketSoporte;

    @BeforeEach
    void setUp() {
        clienteId = 1L;
        clienteDto = ClienteDto.builder().id(clienteId).nombre(faker.name().firstName()).build();
        ticketRequest = TicketRequest.builder()
                .clienteId(clienteId)
                .descripcion(faker.lorem().sentence())
                .tipo("SOPORTE")
                .prioridad("ALTA")
                .categoria("TÉCNICA")
                .build();
        ticketSoporte = TicketSoporte.builder()
                .id(faker.number().randomNumber())
                .clienteId(clienteId)
                .estado("ABIERTO")
                .descripcion(faker.lorem().sentence())
                .fechaCreacion(LocalDateTime.now())
                .build();

        TokenContext.setToken("eyJhbGciOiJIUzI1NiJ9.eyJleHBpcmF0aW9uIjoxNzUxMTc3MjE4Mzg4LCJpc3N1ZWRBdCI6MTc1MTA4MDgxODM4OCwic3ViIjoiYWRtaW4iLCJpYXQiOjE3NTEwODA4MTgsImV4cCI6MTc1MTE3NzIxOH0.gbcKLmX16MshzU09Mtx_G8E36iPxU7CgiSGji5Pamjk");
    }

    @Test
    void crearTicket_deberiaCrearTicketCorrectamente() {

        when(microserviceClient.enviarConToken(anyString(), any(), any(), eq(ClienteDto.class), anyString()))
                .thenReturn(new ResponseEntity<>(clienteDto, HttpStatus.OK));
        when(ticketRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        ServiceResult<TicketSoporteDto> result = soporteService.crearTicket(ticketRequest);
        assertFalse(result.hasErrors());
        assertEquals(clienteId, result.getData().getClienteId());
    }

    @Test
    void obtenerTicketsPorCliente_deberiaRetornarListaCorrecta() {
        Long clienteId = faker.number().randomNumber();
        ClienteDto clienteDto = ClienteDto.builder().id(clienteId).nombre(faker.name().firstName()).build();

        when(microserviceClient.enviarConToken(anyString(), any(), any(), eq(ClienteDto.class), anyString()))
                .thenReturn(new ResponseEntity<>(clienteDto, HttpStatus.OK));

        when(ticketRepository.findByClienteId(clienteId)).thenReturn(List.of(ticketSoporte));

        ServiceResult<List<TicketSoporteDto>> result = soporteService.obtenerTicketsPorCliente(clienteId);

        assertFalse(result.hasErrors());
        assertEquals(1, result.getData().size());
        assertEquals("ABIERTO", result.getData().get(0).getEstado());
    }

    @Test
    void actualizarTicket_deberiaActualizarEstadoYAgregarFechaCierre() {
        Long ticketId = faker.number().randomNumber();
        Long clienteId = faker.number().randomNumber();
        ClienteDto clienteDto = ClienteDto.builder().id(clienteId).nombre(faker.name().firstName()).build();

        TicketSoporte ticket = TicketSoporte.builder()
                .id(ticketId)
                .clienteId(clienteId)
                .estado("ABIERTO")
                .build();

        TicketUpdateRequest request = TicketUpdateRequest.builder()
                .estado("RESUELTO")
                .solucion("Problema resuelto")
                .build();

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(microserviceClient.enviarConToken(anyString(), any(), any(), eq(ClienteDto.class), anyString()))
                .thenReturn(new ResponseEntity<>(clienteDto, HttpStatus.OK));

        ServiceResult<TicketSoporteDto> result = soporteService.actualizarTicket(ticketId, request);

        assertFalse(result.hasErrors());
        assertEquals("RESUELTO", result.getData().getEstado());
        assertNotNull(result.getData().getFechaCierre());
    }
    @Test
    void crearTicket_deberiaRetornarErrorSiClienteNoExiste() {
        when(microserviceClient.enviarConToken(anyString(), any(), any(), eq(ClienteDto.class), anyString()))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        ServiceResult<TicketSoporteDto> result = soporteService.crearTicket(ticketRequest);

        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().contains("Cliente no encontrado"));
    }
    @Test
    void crearTicket_deberiaRetornarErrorSiFallaAlGuardar() {
        when(microserviceClient.enviarConToken(anyString(), any(), any(), eq(ClienteDto.class), anyString()))
                .thenReturn(new ResponseEntity<>(clienteDto, HttpStatus.OK));
        when(ticketRepository.save(any())).thenThrow(new RuntimeException("Error DB"));

        ServiceResult<TicketSoporteDto> result = soporteService.crearTicket(ticketRequest);

        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().contains("Error DB"));
    }

}
