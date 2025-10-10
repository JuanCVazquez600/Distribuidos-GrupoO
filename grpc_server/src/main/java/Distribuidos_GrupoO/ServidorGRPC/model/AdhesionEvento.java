package Distribuidos_GrupoO.ServidorGRPC.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "adhesion_evento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdhesionEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(name = "id_organizacion_voluntario", nullable = false)
    private String idOrganizacionVoluntario;

    @Column(name = "id_voluntario", nullable = false)
    private String idVoluntario;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(length = 20)
    private String telefono;

    @Column(nullable = false, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoAdhesion estado = EstadoAdhesion.PENDIENTE;

    @Column(name = "fecha_adhesion", nullable = false)
    private LocalDateTime fechaAdhesion = LocalDateTime.now();

    @Column(name = "fecha_procesamiento")
    private LocalDateTime fechaProcesamiento;

    @Column(length = 500)
    private String observaciones;

    public enum EstadoAdhesion {
        PENDIENTE,
        CONFIRMADA,
        RECHAZADA,
        CANCELADA
    }

    // Getters y Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getIdOrganizacionVoluntario() {
        return idOrganizacionVoluntario;
    }

    public void setIdOrganizacionVoluntario(String idOrganizacionVoluntario) {
        this.idOrganizacionVoluntario = idOrganizacionVoluntario;
    }

    public String getIdVoluntario() {
        return idVoluntario;
    }

    public void setIdVoluntario(String idVoluntario) {
        this.idVoluntario = idVoluntario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public EstadoAdhesion getEstado() {
        return estado;
    }

    public void setEstado(EstadoAdhesion estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaAdhesion() {
        return fechaAdhesion;
    }

    public void setFechaAdhesion(LocalDateTime fechaAdhesion) {
        this.fechaAdhesion = fechaAdhesion;
    }

    public LocalDateTime getFechaProcesamiento() {
        return fechaProcesamiento;
    }

    public void setFechaProcesamiento(LocalDateTime fechaProcesamiento) {
        this.fechaProcesamiento = fechaProcesamiento;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}