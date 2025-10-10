package Distribuidos_GrupoO.ServidorGRPC.repository;

import Distribuidos_GrupoO.ServidorGRPC.model.AdhesionEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdhesionEventoRepository extends JpaRepository<AdhesionEvento, Integer> {

    /**
     * Busca adhesiones por evento
     */
    List<AdhesionEvento> findByEventId(String eventId);

    /**
     * Busca adhesiones por estado
     */
    List<AdhesionEvento> findByEstado(AdhesionEvento.EstadoAdhesion estado);

    /**
     * Busca adhesiones por organización del voluntario
     */
    List<AdhesionEvento> findByIdOrganizacionVoluntario(String idOrganizacionVoluntario);

    /**
     * Busca una adhesión específica por evento y voluntario
     */
    Optional<AdhesionEvento> findByEventIdAndIdVoluntario(String eventId, String idVoluntario);

    /**
     * Busca adhesiones pendientes por evento
     */
    List<AdhesionEvento> findByEventIdAndEstado(String eventId, AdhesionEvento.EstadoAdhesion estado);
}