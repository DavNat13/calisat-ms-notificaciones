package com.califorge.msnotificaciones.service;

import com.califorge.msnotificaciones.dto.NotificacionEnviarRequest;
import com.califorge.msnotificaciones.dto.NotificacionEventoRequest;
import com.califorge.msnotificaciones.exception.NotificacionNoEncontradaException;
import com.califorge.msnotificaciones.exception.TransicionEstadoNoPermitidaException;
import com.califorge.msnotificaciones.model.Canal;
import com.califorge.msnotificaciones.model.EstadoNotificacion;
import com.califorge.msnotificaciones.model.IntentoEnvio;
import com.califorge.msnotificaciones.model.Notificacion;
import com.califorge.msnotificaciones.model.TipoNotificacion;
import com.califorge.msnotificaciones.provider.EnvioProvider;
import com.califorge.msnotificaciones.provider.ResultadoEnvio;
import com.califorge.msnotificaciones.repository.DestinatarioRepository;
import com.califorge.msnotificaciones.repository.IntentoEnvioRepository;
import com.califorge.msnotificaciones.repository.NotificacionRepository;
import com.califorge.msnotificaciones.repository.PlantillaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    private static final String SUB = "sub-azure-1";

    @Mock
    private NotificacionRepository notificacionRepository;

    @Mock
    private PlantillaRepository plantillaRepository;

    @Mock
    private DestinatarioRepository destinatarioRepository;

    @Mock
    private IntentoEnvioRepository intentoEnvioRepository;

    @Mock
    private EnvioProvider envioProvider;

    @InjectMocks
    private NotificacionService notificacionService;

    @Test
    void ingestar_creaNotificacionPendienteConIdempotencyKey() {
        when(notificacionRepository.findByIdempotencyKey("clave-1")).thenReturn(Optional.empty());
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(invocation -> {
            Notificacion guardada = invocation.getArgument(0);
            guardada.setId(UUID.randomUUID());
            return guardada;
        });

        NotificacionEventoRequest request = new NotificacionEventoRequest(
                Canal.EMAIL, TipoNotificacion.SISTEMA, "Asunto", "Cuerpo", null,
                SUB, "ana@ejemplo.com", "Ana", null, "{\"sku\":\"A-1\"}",
                "calisat-ms-usuarios", "corr-1");

        Notificacion notificacion = notificacionService.ingestar(request, "clave-1");

        assertEquals(EstadoNotificacion.PENDIENTE, notificacion.getEstado());
        assertEquals("clave-1", notificacion.getIdempotencyKey());
        assertEquals(0, notificacion.getIntentos());
        assertEquals(5, notificacion.getMaxIntentos());
        assertEquals(Canal.EMAIL, notificacion.getCanal());
        assertEquals(TipoNotificacion.SISTEMA, notificacion.getTipo());
        assertEquals("calisat-ms-usuarios", notificacion.getOrigenMs());
        assertNotNull(notificacion.getProximoIntentoAt());
        verify(notificacionRepository).save(any(Notificacion.class));
    }

    @Test
    void ingestar_devuelveLaExistenteSiLaClaveSeRepite() {
        Notificacion existente = notificacion(EstadoNotificacion.PENDIENTE);
        existente.setIdempotencyKey("clave-x");
        when(notificacionRepository.findByIdempotencyKey("clave-x")).thenReturn(Optional.of(existente));

        NotificacionEventoRequest request = new NotificacionEventoRequest(
                Canal.EMAIL, TipoNotificacion.SISTEMA, "Asunto", "Cuerpo", null,
                SUB, "ana@ejemplo.com", "Ana", null, null, "calisat-ms-usuarios", "corr-2");

        Notificacion notificacion = notificacionService.ingestar(request, "clave-x");

        assertEquals(existente.getId(), notificacion.getId());
        verify(notificacionRepository, never()).save(any(Notificacion.class));
    }

    @Test
    void enviar_asignaElSubDelUsuarioAutenticadoYQuedaPendiente() {
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(invocation -> {
            Notificacion guardada = invocation.getArgument(0);
            guardada.setId(UUID.randomUUID());
            return guardada;
        });

        NotificacionEnviarRequest request = new NotificacionEnviarRequest(
                Canal.EMAIL, "Tu pedido esta en camino", "Hola, tu pedido fue despachado.",
                null, "yo@ejemplo.com", "Yo");

        Notificacion notificacion = notificacionService.enviar(SUB, request);

        assertEquals(SUB, notificacion.getDestinatarioSub());
        assertEquals(EstadoNotificacion.PENDIENTE, notificacion.getEstado());
        assertEquals(TipoNotificacion.TRANSACCIONAL, notificacion.getTipo());
        assertNotNull(notificacion.getIdempotencyKey());
        assertEquals("api-directa", notificacion.getOrigenMs());
    }

    @Test
    void obtenerPorId_lanza404CuandoNoExiste() {
        UUID id = UUID.randomUUID();
        when(notificacionRepository.findById(id)).thenReturn(Optional.empty());

        NotificacionNoEncontradaException ex = assertThrows(NotificacionNoEncontradaException.class,
                () -> notificacionService.obtenerPorId(id));
        assertTrue(ex.getMessage().contains(String.valueOf(id)));
    }

    @Test
    void reintentar_persisteUnaNotificacionFallida() {
        UUID id = UUID.randomUUID();
        Notificacion notificacion = notificacion(EstadoNotificacion.FALLIDO);
        when(notificacionRepository.findById(id)).thenReturn(Optional.of(notificacion));
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notificacion resultado = notificacionService.reintentar(id);

        assertEquals(EstadoNotificacion.PENDIENTE, resultado.getEstado());
        assertNotNull(resultado.getProximoIntentoAt());
        assertEquals(0, resultado.getIntentos());
    }

    @Test
    void reintentar_lanzaTransicionNoPermitidaDesdeEnviado() {
        UUID id = UUID.randomUUID();
        Notificacion notificacion = notificacion(EstadoNotificacion.ENVIADO);
        when(notificacionRepository.findById(id)).thenReturn(Optional.of(notificacion));

        assertThrows(TransicionEstadoNoPermitidaException.class,
                () -> notificacionService.reintentar(id));
        verify(notificacionRepository, never()).save(any(Notificacion.class));
    }

    @Test
    void procesarPendientes_marcaEnviadoCuandoElProveedorTieneExito() {
        Notificacion notificacion = notificacion(EstadoNotificacion.PENDIENTE);
        when(notificacionRepository.findByEstadoIn(any())).thenReturn(List.of(notificacion));
        when(envioProvider.enviar(any(Notificacion.class)))
                .thenReturn(ResultadoEnvio.exito("LOG", "LOG-1"));
        when(intentoEnvioRepository.save(any(IntentoEnvio.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        int procesadas = notificacionService.procesarPendientes();

        assertEquals(1, procesadas);
        assertEquals(EstadoNotificacion.ENVIADO, notificacion.getEstado());
        assertEquals("LOG-1", notificacion.getSesMessageId());
        assertEquals(1, notificacion.getIntentos());

        ArgumentCaptor<IntentoEnvio> captor = ArgumentCaptor.forClass(IntentoEnvio.class);
        verify(intentoEnvioRepository).save(captor.capture());
        assertEquals(1, captor.getValue().getNumeroIntento());
        assertTrue(captor.getValue().getExito());
        assertEquals("LOG", captor.getValue().getProveedor());
    }

    @Test
    void procesarPendientes_programaReintentoCuandoElProveedorFalla() {
        Notificacion notificacion = notificacion(EstadoNotificacion.PENDIENTE);
        when(notificacionRepository.findByEstadoIn(any())).thenReturn(List.of(notificacion));
        when(envioProvider.enviar(any(Notificacion.class)))
                .thenReturn(ResultadoEnvio.fallo("SES", 500, "error temporal", true));
        when(intentoEnvioRepository.save(any(IntentoEnvio.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime antes = LocalDateTime.now();
        int procesadas = notificacionService.procesarPendientes();

        assertEquals(1, procesadas);
        assertEquals(EstadoNotificacion.REINTENTO, notificacion.getEstado());
        assertEquals(1, notificacion.getIntentos());
        assertNull(notificacion.getSesMessageId());
        assertNotNull(notificacion.getProximoIntentoAt());
        assertTrue(notificacion.getProximoIntentoAt().isAfter(antes));

        ArgumentCaptor<IntentoEnvio> captor = ArgumentCaptor.forClass(IntentoEnvio.class);
        verify(intentoEnvioRepository).save(captor.capture());
        assertEquals(Boolean.FALSE, captor.getValue().getExito());
        assertEquals(500, captor.getValue().getHttpStatus());
        assertEquals("error temporal", captor.getValue().getError());
    }

    private Notificacion notificacion(EstadoNotificacion estado) {
        Notificacion notificacion = new Notificacion();
        notificacion.setId(UUID.randomUUID());
        notificacion.setDestinatarioSub(SUB);
        notificacion.setDestinatarioEmail("ana@ejemplo.com");
        notificacion.setDestinatarioNombre("Ana");
        notificacion.setCanal(Canal.EMAIL);
        notificacion.setTipo(TipoNotificacion.TRANSACCIONAL);
        notificacion.setAsunto("Asunto");
        notificacion.setCuerpoTexto("Cuerpo");
        notificacion.setEstado(estado);
        notificacion.setIntentos(0);
        notificacion.setMaxIntentos(5);
        notificacion.setProximoIntentoAt(LocalDateTime.now().minusMinutes(1));
        notificacion.setIdempotencyKey(UUID.randomUUID().toString());
        notificacion.setOrigenMs("test");
        return notificacion;
    }
}
