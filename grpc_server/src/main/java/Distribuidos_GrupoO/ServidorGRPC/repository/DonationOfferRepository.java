package Distribuidos_GrupoO.ServidorGRPC.repository;

import Distribuidos_GrupoO.ServidorGRPC.model.DonationOfferEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DonationOfferRepository extends JpaRepository<DonationOfferEntity, Integer> {
    
   // Busca una oferta por su offerId usando SQL nativo
    @Query(value = "SELECT * FROM donation_offers WHERE offer_id = :offerId", nativeQuery = true)
    Optional<DonationOfferEntity> findByOfferId(@Param("offerId") String offerId);

    // Verifica si existe una oferta con el offerId especificado y pertenece a la organización dada
    // Spring Data genera automáticamente: SELECT COUNT(*) > 0 FROM donation_offers WHERE offer_id = ?1 AND organization_id = ?2
    boolean existsByOfferIdAndOrganizationId(String offerId, String organizationId);
}