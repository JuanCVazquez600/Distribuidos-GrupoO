package Distribuidos_GrupoO.ServidorGRPC.repository;

import Distribuidos_GrupoO.ServidorGRPC.model.DonationOfferEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DonationOfferRepository extends JpaRepository<DonationOfferEntity, Integer> {
    Optional<DonationOfferEntity> findByOfferId(String offerId);
    boolean existsByOfferIdAndOrganizationId(String offerId, String organizationId);
}