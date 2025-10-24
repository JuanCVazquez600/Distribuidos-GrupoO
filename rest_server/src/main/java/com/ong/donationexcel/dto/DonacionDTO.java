package com.ong.donationexcel.dto;

import com.ong.donationexcel.model.CategoriaEnum;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

@Schema(description = "DTO para transferencia de datos de inventario de donaciones")
public class DonacionDTO {

    @Schema(description = "ID único del inventario de donación", example = "1")
    private Integer id;

    @NotNull(message = "La categoría es obligatoria")
    @Schema(description = "Categoría de la donación", example = "ROPA", required = true)
    private CategoriaEnum categoria;

    @NotBlank(message = "La descripción es obligatoria")
    @Schema(description = "Descripción detallada de la donación", example = "Ropa de invierno para niños", required = true)
    private String descripcion;

    @PositiveOrZero(message = "La cantidad debe ser mayor o igual a cero")
    @Schema(description = "Cantidad de items donados", example = "5")
    private Integer cantidad;

    @Schema(description = "Indica si el registro está eliminado", example = "false")
    private Boolean eliminado;

    @Schema(description = "Fecha de alta del registro", example = "2023-10-23T10:30:00")
    private LocalDateTime fechaAlta;

    @Schema(description = "Usuario que dio de alta el registro")
    private String usuarioAlta;

    @Schema(description = "Fecha de última modificación", example = "2023-10-23T15:30:00")
    private LocalDateTime fechaModificacion;

    @Schema(description = "Usuario que modificó el registro")
    private String usuarioModificacion;

    // Constructor vacío
    public DonacionDTO() {
    }

    // Constructor con parámetros principales
    public DonacionDTO(CategoriaEnum categoria, String descripcion, Integer cantidad) {
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
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

    public String getUsuarioAlta() {
        return usuarioAlta;
    }

    public void setUsuarioAlta(String usuarioAlta) {
        this.usuarioAlta = usuarioAlta;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public String getUsuarioModificacion() {
        return usuarioModificacion;
    }

    public void setUsuarioModificacion(String usuarioModificacion) {
        this.usuarioModificacion = usuarioModificacion;
    }
}