package com.califorge.msnotificaciones.client;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Vista minima del carrito en calisat-ms-carrito
 * (GET /api/v1/carrito?usuarioSub=&estado=); solo los campos que consume
 * el cron de carritos abandonados.
 */
public record CarritoDto(
        UUID id,
        String usuarioSub,
        String estado,
        List<ItemDto> items,
        LocalDateTime fechaActualizacion) {

    public record ItemDto(UUID id, String sku, Integer cantidad, BigDecimal precioUnitarioVisto) {}
}
