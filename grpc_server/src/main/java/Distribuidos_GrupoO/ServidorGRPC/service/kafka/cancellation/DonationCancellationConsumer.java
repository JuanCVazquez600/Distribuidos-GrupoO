package Distribuidos_GrupoO.ServidorGRPC.service.kafka.cancellation;

import Distribuidos_GrupoO.ServidorGRPC.service.kafka.request.DonationRequestConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class DonationCancellationConsumer {

    @Autowired
    private DonationRequestConsumer donationRequestConsumer;

    @KafkaListener(topics = "baja-solicitud-donaciones", groupId = "baja-solicitudes-group", containerFactory = "donationCancellationKafkaListenerContainerFactory")
    public void listen(DonationCancellation cancellation) {
        // Procesar la baja de solicitud
        processCancellation(cancellation);
    }

    private void processCancellation(DonationCancellation cancellation) {
        // Remover la solicitud de la lista de solicitudes activas
        donationRequestConsumer.removeRequest(cancellation.getOrganizationId(), cancellation.getRequestId());
        
        System.out.println("Solicitud dada de baja: " + 
                          "Organización: " + cancellation.getOrganizationId() + 
                          ", Solicitud ID: " + cancellation.getRequestId());
    }
}