package Distribuidos_GrupoO.ServidorGRPC.service.kafka.cancellation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class DonationCancellationProducer {
    private static final String TOPIC = "baja-solicitud-donaciones";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendCancellation(DonationCancellation cancellation) {
        kafkaTemplate.send(TOPIC, cancellation);
    }
}