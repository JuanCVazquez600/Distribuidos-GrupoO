package Distribuidos_GrupoO.ServidorGRPC.model;

import lombok.*;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@Table(name = "evento_miembros")
public class EventoMiembros implements Serializable {

    private Integer eventoId;
    private Integer donacionId;

    public EventoMiembros() {
    }

    public EventoMiembros(Integer eventoId, Integer donacionId) {
        this.eventoId = eventoId;
        this.donacionId = donacionId;
    }
}
