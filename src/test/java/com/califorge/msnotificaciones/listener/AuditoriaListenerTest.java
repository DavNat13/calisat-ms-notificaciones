package com.califorge.msnotificaciones.listener;

import com.califorge.msnotificaciones.dto.AuditoriaMensaje;
import com.califorge.msnotificaciones.model.ResultadoAuditoria;
import com.califorge.msnotificaciones.model.TipoAuditoria;
import com.califorge.msnotificaciones.service.AuditoriaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditoriaListenerTest {

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private AuditoriaListener auditoriaListener;

    @Test
    void mensajeValido_seRegistraEnElServicio() {
        AuditoriaMensaje mensaje = AuditoriaMensaje.de(TipoAuditoria.USUARIO, "usuario.registrado",
                "id-1", null, "ana@ejemplo.com", ResultadoAuditoria.ENVIADO, null);

        auditoriaListener.onAuditoria(mensaje);

        verify(auditoriaService).registrar(mensaje);
    }

    @Test
    void mensajeSinClave_noInvocaElServicio() {
        auditoriaListener.onAuditoria(new AuditoriaMensaje(TipoAuditoria.ORDEN, "orden.confirmada",
                "orden-1", "sub-1", "ana@ejemplo.com", ResultadoAuditoria.ENVIADO, null, null, null));

        verify(auditoriaService, never()).registrar(any());
    }

    @Test
    void mensajeNulo_noInvocaElServicio() {
        auditoriaListener.onAuditoria(null);

        verify(auditoriaService, never()).registrar(any());
    }
}
