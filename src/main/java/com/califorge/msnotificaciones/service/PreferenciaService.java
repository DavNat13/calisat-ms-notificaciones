package com.califorge.msnotificaciones.service;

import com.califorge.msnotificaciones.dto.PreferenciaItemRequest;
import com.califorge.msnotificaciones.dto.PreferenciaRequest;
import com.califorge.msnotificaciones.model.Suscripcion;
import com.califorge.msnotificaciones.repository.SuscripcionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Preferencias de suscripcion a campanas (opt-in/out) por usuario.
 * opt_in nace en FALSE (consentimiento previo); actualizar con optIn=true
 * registra el consentimiento explicito.
 */
@Service
@Transactional
public class PreferenciaService {

    private final SuscripcionRepository suscripcionRepository;

    public PreferenciaService(SuscripcionRepository suscripcionRepository) {
        this.suscripcionRepository = suscripcionRepository;
    }

    @Transactional(readOnly = true)
    public List<Suscripcion> obtener(String azureSub) {
        return suscripcionRepository.findByUsuarioSubOrderByTipoCampanaAsc(azureSub);
    }

    /**
     * Upsert de las preferencias enviadas: crea la suscripcion si no existe
     * y actualiza el opt_in en caso contrario. El token_desuscripcion se
     * genera al crear y no se modifica en actualizaciones posteriores.
     */
    public List<Suscripcion> actualizar(String azureSub, PreferenciaRequest request) {
        for (PreferenciaItemRequest item : request.preferencias()) {
            Suscripcion suscripcion = suscripcionRepository
                    .findByUsuarioSubAndTipoCampanaAndCanal(azureSub, item.tipoCampana(), item.canal())
                    .orElseGet(() -> {
                        Suscripcion nueva = new Suscripcion();
                        nueva.setUsuarioSub(azureSub);
                        nueva.setTipoCampana(item.tipoCampana());
                        nueva.setCanal(item.canal());
                        return nueva;
                    });
            suscripcion.setOptIn(item.optIn());
            suscripcionRepository.save(suscripcion);
        }
        return suscripcionRepository.findByUsuarioSubOrderByTipoCampanaAsc(azureSub);
    }
}
