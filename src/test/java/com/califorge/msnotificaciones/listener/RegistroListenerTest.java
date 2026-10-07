package com.califorge.msnotificaciones.listener;

import com.califorge.msnotificaciones.dto.RegistroMensaje;
import com.califorge.msnotificaciones.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RegistroListenerTest {

    @Mock
    private EmailService emailService;

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
}
