package com.califorge.msnotificaciones.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarritoClientTest {

    @Mock
    private RestTemplate restTemplate;

    private CarritoClient cliente() {
        return new CarritoClient(restTemplate, "http://localhost:8084");
    }

    @Test
    void buscarPorUsuario_devuelveElCarritoCuandoResponde() {
        UUID id = UUID.randomUUID();
        CarritoDto carrito = new CarritoDto(
                id, "sub-1", "ABIERTO", List.of(new CarritoDto.ItemDto(UUID.randomUUID(), "SKU-1", 2, null)),
                LocalDateTime.now());
        when(restTemplate.getForObject(
                "http://localhost:8084/api/v1/carrito?usuarioSub={usuarioSub}&estado={estado}",
                CarritoDto.class, "sub-1", "ABIERTO"))
                .thenReturn(carrito);

        Optional<CarritoDto> resultado = cliente().buscarPorUsuario("sub-1", "ABIERTO");

        assertTrue(resultado.isPresent());
        assertEquals(id, resultado.get().id());
        assertEquals(1, resultado.get().items().size());
        assertEquals("SKU-1", resultado.get().items().get(0).sku());
    }

    @Test
    void buscarPorUsuario_degradaAVacioSiElCarritoEstaCaido() {
        when(restTemplate.getForObject(
                "http://localhost:8084/api/v1/carrito?usuarioSub={usuarioSub}&estado={estado}",
                CarritoDto.class, "sub-1", "ABIERTO"))
                .thenThrow(new ResourceAccessException("Connection refused"));

        assertTrue(cliente().buscarPorUsuario("sub-1", "ABIERTO").isEmpty());
    }
}
