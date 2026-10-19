package com.califorge.msnotificaciones.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento de auditoria persistido por el consumidor de auditoria.queue:
 * registra cada usuario/orden/envio procesado y el resultado del correo
 * automatizado asociado.
 */
@Entity
@Table(name = "auditoria_evento")
public class AuditoriaEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Size(max = 200, message = "clave no puede superar 200 caracteres")
    @Column(name = "clave", nullable = false, unique = true, length = 200)
    private String clave;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoAuditoria tipo;

    @Size(max = 64, message = "evento no puede superar 64 caracteres")
    @Column(name = "evento", nullable = false, length = 64)
    private String evento;

    @Size(max = 100, message = "referenciaId no puede superar 100 caracteres")
    @Column(name = "referencia_id", length = 100)
    private String referenciaId;

    @Size(max = 255, message = "usuarioSub no puede superar 255 caracteres")
    @Column(name = "usuario_sub", length = 255)
    private String usuarioSub;

    @Size(max = 320, message = "destinatarioEmail no puede superar 320 caracteres")
    @Column(name = "destinatario_email", length = 320)
    private String destinatarioEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado_correo", nullable = false, length = 30)
    private ResultadoAuditoria resultadoCorreo;

    @Size(max = 500, message = "detalle no puede superar 500 caracteres")
    @Column(name = "detalle", length = 500)
    private String detalle;

    @Column(name = "fecha_evento", nullable = false)
    private Instant fechaEvento;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
    }

    public AuditoriaEvento() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }

    public TipoAuditoria getTipo() { return tipo; }
    public void setTipo(TipoAuditoria tipo) { this.tipo = tipo; }

    public String getEvento() { return evento; }
    public void setEvento(String evento) { this.evento = evento; }

    public String getReferenciaId() { return referenciaId; }
    public void setReferenciaId(String referenciaId) { this.referenciaId = referenciaId; }

    public String getUsuarioSub() { return usuarioSub; }
    public void setUsuarioSub(String usuarioSub) { this.usuarioSub = usuarioSub; }

    public String getDestinatarioEmail() { return destinatarioEmail; }
    public void setDestinatarioEmail(String destinatarioEmail) { this.destinatarioEmail = destinatarioEmail; }

    public ResultadoAuditoria getResultadoCorreo() { return resultadoCorreo; }
    public void setResultadoCorreo(ResultadoAuditoria resultadoCorreo) { this.resultadoCorreo = resultadoCorreo; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }

    public Instant getFechaEvento() { return fechaEvento; }
    public void setFechaEvento(Instant fechaEvento) { this.fechaEvento = fechaEvento; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
