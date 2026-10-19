package com.califorge.msnotificaciones.listener;

import com.califorge.msnotificaciones.dto.AuditoriaMensaje;
import com.califorge.msnotificaciones.dto.RegistroMensaje;
import com.califorge.msnotificaciones.model.ResultadoAuditoria;
import com.califorge.msnotificaciones.model.TipoAuditoria;
import com.califorge.msnotificaciones.service.AuditoriaProductor;
import com.califorge.msnotificaciones.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RegistroListenerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private AuditoriaProductor auditoriaProductor;

    @InjectMocks
    private RegistroListener registroListener;

    @Test
    void registroValido_enviaCorreoDeBienvenida() {
        registroListener.onUsuarioRegistrado(
                new RegistroMensaje("7c9e6679-7425-40de-944b-e07fc1f90ae7", "Ana Perez", "ana@ejemplo.com"));

        verify(emailService).enviarBienvenida("ana@ejemplo.com", "Ana Perez");
    }

    @Test
    void nombreNulo_noImpideElCorreo() {
        registroListener.onUsuarioRegistrado(
                new RegistroMensaje("7c9e6679-7425-40de-944b-e07fc1f90ae7", null, "ana@ejemplo.com"));

        verify(emailService).enviarBienvenida("ana@ejemplo.com", null);
    }

    @Test
    void emailRepetido_seEnviaUnaSolaVezPorMensaje() {
        registroListener.onUsuarioRegistrado(
                new RegistroMensaje("id-1", "Ana", "ana@ejemplo.com"));
        registroListener.onUsuarioRegistrado(
                new RegistroMensaje("id-2", "Ana", "ana@ejemplo.com"));

        verify(emailService, times(2)).enviarBienvenida("ana@ejemplo.com", "Ana");
    }

    @Test
    void correoExitoso_publicaAuditoriaEnviado() {
        registroListener.onUsuarioRegistrado(
                new RegistroMensaje("id-9", "Ana", "ana@ejemplo.com"));

        ArgumentCaptor<AuditoriaMensaje> captor = ArgumentCaptor.forClass(AuditoriaMensaje.class);
        verify(auditoriaProductor).publicarUsuario(captor.capture());

        AuditoriaMensaje mensaje = captor.getValue();
        assertEquals(TipoAuditoria.USUARIO, mensaje.tipo());
        assertEquals("usuario.registrado", mensaje.evento());
        assertEquals("id-9", mensaje.referenciaId());
        assertEquals("ana@ejemplo.com", mensaje.destinatarioEmail());
        assertEquals(ResultadoAuditoria.ENVIADO, mensaje.resultadoCorreo());
        assertTrue(mensaje.clave().startsWith("USUARIO:usuario.registrado:id-9:"));
    }

    @Test
    void falloSmtp_publicaAuditoriaFallidoSinPropagarExcepcion() {
        doThrow(new RuntimeException("SMTP caido")).when(emailService)
                .enviarBienvenida("ana@ejemplo.com", "Ana");

        registroListener.onUsuarioRegistrado(
                new RegistroMensaje("id-10", "Ana", "ana@ejemplo.com"));

        ArgumentCaptor<AuditoriaMensaje> captor = ArgumentCaptor.forClass(AuditoriaMensaje.class);
        verify(auditoriaProductor).publicarUsuario(captor.capture());

        assertEquals(ResultadoAuditoria.FALLIDO, captor.getValue().resultadoCorreo());
        assertEquals("SMTP caido", captor.getValue().detalle());
    }
}
