package Distribuidos_GrupoO.ServidorGRPC.service.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class DonationTransferConsumer {
    @Value("${spring.organization.id:org-123}") // Cambia este valor por el id real de tu organización
    private String organizationId;

    @KafkaListener(topics = "transferencia-donaciones/${spring.organization.id}", groupId = "transferencias-group", containerFactory = "donationTransferKafkaListenerContainerFactory")
    public void listen(DonationTransfer transfer) {
        // Aquí deberías sumar al inventario local la cantidad recibida
        // Por ejemplo: inventarioService.sumarDonacion(transfer);
        System.out.println("Recibida transferencia para esta organización: " + transfer);
    }
}
