package Distribuidos_GrupoO.ServidorGRPC.model;

import javax.persistence.*;

import Distribuidos_GrupoO.ServidorGRPC.model.EventoMiembros;
import Distribuidos_GrupoO.ServidorGRPC.model.EventoSolidario;
import Distribuidos_GrupoO.ServidorGRPC.model.InventarioDeDonaciones;
import Distribuidos_GrupoO.ServidorGRPC.model.Usuario;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "evento_donaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class EventoDonacion {

    @EmbeddedId
    private EventoMiembros id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("eventoId")
    @JoinColumn(name = "evento_id")
    private EventoSolidario evento;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("donacionId")
    @JoinColumn(name = "donacion_id")
    private InventarioDeDonaciones donacion;

    @Column(name = "cantidad_usada", nullable = false)
    private Integer cantidadUsada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_modificacion")
    private Usuario usuarioModificacion;

    @Column(name = "fecha_modificacion", nullable = false)
    private LocalDateTime fechaModificacion;

}