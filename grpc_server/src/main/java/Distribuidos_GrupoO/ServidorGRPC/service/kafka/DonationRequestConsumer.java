package Distribuidos_GrupoO.ServidorGRPC.service.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class DonationRequestConsumer {
    private final List<DonationRequest> requests = new ArrayList<>();

    @KafkaListener(topics = "solicitud-donaciones", groupId = "solicitudes-group", containerFactory = "donationRequestKafkaListenerContainerFactory")
    public void listen(DonationRequest request) {
        requests.add(request);
    }

    public List<DonationRequest> getRequests() {
        return new ArrayList<>(requests);
    }
}
