package Distribuidos_GrupoO.ServidorGRPC.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad para persistir las bajas de eventos recibidas por Kafka
 */
@Entity
@Table(name = "evento_baja")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventoBaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "organizacion_id", nullable = false, length = 100)
    private String organizacionId;

    @Column(name = "evento_id", nullable = false, length = 50)
    private String eventoId;

    @Column(name = "fecha_baja", nullable = false)
    private LocalDateTime fechaBaja;

    @Column(name = "existia_en_bd", nullable = false)
    private Boolean existiaEnBd;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoBaja estado;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Column(name = "fecha_procesamiento", nullable = false)
    private LocalDateTime fechaProcesamiento;

    /**
     * Estados posibles de una baja de evento
     */
    public enum EstadoBaja {
        RECIBIDO,       // Mensaje recibido pero no procesado
        PROCESADO,      // Baja procesada correctamente
        REVISADO,       // Revisado manualmente (para eventos que existían en BD)
        ERROR           // Error al procesar
    }

    // Constructor de conveniencia
    public EventoBaja(String organizacionId, String eventoId, Boolean existiaEnBd, EstadoBaja estado, String observaciones) {
        this.organizacionId = organizacionId;
        this.eventoId = eventoId;
        this.existiaEnBd = existiaEnBd;
        this.estado = estado;
        this.observaciones = observaciones;
        this.fechaBaja = LocalDateTime.now();
        this.fechaProcesamiento = LocalDateTime.now();
    }

    // Getters y Setters manuales para compatibilidad
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOrganizacionId() {
        return organizacionId;
    }

    public void setOrganizacionId(String organizacionId) {
        this.organizacionId = organizacionId;
    }

    public String getEventoId() {
        return eventoId;
    }

    public void setEventoId(String eventoId) {
        this.eventoId = eventoId;
    }

    public LocalDateTime getFechaBaja() {
        return fechaBaja;
    }

    public void setFechaBaja(LocalDateTime fechaBaja) {
        this.fechaBaja = fechaBaja;
    }

    public Boolean getExistiaEnBd() {
        return existiaEnBd;
    }

    public void setExistiaEnBd(Boolean existiaEnBd) {
        this.existiaEnBd = existiaEnBd;
    }

    public EstadoBaja getEstado() {
        return estado;
    }

    public void setEstado(EstadoBaja estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDateTime getFechaProcesamiento() {
        return fechaProcesamiento;
    }

    public void setFechaProcesamiento(LocalDateTime fechaProcesamiento) {
        this.fechaProcesamiento = fechaProcesamiento;
    }

    @Override
    public String toString() {
        return "EventoBaja{" +
                "id=" + id +
                ", organizacionId='" + organizacionId + '\'' +
                ", eventoId='" + eventoId + '\'' +
                ", fechaBaja=" + fechaBaja +
                ", existiaEnBd=" + existiaEnBd +
                ", estado=" + estado +
                ", observaciones='" + observaciones + '\'' +
                ", fechaProcesamiento=" + fechaProcesamiento +
                '}';
    }
}