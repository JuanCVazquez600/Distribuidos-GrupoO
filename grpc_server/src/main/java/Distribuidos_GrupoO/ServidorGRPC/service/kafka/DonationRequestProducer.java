package Distribuidos_GrupoO.ServidorGRPC.service.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class DonationRequestProducer {
    private static final String TOPIC = "solicitud-donaciones";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendRequest(DonationRequest request) {
        kafkaTemplate.send(TOPIC, request);
    }
}
