package Distribuidos_GrupoO.ServidorGRPC.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Table(name = "evento_solidario")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class EventoSolidario {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        @Column(name = "nombre_evento", nullable = false, length = 255)
        private String nombreEvento;

        @Column(name = "descripcion_evento", nullable = false, length = 500)
        private String descripcionEvento;

        @Column(name = "fecha_evento", nullable = false)
        private LocalDateTime fechaEvento;

        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(
                name = "evento_miembros",
                joinColumns = @JoinColumn(name = "evento_id"),
                inverseJoinColumns = @JoinColumn(name = "usuario_id")
        )
        private Set<Usuario> miembros;


        //Getters y Setters


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

    public Set<Usuario> getMiembros() {
        return miembros;
    }

    public void setMiembros(Set<Usuario> miembros) {
        this.miembros = miembros;
    }
}