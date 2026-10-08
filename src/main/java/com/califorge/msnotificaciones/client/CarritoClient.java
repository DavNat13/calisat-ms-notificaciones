package com.califorge.msnotificaciones.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Cliente HTTP de calisat-ms-carrito (fase B). Sin service discovery:
 * base URL = calisat.gateway.url (el API Gateway: cero IPs en el repo).
 *
 * <p>Consumido por el cron de carritos abandonados (listado interno del
 * carrito, ver diseno: GET /api/v1/carrito?usuarioSub=&estado=).</p>
 *
 * <p>Resiliencia: si el carrito esta caido devuelve Optional vacio y el
 * cron simplemente no detecta abandonos en esa pasada (jamas falla).</p>
 */
@Component
public class CarritoClient {

    private static final Logger log = LoggerFactory.getLogger(CarritoClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public CarritoClient(RestTemplate restTemplate,
                         @Value("${calisat.urls.carrito:${calisat.gateway.url}}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /**
     * Lee el carrito de un usuario filtrando por estado (p.ej. ABIERTO).
     *
     * @return el carrito si el servicio respondio; vacio si esta caido
     */
    public Optional<CarritoDto> buscarPorUsuario(String usuarioSub, String estado) {
        try {
            CarritoDto carrito = restTemplate.getForObject(
                    baseUrl + "/api/v1/carrito?usuarioSub={usuarioSub}&estado={estado}",
                    CarritoDto.class, usuarioSub, estado);
            return Optional.ofNullable(carrito);
        } catch (RestClientException ex) {
            log.warn("Carrito no disponible para el usuario '{}' ({}): se omite la lectura",
                    usuarioSub, ex.getMessage());
            return Optional.empty();
        }
    }
}
