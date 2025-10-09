package Distribuidos_GrupoO.ServidorGRPC.service.kafka.offer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class DonationOfferProducer {
    private static final String TOPIC = "oferta-donaciones";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendOffer(DonationOffer offer) {
        kafkaTemplate.send(TOPIC, offer);
    }
}
