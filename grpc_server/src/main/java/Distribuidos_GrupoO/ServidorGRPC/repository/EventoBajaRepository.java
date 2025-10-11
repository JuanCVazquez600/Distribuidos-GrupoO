package Distribuidos_GrupoO.ServidorGRPC.repository;

import Distribuidos_GrupoO.ServidorGRPC.model.EventoBaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventoBajaRepository extends JpaRepository<EventoBaja, Integer> {

    /**
     * Busca bajas por organización
     */
    List<EventoBaja> findByOrganizacionId(String organizacionId);

    /**
     * Busca bajas por evento específico
     */
    List<EventoBaja> findByEventoId(String eventoId);

    /**
     * Busca bajas por estado
     */
    List<EventoBaja> findByEstado(EventoBaja.EstadoBaja estado);

    /**
     * Busca una baja específica por organización y evento
     */
    Optional<EventoBaja> findByOrganizacionIdAndEventoId(String organizacionId, String eventoId);

    /**
     * Busca bajas que existían en nuestra BD
     */
    List<EventoBaja> findByExistiaEnBdTrue();

    /**
     * Busca bajas por rango de fechas
     */
    List<EventoBaja> findByFechaBajaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Busca bajas pendientes de revisión
     */
    @Query("SELECT eb FROM EventoBaja eb WHERE eb.estado = 'PROCESADO' AND eb.existiaEnBd = true")
    List<EventoBaja> findBajasPendientesRevision();

    /**
     * Cuenta bajas por organización
     */
    @Query("SELECT COUNT(eb) FROM EventoBaja eb WHERE eb.organizacionId = :organizacionId")
    Long countByOrganizacionId(@Param("organizacionId") String organizacionId);

    /**
     * Busca últimas bajas (para dashboard/reportes)
     */
    @Query("SELECT eb FROM EventoBaja eb ORDER BY eb.fechaProcesamiento DESC")
    List<EventoBaja> findUltimasBajas();
}