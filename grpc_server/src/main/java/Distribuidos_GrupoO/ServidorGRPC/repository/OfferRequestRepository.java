package Distribuidos_GrupoO.ServidorGRPC.repository;

import Distribuidos_GrupoO.ServidorGRPC.model.AdhesionEvento;
import Distribuidos_GrupoO.ServidorGRPC.model.OfferRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferRequestRepository extends JpaRepository<OfferRequestEntity, Integer> {

}
