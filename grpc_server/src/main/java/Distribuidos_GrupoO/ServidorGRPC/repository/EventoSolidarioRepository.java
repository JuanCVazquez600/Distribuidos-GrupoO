package Distribuidos_GrupoO.ServidorGRPC.repository;

import Distribuidos_GrupoO.ServidorGRPC.model.EventoSolidario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventoSolidarioRepository extends JpaRepository<EventoSolidario, Integer> {

    List<EventoSolidario> findByFechaEventoAfter(LocalDateTime fecha);

    List<EventoSolidario> findByFechaEventoBefore(LocalDateTime fecha);

}