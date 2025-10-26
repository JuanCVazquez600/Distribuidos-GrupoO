package com.ong.donationexcel.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "filtro_guardado")
public class FiltroGuardado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "El nombre del filtro es obligatorio")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotNull(message = "El tipo de filtro es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_filtro", nullable = false)
    private TipoFiltro tipoFiltro;

    @Column(name = "filtros_json", columnDefinition = "TEXT")
    private String filtrosJson;

    @NotNull(message = "El usuario es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @Column(nullable = false)
    private Boolean eliminado = false;

    @Column(nullable = false)
    private Boolean activo = true;

    public enum TipoFiltro {
        EVENTO_PARTICIPACION,
        DONACIONES
    }

    // Constructor vacío
    public FiltroGuardado() {
    }

    // Constructor con parámetros
    public FiltroGuardado(String nombre, TipoFiltro tipoFiltro, String filtrosJson, Usuario usuario) {
        this.nombre = nombre;
        this.tipoFiltro = tipoFiltro;
        this.filtrosJson = filtrosJson;
        this.usuario = usuario;
        this.fechaCreacion = LocalDateTime.now();
        this.eliminado = false;
    }

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (eliminado == null) {
            eliminado = false;
        }
        if (activo == null) {
            activo = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoFiltro getTipoFiltro() {
        return tipoFiltro;
    }

    public void setTipoFiltro(TipoFiltro tipoFiltro) {
        this.tipoFiltro = tipoFiltro;
    }

    public String getFiltrosJson() {
        return filtrosJson;
    }

    public void setFiltrosJson(String filtrosJson) {
        this.filtrosJson = filtrosJson;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public Boolean getEliminado() {
        return eliminado;
    }

    public void setEliminado(Boolean eliminado) {
        this.eliminado = eliminado;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
