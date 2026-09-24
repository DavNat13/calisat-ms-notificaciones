package com.califorge.msnotificaciones.listener;

import com.califorge.msnotificaciones.dto.EnvioMensaje;
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
class EnvioListenerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private DestinatarioRepository destinatarioRepository;

    @InjectMocks
    private EnvioListener envioListener;

    private static final String SUB = "sub-001";

    @Test
    void envioDespachado_enviaCorreoDeDespacho() {
        when(destinatarioRepository.findByAzureSub(SUB))
                .thenReturn(Optional.of(destinatario("ana@ejemplo.com", true)));

        envioListener.onEnvioMensaje(new EnvioMensaje(
                "envio-1", "orden-1", SUB, "CAL-ABC123DEF456", "SEUR", "ENVIO_DESPACHADO", "DESPACHADO"));

        verify(emailService).enviarEnvioDespachado("ana@ejemplo.com", "CAL-ABC123DEF456", "SEUR");
    }

    @Test
    void envioEntregado_enviaCorreoDeEntrega() {
        when(destinatarioRepository.findByAzureSub(SUB))
                .thenReturn(Optional.of(destinatario("ana@ejemplo.com", true)));

        envioListener.onEnvioMensaje(new EnvioMensaje(
                "envio-2", "orden-1", SUB, "CAL-ABC123DEF456", "SEUR", "ENVIO_ENTREGADO", "ENTREGADO"));

        verify(emailService).enviarEnvioEntregado("ana@ejemplo.com", "CAL-ABC123DEF456");
    }

    @Test
    void destinatarioDesconocido_omiteElCorreo() {
        when(destinatarioRepository.findByAzureSub(SUB)).thenReturn(Optional.empty());

        envioListener.onEnvioMensaje(new EnvioMensaje(
                "envio-3", "orden-1", SUB, "CAL-ABC123DEF456", null, "ENVIO_DESPACHADO", "DESPACHADO"));

        verify(emailService, never()).enviarEnvioDespachado(anyString(), anyString(), anyString());
    }

    @Test
    void destinatarioInactivo_omiteElCorreo() {
        when(destinatarioRepository.findByAzureSub(SUB))
                .thenReturn(Optional.of(destinatario("ana@ejemplo.com", false)));

        envioListener.onEnvioMensaje(new EnvioMensaje(
                "envio-4", "orden-1", SUB, "CAL-ABC123DEF456", null, "ENVIO_ENTREGADO", "ENTREGADO"));

        verify(emailService, never()).enviarEnvioEntregado(anyString(), anyString());
    }

    @Test
    void eventoDesconocido_noEnviaCorreo() {
        when(destinatarioRepository.findByAzureSub(SUB))
                .thenReturn(Optional.of(destinatario("ana@ejemplo.com", true)));

        envioListener.onEnvioMensaje(new EnvioMensaje(
                "envio-5", "orden-1", SUB, "CAL-ABC123DEF456", null, "ENVIO_OTRO", "EN_TRANSITO"));

        verify(emailService, never()).enviarEnvioDespachado(anyString(), anyString(), anyString());
        verify(emailService, never()).enviarEnvioEntregado(anyString(), anyString());
    }

    @Test
    void mensajeSinEvento_noResuelveDestinatario() {
        envioListener.onEnvioMensaje(new EnvioMensaje(
                "envio-6", "orden-1", SUB, "CAL-ABC123DEF456", null, null, null));

        verify(destinatarioRepository, never()).findByAzureSub(anyString());
        verify(emailService, never()).enviarEnvioEntregado(anyString(), anyString());
    }

    private Destinatario destinatario(String email, boolean activo) {
        Destinatario destinatario = new Destinatario();
        destinatario.setAzureSub(SUB);
        destinatario.setEmail(email);
        destinatario.setActivo(activo);
        return destinatario;
    }
}
