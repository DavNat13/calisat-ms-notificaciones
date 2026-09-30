package com.califorge.msnotificaciones.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verificacion (sin broker) de colas, bindings y argumentos de dead-letter
 * declarados en RabbitConfig.
 */
class RabbitConfigTest {

    private final RabbitConfig config = new RabbitConfig();

    @Test
    void colasDeAlertasYPagosSeEnlazanAlExchangeCompartido() {
        assertEquals("inventario.alertas.queue", config.inventarioAlertasQueue().getName());
        assertEquals("calisat.exchange", config.inventarioAlertasBinding().getExchange());
        assertEquals("inventario.alertas.queue", config.inventarioAlertasBinding().getDestination());
        assertEquals("inventario.stock.critico", config.inventarioAlertasBinding().getRoutingKey());

        assertEquals("pagos.procesados.queue", config.pagosProcesadosQueue().getName());
        assertEquals("calisat.exchange", config.pagosProcesadosBinding().getExchange());
        assertEquals("pago.procesado", config.pagosProcesadosBinding().getRoutingKey());
    }

    @Test
    void dlqSeEnlazaAlExchangeDeadLetter() {
        assertTrue(config.deadLetterExchange().isDurable());
        assertEquals("calisat.dlx", config.deadLetterExchange().getName());
        assertEquals("dlq.general.queue", config.dlqGeneralQueue().getName());
        assertEquals("calisat.dlx", config.dlqGeneralBinding().getExchange());
        assertEquals("dlq.general", config.dlqGeneralBinding().getRoutingKey());
    }

    @Test
    void colasDeTrabajoVuelcanSusFallidosALaDlq() {
        List<Queue> colasDeTrabajo = List.of(
                config.notificacionesQueue(),
                config.ordenesQueue(),
                config.enviosQueue(),
                config.inventarioAlertasQueue(),
                config.pagosProcesadosQueue());

        for (Queue cola : colasDeTrabajo) {
            assertEquals("calisat.dlx", cola.getArguments().get("x-dead-letter-exchange"));
            assertEquals("dlq.general", cola.getArguments().get("x-dead-letter-routing-key"));
        }
        assertTrue(config.dlqGeneralQueue().getArguments().isEmpty());
    }

    @Test
    void bindingsDeNotificacionesOrdenesYEnviosSeMantienen() {
        assertEquals("usuario.registrado", config.registroBinding().getRoutingKey());
        assertEquals("orden.confirmada", config.ordenConfirmadaBinding().getRoutingKey());
        assertEquals("orden.cancelada", config.ordenCanceladaBinding().getRoutingKey());
        assertEquals("envio.despachado", config.envioDespachadoBinding().getRoutingKey());
        assertEquals("envio.entregado", config.envioEntregadoBinding().getRoutingKey());
    }
}
