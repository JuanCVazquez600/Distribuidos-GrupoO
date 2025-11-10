package Distribuidos_GrupoO.ServidorGRPC.service.kafka.offer;

import Distribuidos_GrupoO.ServidorGRPC.model.DonationOfferEntity;
import Distribuidos_GrupoO.ServidorGRPC.repository.DonationOfferRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DonationOfferConsumer {
    private final List<DonationOffer> offers = new ArrayList<>();

    @Autowired
    private DonationOfferRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "oferta-donaciones", groupId = "ofertas-group", containerFactory = "donationOfferKafkaListenerContainerFactory")
    public void listen(DonationOffer offer) {
        offers.add(offer);
        
        // Solo guardar ofertas de nuestra organización
        if ("org-300".equals(offer.getOrganizationId())) {
            try {
                DonationOfferEntity entity = new DonationOfferEntity();
                entity.setOfferId(offer.getOfferId());
                entity.setOrganizationId(offer.getOrganizationId());
                entity.setDonationsJson(objectMapper.writeValueAsString(offer.getDonations()));
                repository.save(entity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public List<DonationOffer> getOffers() {
        return new ArrayList<>(offers);
    }
}
