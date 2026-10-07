package com.califorge.msnotificaciones.provider;

import com.califorge.msnotificaciones.model.Notificacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Implementacion no-op del proveedor de envio: solo registra en log y
 * devuelve exito. Se activa con notificaciones.envio.proveedor=log
 * (pruebas/CI); el despacho real es EmailEnvioProvider.
 */
@Component
@ConditionalOnProperty(name = "notificaciones.envio.proveedor", havingValue = "log")
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
