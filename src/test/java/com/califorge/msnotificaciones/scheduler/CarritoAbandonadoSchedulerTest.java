package com.califorge.msnotificaciones.scheduler;

import com.califorge.msnotificaciones.client.CarritoClient;
import com.califorge.msnotificaciones.client.CarritoDto;
import com.califorge.msnotificaciones.dto.NotificacionEventoRequest;
import com.califorge.msnotificaciones.model.Destinatario;
import com.califorge.msnotificaciones.service.DestinatarioService;
import com.califorge.msnotificaciones.service.NotificacionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarritoAbandonadoSchedulerTest {

    @Mock
    private CarritoClient carritoClient;

    @Mock
    private DestinatarioService destinatarioService;

    @Mock
    private NotificacionService notificacionService;

    @InjectMocks
    private CarritoAbandonadoScheduler scheduler;

    @Test
    void detectar_ingestaNotificacionCuandoElCarritoEstaAbandonado() {
        UUID idCarrito = UUID.randomUUID();
        Destinatario destinatario = destinatario(true);
        CarritoDto carrito = new CarritoDto(
                idCarrito, destinatario.getAzureSub(), "ABIERTO", List.of(),
                LocalDateTime.now().minusDays(40));
        when(destinatarioService.listar()).thenReturn(List.of(destinatario));
        when(carritoClient.buscarPorUsuario("sub-1", "ABIERTO")).thenReturn(Optional.of(carrito));

        scheduler.detectarCarritosAbandonados();

        verify(notificacionService).ingestar(
                any(NotificacionEventoRequest.class),
                eq("carrito-abandonado-" + idCarrito));
    }

    @Test
    void detectar_noPublicaCuandoElCarritoEsReciente() {
        Destinatario destinatario = destinatario(true);
        CarritoDto carrito = new CarritoDto(
                UUID.randomUUID(), destinatario.getAzureSub(), "ABIERTO", List.of(),
                LocalDateTime.now().minusDays(2));
        when(destinatarioService.listar()).thenReturn(List.of(destinatario));
        when(carritoClient.buscarPorUsuario("sub-1", "ABIERTO")).thenReturn(Optional.of(carrito));

        scheduler.detectarCarritosAbandonados();

        verify(notificacionService, never()).ingestar(any(), anyString());
    }

    @Test
    void detectar_omiteDestinatariosInactivosSinConsultarElCarrito() {
        when(destinatarioService.listar()).thenReturn(List.of(destinatario(false)));

        scheduler.detectarCarritosAbandonados();

        verifyNoInteractions(carritoClient, notificacionService);
    }

    @Test
    void detectar_noLanzaExcepcionSiElServicioDeCarritoFalla() {
        Destinatario destinatario = destinatario(true);
        when(destinatarioService.listar()).thenReturn(List.of(destinatario));
        when(carritoClient.buscarPorUsuario(anyString(), anyString()))
                .thenThrow(new IllegalStateException("carrito caido"));

        assertDoesNotThrow(() -> scheduler.detectarCarritosAbandonados());
        verify(notificacionService, never()).ingestar(any(), anyString());
    }

    private Destinatario destinatario(boolean activo) {
        Destinatario destinatario = new Destinatario();
        destinatario.setAzureSub("sub-1");
        destinatario.setEmail("ana@ejemplo.com");
        destinatario.setNombre("Ana");
        destinatario.setActivo(activo);
        return destinatario;
    }
}
