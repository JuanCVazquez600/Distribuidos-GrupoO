package com.ong.donationexcel.repository;

import com.ong.donationexcel.model.EventoSolidario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventoSolidarioRepository extends JpaRepository<EventoSolidario, Integer> {

    /**
     * Busca todos los eventos que no estén eliminados y carga los participantes
     */
    @Query("SELECT DISTINCT e FROM EventoSolidario e LEFT JOIN FETCH e.participantes WHERE e.eliminado = false")
    List<EventoSolidario> findByEliminadoFalse();

    /**
     * Busca eventos por rango de fechas que no estén eliminados
     */
    @Query("SELECT e FROM EventoSolidario e WHERE e.fechaEvento >= :fechaInicio AND e.fechaEvento <= :fechaFin AND e.eliminado = false ORDER BY e.fechaEvento DESC")
    List<EventoSolidario> findByFechaEventoBetweenAndEliminadoFalse(
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin
    );

    /**
     * Busca eventos futuros que no estén eliminados
     */
    @Query("SELECT e FROM EventoSolidario e WHERE e.fechaEvento > :now AND e.eliminado = false ORDER BY e.fechaEvento ASC")
    List<EventoSolidario> findByFechaEventoAfterAndEliminadoFalse(@Param("now") LocalDateTime now);

    /**
     * Busca eventos por nombre que contenga el texto especificado
     */
    List<EventoSolidario> findByNombreEventoContainingIgnoreCaseAndEliminadoFalse(String nombre);

    /**
     * Busca eventos por descripción que contenga el texto especificado
     */
    List<EventoSolidario> findByDescripcionEventoContainingIgnoreCaseAndEliminadoFalse(String descripcion);

    /**
     * Busca eventos con participantes (para reportes de participación)
     */
    @Query("SELECT DISTINCT e FROM EventoSolidario e JOIN e.participantes p WHERE e.eliminado = false")
    List<EventoSolidario> findEventosConParticipantes();

    /**
     * Busca eventos por participante específico
     */
    @Query("SELECT e FROM EventoSolidario e JOIN e.participantes p WHERE p.id = :usuarioId AND e.eliminado = false")
    List<EventoSolidario> findByParticipanteId(@Param("usuarioId") Integer usuarioId);

    /**
     * Cuenta eventos por rango de fechas
     */
    @Query("SELECT COUNT(e) FROM EventoSolidario e WHERE e.fechaEvento >= :fechaInicio AND e.fechaEvento <= :fechaFin AND e.eliminado = false")
    Long countByFechaEventoBetweenAndEliminadoFalse(
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin
    );
}
