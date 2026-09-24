package com.califorge.msnotificaciones.provider;

import com.califorge.msnotificaciones.model.Notificacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Implementacion no-op del proveedor de envio: solo registra en log y
 * devuelve exito. Sustituira por AWS SES v2 en la fase de integracion.
 */
@Component
public class LogEnvioProvider implements EnvioProvider {

    private static final Logger log = LoggerFactory.getLogger(LogEnvioProvider.class);

    @Override
    public ResultadoEnvio enviar(Notificacion notificacion) {
        log.info("[LOG-ENVIO] canal={} tipo={} destinatario={} asunto=\"{}\"",
                notificacion.getCanal(),
                notificacion.getTipo(),
                notificacion.getDestinatarioEmail() != null
                        ? notificacion.getDestinatarioEmail()
                        : notificacion.getDestinatarioSub(),
                notificacion.getAsunto());
        return ResultadoEnvio.exito("LOG", "LOG-" + UUID.randomUUID());
    }
}
