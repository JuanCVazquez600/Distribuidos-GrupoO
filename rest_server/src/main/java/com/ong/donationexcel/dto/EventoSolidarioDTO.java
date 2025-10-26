package com.ong.donationexcel.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;

@Schema(description = "DTO para transferencia de datos de eventos solidarios")
public class EventoSolidarioDTO {

    @Schema(description = "ID único del evento", example = "1")
    private Integer id;

    @NotBlank(message = "El nombre del evento es obligatorio")
    @Schema(description = "Nombre del evento solidario", example = "Recogida de alimentos", required = true)
    private String nombreEvento;

    @NotBlank(message = "La descripción del evento es obligatoria")
    @Schema(description = "Descripción detallada del evento", example = "Evento para recoger alimentos no perecederos", required = true)
    private String descripcionEvento;

    @NotNull(message = "La fecha del evento es obligatoria")
    @Schema(description = "Fecha y hora del evento", example = "2023-12-25T10:00:00", required = true)
    private LocalDateTime fechaEvento;

    @Schema(description = "Indica si el evento está eliminado", example = "false")
    private Boolean eliminado;



    @Schema(description = "ID del usuario que dio de alta el evento")
    private Integer usuarioAltaId;

    @Schema(description = "Usuario que dio de alta el evento")
    private String usuarioAlta;

    @Schema(description = "Fecha de última modificación", example = "2023-10-23T15:30:00")
    private LocalDateTime fechaModificacion;

    @Schema(description = "Usuario que modificó el evento")
    private String usuarioModificacion;

    @Schema(description = "Número de participantes inscritos")
    private Integer numeroParticipantes;

    @Schema(description = "Lista de nombres de participantes")
    private Set<String> participantes;

    // Constructor vacío
    public EventoSolidarioDTO() {
    }

    // Constructor con parámetros principales
    public EventoSolidarioDTO(String nombreEvento, String descripcionEvento, LocalDateTime fechaEvento) {
        this.nombreEvento = nombreEvento;
        this.descripcionEvento = descripcionEvento;
        this.fechaEvento = fechaEvento;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombreEvento() {
        return nombreEvento;
    }

    public void setNombreEvento(String nombreEvento) {
        this.nombreEvento = nombreEvento;
    }

    public String getDescripcionEvento() {
        return descripcionEvento;
    }

    public void setDescripcionEvento(String descripcionEvento) {
        this.descripcionEvento = descripcionEvento;
    }

    public LocalDateTime getFechaEvento() {
        return fechaEvento;
    }

    public void setFechaEvento(LocalDateTime fechaEvento) {
        this.fechaEvento = fechaEvento;
    }

    public Boolean getEliminado() {
        return eliminado;
    }

    public void setEliminado(Boolean eliminado) {
        this.eliminado = eliminado;
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

    public Integer getNumeroParticipantes() {
        return numeroParticipantes;
    }

    public void setNumeroParticipantes(Integer numeroParticipantes) {
        this.numeroParticipantes = numeroParticipantes;
    }

    public Set<String> getParticipantes() {
        return participantes;
    }

    public void setParticipantes(Set<String> participantes) {
        this.participantes = participantes;
    }

    public Integer getUsuarioAltaId() {
        return usuarioAltaId;
    }

    public void setUsuarioAltaId(Integer usuarioAltaId) {
        this.usuarioAltaId = usuarioAltaId;
    }
}
