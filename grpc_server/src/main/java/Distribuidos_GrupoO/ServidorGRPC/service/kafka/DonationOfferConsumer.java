package Distribuidos_GrupoO.ServidorGRPC.service.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DonationOfferConsumer {
    private final List<DonationOffer> offers = new ArrayList<>();

    @KafkaListener(topics = "oferta-donaciones", groupId = "ofertas-group", containerFactory = "donationOfferKafkaListenerContainerFactory")
    public void listen(DonationOffer offer) {
        offers.add(offer);
    }

    public List<DonationOffer> getOffers() {
        return new ArrayList<>(offers);
    }
}
