package Distribuidos_GrupoO.ServidorGRPC.repository;

import Distribuidos_GrupoO.ServidorGRPC.model.EventoDonacion;
import Distribuidos_GrupoO.ServidorGRPC.model.EventoMiembros;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventoDonacionRepository extends JpaRepository<EventoDonacion, EventoMiembros> {


}
