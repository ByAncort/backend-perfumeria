package org.necronet.mslogistica.service;

import lombok.RequiredArgsConstructor;
import org.app.dto.ServiceResult;
import org.necronet.mslogistica.dto.EnvioDto;
import org.necronet.mslogistica.dto.PagoDto;
import org.necronet.mslogistica.dto.RutaOptimizadaDto;
import org.necronet.mslogistica.dto.SeguimientoResponse;
import org.necronet.mslogistica.model.Envio;
import org.necronet.mslogistica.model.RutaOptimizada;
import org.necronet.mslogistica.repository.EnvioRepository;
import org.necronet.mslogistica.shared.MicroserviceClient;
import org.necronet.mslogistica.shared.TokenContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LogisticaService {

    private final MicroserviceClient microserviceClient;
    private final EnvioRepository envioRepository;

    @Value("${auth.url.carritoMicro}")
    private String CARRITO_SERVICE_URL;

    @Value("${logistica.codigo.prefijo}")
    private String CODIGO_SEGUIMIENTO_PREFIJO;

    public ServiceResult<PagoDto> llamarPago(Long id) {
        List<String> errors = new ArrayList<>();
        try {
            String token = TokenContext.getToken();
            String url = CARRITO_SERVICE_URL + "/api/pagos/" + id;

            ResponseEntity<PagoDto> response = microserviceClient.enviarConToken(
                    url,
                    HttpMethod.GET,
                    null,
                    PagoDto.class,
                    token
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                errors.add("Error al obtener el pago. Código de estado: " + response.getStatusCode());
                return new ServiceResult<>(errors);
            }

            return new ServiceResult<>(response.getBody());

        } catch (Exception e) {
            errors.add("Excepción al llamar al microservicio de pagos: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
    }

    @Transactional
    public ServiceResult<EnvioDto> crearEnvio(Long pagoId, String direccionEnvio, String ciudad,
                                              String provincia, String codigoPostal, String pais,
                                              String metodoEnvio) {
        List<String> errors = new ArrayList<>();

        try {
            // Verificar si ya existe un envío para este pago
            if (envioRepository.findByPagoId(pagoId).isPresent()) {
                errors.add("Ya existe un envío registrado para este pago");
                return new ServiceResult<>(errors);
            }

            // Obtener información del pago
            ServiceResult<PagoDto> pagoResult = llamarPago(pagoId);
            if (pagoResult.hasErrors() || pagoResult.getData() == null) {
                errors.addAll(pagoResult.getErrors());
                return new ServiceResult<>(errors);
            }

            PagoDto pago = pagoResult.getData();

            if (!"COMPLETADO".equalsIgnoreCase(pago.getEstado())) {
                errors.add("El pago no está aprobado, no se puede crear el envío");
                return new ServiceResult<>(errors);
            }

            String codigoSeguimiento = generarCodigoSeguimiento();

            // Crear el envío
            Envio envio = Envio.builder()
                    .pagoId(pagoId)
                    .usuarioId(pago.getUsuarioId())
                    .carritoId(pago.getCarritoId())
                    .codigoSeguimiento(codigoSeguimiento)
                    .estado("PREPARANDO")
                    .direccionEnvio(direccionEnvio)
                    .ciudad(ciudad)
                    .provincia(provincia)
                    .codigoPostal(codigoPostal)
                    .pais(pais)
                    .fechaCreacion(LocalDateTime.now())
                    .fechaActualizacion(LocalDateTime.now())
                    .metodoEnvio(metodoEnvio)
                    .build();

            envio = envioRepository.save(envio);

            // Optimizar ruta (simulación)
            optimizarRuta(envio);

            return new ServiceResult<>(convertToDto(envio));

        } catch (Exception e) {
            errors.add("Error al crear el envío: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
    }

    public ServiceResult<SeguimientoResponse> obtenerEstadoEnvio(String codigoSeguimiento) {
        List<String> errors = new ArrayList<>();

        try {
            Optional<Envio> envioOpt = envioRepository.findByCodigoSeguimiento(codigoSeguimiento);
            if (envioOpt.isEmpty()) {
                errors.add("No se encontró ningún envío con el código proporcionado");
                return new ServiceResult<>(errors);
            }

            Envio envio = envioOpt.get();

            SeguimientoResponse response = new SeguimientoResponse();
            response.setCodigoSeguimiento(envio.getCodigoSeguimiento());
            response.setEstado(envio.getEstado());
            response.setUltimaActualizacion(envio.getFechaActualizacion());

            // Simular ubicación basada en el estado
            switch (envio.getEstado().toUpperCase()) {
                case "PREPARANDO":
                    response.setUbicacionActual("Almacén central");
                    response.setFechaEstimadaEntrega(LocalDateTime.now().plusDays(3));
                    response.setMensaje("Su paquete está siendo preparado para el envío");
                    break;
                case "EN_TRANSITO":
                    response.setUbicacionActual("En camino a " + envio.getCiudad());
                    response.setFechaEstimadaEntrega(LocalDateTime.now().plusDays(1));
                    response.setMensaje("Su paquete está en camino");
                    break;
                case "ENTREGADO":
                    response.setUbicacionActual("Entregado en " + envio.getDireccionEnvio());
                    response.setFechaEstimadaEntrega(envio.getFechaEntrega());
                    response.setMensaje("Paquete entregado con éxito");
                    break;
                default:
                    response.setUbicacionActual("Almacén central");
                    response.setFechaEstimadaEntrega(LocalDateTime.now().plusDays(3));
                    response.setMensaje("Estado del envío: " + envio.getEstado());
            }

            return new ServiceResult<>(response);

        } catch (Exception e) {
            errors.add("Error al obtener el estado del envío: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
    }

    @Transactional
    public ServiceResult<EnvioDto> actualizarEstadoEnvio(String codigoSeguimiento, String nuevoEstado) {
        List<String> errors = new ArrayList<>();

        try {
            Optional<Envio> envioOpt = envioRepository.findByCodigoSeguimiento(codigoSeguimiento);
            if (envioOpt.isEmpty()) {
                errors.add("No se encontró ningún envío con el código proporcionado");
                return new ServiceResult<>(errors);
            }

            Envio envio = envioOpt.get();
            envio.setEstado(nuevoEstado);
            envio.setFechaActualizacion(LocalDateTime.now());

            if ("ENTREGADO".equalsIgnoreCase(nuevoEstado)) {
                envio.setFechaEntrega(LocalDateTime.now());
            }

            envio = envioRepository.save(envio);

            return new ServiceResult<>(convertToDto(envio));

        } catch (Exception e) {
            errors.add("Error al actualizar el estado del envío: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
    }

    public ServiceResult<List<EnvioDto>> obtenerEnviosPorUsuario(Long usuarioId) {
        List<String> errors = new ArrayList<>();

        try {
            List<Envio> envios = envioRepository.findByUsuarioId(usuarioId);
            List<EnvioDto> dtos = envios.stream()
                    .map(this::convertToDto)
                    .toList();

            return new ServiceResult<>(dtos);

        } catch (Exception e) {
            errors.add("Error al obtener los envíos del usuario: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
    }

    public ServiceResult<RutaOptimizadaDto> optimizarRutaEnvio(Long envioId) {
        List<String> errors = new ArrayList<>();

        try {
            Optional<Envio> envioOpt = envioRepository.findById(envioId);
            if (envioOpt.isEmpty()) {
                errors.add("No se encontró el envío especificado");
                return new ServiceResult<>(errors);
            }

            Envio envio = envioOpt.get();
            RutaOptimizada ruta = optimizarRuta(envio);

            RutaOptimizadaDto dto = new RutaOptimizadaDto();
            dto.setOrigen("Almacén Central");
            dto.setDestino(envio.getCiudad() + ", " + envio.getProvincia());
            dto.setRutaRecomendada(ruta.getRutaRecomendada());
            dto.setDistancia(ruta.getDistancia());
            dto.setTiempoEstimado(ruta.getTiempoEstimado());
            dto.setTransportista(ruta.getTransportista());

            return new ServiceResult<>(dto);

        } catch (Exception e) {
            errors.add("Error al optimizar la ruta: " + e.getMessage());
            return new ServiceResult<>(errors);
        }
    }

    public String generarCodigoSeguimiento() {
        return CODIGO_SEGUIMIENTO_PREFIJO +
                UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private EnvioDto convertToDto(Envio envio) {
        return EnvioDto.builder()
                .id(envio.getId())
                .pagoId(envio.getPagoId())
                .usuarioId(envio.getUsuarioId())
                .carritoId(envio.getCarritoId())
                .codigoSeguimiento(envio.getCodigoSeguimiento())
                .estado(envio.getEstado())
                .direccionEnvio(envio.getDireccionEnvio())
                .ciudad(envio.getCiudad())
                .provincia(envio.getProvincia())
                .codigoPostal(envio.getCodigoPostal())
                .pais(envio.getPais())
                .fechaCreacion(envio.getFechaCreacion())
                .fechaActualizacion(envio.getFechaActualizacion())
                .fechaEntrega(envio.getFechaEntrega())
                .metodoEnvio(envio.getMetodoEnvio())
                .notas(envio.getNotas())
                .build();
    }

    private RutaOptimizada optimizarRuta(Envio envio) {
        // Simulación de optimización de ruta
        // En una implementación real, esto podría integrarse con un servicio como Google Maps
        // o un sistema de gestión de transporte

        RutaOptimizada ruta = new RutaOptimizada();
        ruta.setOrigen("Almacén Central");
        ruta.setDestino(envio.getCiudad() + ", " + envio.getProvincia());

        // Simular diferentes rutas basadas en la ciudad de destino
        if (envio.getCiudad().equalsIgnoreCase("Buenos Aires")) {
            ruta.setRutaRecomendada("Ruta 9 -> Autopista Panamericana");
            ruta.setDistancia(50.5);
            ruta.setTiempoEstimado(1.5);
            ruta.setTransportista("Transporte Rápido SA");
        } else if (envio.getCiudad().equalsIgnoreCase("Córdoba")) {
            ruta.setRutaRecomendada("Ruta 8 -> Autopista Córdoba-Rosario");
            ruta.setDistancia(700.0);
            ruta.setTiempoEstimado(8.0);
            ruta.setTransportista("Envíos Nacionales SL");
        } else {
            ruta.setRutaRecomendada("Ruta principal más cercana");
            ruta.setDistancia(300.0);
            ruta.setTiempoEstimado(5.0);
            ruta.setTransportista("Correo Central");
        }

        return ruta;
    }
}