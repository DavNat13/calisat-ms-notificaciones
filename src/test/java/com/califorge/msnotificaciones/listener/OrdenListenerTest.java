package com.califorge.msnotificaciones.listener;

import com.califorge.msnotificaciones.dto.OrdenMensaje;
import com.califorge.msnotificaciones.model.Destinatario;
import com.califorge.msnotificaciones.repository.DestinatarioRepository;
import com.califorge.msnotificaciones.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdenListenerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private DestinatarioRepository destinatarioRepository;

    @InjectMocks
    private OrdenListener ordenListener;

    private static final String SUB = "sub-001";

    @Test
    void ordenConfirmada_enviaCorreoDeConfirmacion() {
        when(destinatarioRepository.findByAzureSub(SUB))
                .thenReturn(Optional.of(destinatario("ana@ejemplo.com", true)));

        ordenListener.onOrdenMensaje(new OrdenMensaje("orden-1", SUB, "ORDEN_CONFIRMADA", "PAGADA", "25.50"));

        verify(emailService).enviarOrdenConfirmada("ana@ejemplo.com", "orden-1", "25.50");
    }

    @Test
    void ordenCancelada_enviaCorreoDeCancelacion() {
        when(destinatarioRepository.findByAzureSub(SUB))
                .thenReturn(Optional.of(destinatario("ana@ejemplo.com", true)));

        ordenListener.onOrdenMensaje(new OrdenMensaje("orden-2", SUB, "ORDEN_CANCELADA", "CANCELADA", "10.00"));

        verify(emailService).enviarOrdenCancelada("ana@ejemplo.com", "orden-2");
    }

    @Test
    void destinatarioDesconocido_omiteElCorreo() {
        when(destinatarioRepository.findByAzureSub(SUB)).thenReturn(Optional.empty());

        ordenListener.onOrdenMensaje(new OrdenMensaje("orden-3", SUB, "ORDEN_CONFIRMADA", "PAGADA", "1.00"));

        verify(emailService, never()).enviarOrdenConfirmada(anyString(), anyString(), anyString());
    }

    @Test
    void destinatarioInactivo_omiteElCorreo() {
        when(destinatarioRepository.findByAzureSub(SUB))
                .thenReturn(Optional.of(destinatario("ana@ejemplo.com", false)));

        ordenListener.onOrdenMensaje(new OrdenMensaje("orden-4", SUB, "ORDEN_CANCELADA", "CANCELADA", "1.00"));

        verify(emailService, never()).enviarOrdenCancelada(anyString(), anyString());
    }

    @Test
    void destinatarioSinEmail_omiteElCorreo() {
        when(destinatarioRepository.findByAzureSub(SUB))
                .thenReturn(Optional.of(destinatario(null, true)));

        ordenListener.onOrdenMensaje(new OrdenMensaje("orden-5", SUB, "ORDEN_CONFIRMADA", "PAGADA", "1.00"));

        verify(emailService, never()).enviarOrdenConfirmada(anyString(), anyString(), anyString());
    }

    @Test
    void eventoDesconocido_noEnviaCorreo() {
        when(destinatarioRepository.findByAzureSub(SUB))
                .thenReturn(Optional.of(destinatario("ana@ejemplo.com", true)));

        ordenListener.onOrdenMensaje(new OrdenMensaje("orden-6", SUB, "ORDEN_OTRA", "PENDIENTE", "1.00"));

        verify(emailService, never()).enviarOrdenConfirmada(anyString(), anyString(), anyString());
        verify(emailService, never()).enviarOrdenCancelada(anyString(), anyString());
    }

    @Test
    void mensajeSinEvento_noResuelveDestinatario() {
        ordenListener.onOrdenMensaje(new OrdenMensaje("orden-7", SUB, null, null, null));

        verify(destinatarioRepository, never()).findByAzureSub(anyString());
        verify(emailService, never()).enviarOrdenConfirmada(anyString(), anyString(), anyString());
    }

    private Destinatario destinatario(String email, boolean activo) {
        Destinatario destinatario = new Destinatario();
        destinatario.setAzureSub(SUB);
        destinatario.setEmail(email);
        destinatario.setActivo(activo);
        return destinatario;
    }
}
