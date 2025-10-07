package Distribuidos_GrupoO.ServidorGRPC.service.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class DonationTransferProducer {
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendTransfer(String recipientOrgId, DonationTransfer transfer) {
        // Use a fixed topic name without slashes
        String topic = "transferencia-donaciones";
        kafkaTemplate.send(topic, transfer);
    }
}
