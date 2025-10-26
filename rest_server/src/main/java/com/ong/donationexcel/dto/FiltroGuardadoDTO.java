package com.ong.donationexcel.dto;

import com.ong.donationexcel.model.FiltroGuardado;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "DTO para transferencia de datos de filtros guardados")
public class FiltroGuardadoDTO {

    @Schema(description = "ID único del filtro guardado", example = "1")
    private Integer id;

    @NotBlank(message = "El nombre del filtro es obligatorio")
    @Schema(description = "Nombre del filtro guardado", example = "Mis eventos de diciembre", required = true)
    private String nombre;

    @NotNull(message = "El tipo de filtro es obligatorio")
    @Schema(description = "Tipo de filtro", example = "EVENTO_PARTICIPACION", required = true)
    private FiltroGuardado.TipoFiltro tipoFiltro;

    @Schema(description = "Configuración de filtros en formato JSON", example = "{\"fechaInicio\":\"2023-12-01\",\"fechaFin\":\"2023-12-31\"}")
    private String filtrosJson;

    @Schema(description = "ID del usuario propietario del filtro", example = "1")
    private Integer usuarioId;

    @Schema(description = "Nombre del usuario propietario", example = "Juan Pérez")
    private String usuarioNombre;

    @Schema(description = "Fecha de creación del filtro", example = "2023-10-23T10:30:00")
    private LocalDateTime fechaCreacion;

    @Schema(description = "Fecha de última modificación", example = "2023-10-23T15:30:00")
    private LocalDateTime fechaModificacion;

    @Schema(description = "Indica si el filtro está eliminado", example = "false")
    private Boolean eliminado;

    @Schema(description = "Indica si el filtro está activo", example = "true")
    private Boolean activo;

    // Constructor vacío
    public FiltroGuardadoDTO() {
    }

    // Constructor con parámetros
    public FiltroGuardadoDTO(String nombre, FiltroGuardado.TipoFiltro tipoFiltro, String filtrosJson, Integer usuarioId) {
        this.nombre = nombre;
        this.tipoFiltro = tipoFiltro;
        this.filtrosJson = filtrosJson;
        this.usuarioId = usuarioId;
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

    public FiltroGuardado.TipoFiltro getTipoFiltro() {
        return tipoFiltro;
    }

    public void setTipoFiltro(FiltroGuardado.TipoFiltro tipoFiltro) {
        this.tipoFiltro = tipoFiltro;
    }

    public String getFiltrosJson() {
        return filtrosJson;
    }

    public void setFiltrosJson(String filtrosJson) {
        this.filtrosJson = filtrosJson;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
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
