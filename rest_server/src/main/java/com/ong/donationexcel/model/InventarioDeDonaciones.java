package com.ong.donationexcel.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventario_de_donaciones")
public class InventarioDeDonaciones {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "La categoría es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoriaEnum categoria;

    @NotBlank(message = "La descripción es obligatoria")
    @Column(nullable = false, length = 255)
    private String descripcion;

    @PositiveOrZero(message = "La cantidad debe ser mayor o igual a cero")
    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private Boolean eliminado = false;

    @Column(name = "fecha_alta", nullable = false, updatable = false)
    private LocalDateTime fechaAlta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_alta")
    private Usuario usuarioAlta;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_modificacion")
    private Usuario usuarioModificacion;

    // Constructor vacío
    public InventarioDeDonaciones() {
    }

    // Constructor con parámetros principales
    public InventarioDeDonaciones(CategoriaEnum categoria, String descripcion, Integer cantidad) {
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.fechaAlta = LocalDateTime.now();
        this.eliminado = false;
    }

    @PrePersist
    protected void onCreate() {
        if (fechaAlta == null) {
            fechaAlta = LocalDateTime.now();
        }
        if (eliminado == null) {
            eliminado = false;
        }
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public CategoriaEnum getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaEnum categoria) {
        this.categoria = categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Boolean getEliminado() {
        return eliminado;
    }

    public void setEliminado(Boolean eliminado) {
        this.eliminado = eliminado;
    }

    public LocalDateTime getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(LocalDateTime fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public Usuario getUsuarioAlta() {
        return usuarioAlta;
    }

    public void setUsuarioAlta(Usuario usuarioAlta) {
        this.usuarioAlta = usuarioAlta;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public Usuario getUsuarioModificacion() {
        return usuarioModificacion;
    }

    public void setUsuarioModificacion(Usuario usuarioModificacion) {
        this.usuarioModificacion = usuarioModificacion;
    }
}