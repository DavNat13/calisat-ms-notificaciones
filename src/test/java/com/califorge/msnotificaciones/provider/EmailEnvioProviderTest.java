package com.califorge.msnotificaciones.provider;

import com.califorge.msnotificaciones.model.Canal;
import com.califorge.msnotificaciones.model.Notificacion;
import com.califorge.msnotificaciones.model.TipoNotificacion;
import com.califorge.msnotificaciones.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Despacho SMTP real: exito, guardas y reintentabilidad. */
class EmailEnvioProviderTest {

    private final EmailService emailService = mock(EmailService.class);
    private final EmailEnvioProvider provider = new EmailEnvioProvider(emailService);

    private Notificacion notificacion(String email, String asunto) {
        Notificacion n = new Notificacion();
        n.setCanal(Canal.EMAIL);
        n.setTipo(TipoNotificacion.TRANSACCIONAL);
        n.setDestinatarioEmail(email);
        n.setDestinatarioSub("sub-1");
        n.setAsunto(asunto);
        n.setCuerpoTexto("Cuerpo del correo");
        return n;
    }

    @Test
    void despachaPorSmtpYDevuelveExito() {
        Notificacion n = notificacion("ana@ejemplo.com", "Bienvenido a Calisat");

        ResultadoEnvio resultado = provider.enviar(n);

        assertTrue(resultado.exito());
        assertEquals("SMTP", resultado.proveedor());
        verify(emailService).enviar("ana@ejemplo.com", "Bienvenido a Calisat", "Cuerpo del correo");
    }

    @Test
    void sinDestinatarioEmailFallaSinReintento() {
        ResultadoEnvio resultado = provider.enviar(notificacion(null, "Asunto"));

        assertFalse(resultado.exito());
        assertFalse(resultado.reintentable());
    }

    @Test
    void sinAsuntoFallaSinReintento() {
        ResultadoEnvio resultado = provider.enviar(notificacion("ana@ejemplo.com", " "));

        assertFalse(resultado.exito());
        assertFalse(resultado.reintentable());
    }

    @Test
    void errorSmtpSeReintenta() {
        Notificacion n = notificacion("ana@ejemplo.com", "Asunto");
        doThrow(new MailSendException("caido"))
                .when(emailService).enviar("ana@ejemplo.com", "Asunto", "Cuerpo del correo");

        ResultadoEnvio resultado = provider.enviar(n);

        assertFalse(resultado.exito());
        assertTrue(resultado.reintentable());
    }
}
