package Distribuidos_GrupoO.ServidorGRPC.service.kafka.transfer;

import Distribuidos_GrupoO.ServidorGRPC.service.IInventarioDeDonacionesService;
import Distribuidos_GrupoO.ServidorGRPC.model.InventarioDeDonaciones;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class DonationTransferConsumer {
    @Value("${spring.organization.id:org-123}") // Cambia este valor por el id real de tu organización
    private String organizationId;

    @Autowired
    private IInventarioDeDonacionesService inventarioService;

    @KafkaListener(topics = "transferencia-donaciones", groupId = "transferencias-group", containerFactory = "donationTransferKafkaListenerContainerFactory")
    public void listen(DonationTransfer transfer) {
        try {
            // Verificar si la transferencia es para nuestra organización
            if (transfer.getRecipientOrganizationId() == null || !organizationId.equals(transfer.getRecipientOrganizationId())) {
                // Si no es para nosotros, ignoramos el mensaje
                System.out.println("Transferencia ignorada - no es para nuestra organización. Destinatario: " + transfer.getRecipientOrganizationId());
                return;
            }
            
            System.out.println("Procesando transferencia para nuestra organización: " + organizationId);
            for (DonationTransfer.DonationItem item : transfer.getDonations()) {
                InventarioDeDonaciones.CategoriaEnum categoria;
                try {
                    categoria = InventarioDeDonaciones.CategoriaEnum.valueOf(item.getCategory());
                } catch (IllegalArgumentException e) {
                    System.err.println("Categoría inválida: " + item.getCategory());
                    continue;
                }
                String descripcion = item.getDescription();
                int cantidad;
                try {
                    String cantidadStr = item.getQuantity().replaceAll("[^0-9]", "");
                    cantidad = Integer.parseInt(cantidadStr);
                } catch (NumberFormatException e) {
                    System.err.println("Cantidad inválida: " + item.getQuantity());
                    continue;
                }
                inventarioService.crearOActualizarInventario(categoria, descripcion, cantidad);
            }
            System.out.println("Recibida transferencia para esta organización: " + transfer);
        } catch (Exception e) {
            System.err.println("Error al procesar transferencia: " + e.getMessage());
        }
    }
}
