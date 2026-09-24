package com.califorge.msnotificaciones.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Plantilla de mensaje con render simple {{var}} (sin Thymeleaf).
 * El campo variables guarda la lista/claves de variables en formato JSON
 * (almacenado como text; la conversion a JSONB es una mejora posterior).
 */
@Entity
@Table(name = "plantilla")
public class Plantilla {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "codigo es obligatorio")
    @Size(max = 100, message = "codigo no puede superar 100 caracteres")
    @Column(name = "codigo", nullable = false, unique = true, length = 100)
    private String codigo;

    @Column(name = "asunto", length = 500)
    private String asunto;

    @Column(name = "cuerpo_texto", columnDefinition = "text")
    private String cuerpoTexto;

    @Column(name = "cuerpo_html", columnDefinition = "text")
    private String cuerpoHtml;

    @Column(name = "variables", columnDefinition = "text")
    private String variables;

    @Column(name = "activa", nullable = false)
    private Boolean activa = true;

    @Column(name = "version", nullable = false)
    private Integer version = 1;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = fechaCreacion;
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public Plantilla() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }

    public String getCuerpoTexto() { return cuerpoTexto; }
    public void setCuerpoTexto(String cuerpoTexto) { this.cuerpoTexto = cuerpoTexto; }

    public String getCuerpoHtml() { return cuerpoHtml; }
    public void setCuerpoHtml(String cuerpoHtml) { this.cuerpoHtml = cuerpoHtml; }

    public String getVariables() { return variables; }
    public void setVariables(String variables) { this.variables = variables; }

    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
