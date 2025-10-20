package Distribuidos_GrupoO.ServidorGRPC.controller;

import Distribuidos_GrupoO.ServidorGRPC.service.IInventarioDeDonacionesService;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.transfer.DonationTransfer;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.transfer.DonationTransferProducer;
import Distribuidos_GrupoO.ServidorGRPC.model.InventarioDeDonaciones;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para gestión de transferencias de donaciones entre organizaciones
 */
@RestController
@RequestMapping("/transfers")
public class DonationTransferController {
    @Autowired
    private DonationTransferProducer producer;

    @Autowired
    private IInventarioDeDonacionesService inventarioService;

    @Value("${spring.organization.id:org-123}")
    private String organizationId;

    /**
     * Enviar transferencia de donaciones a otra organización
     */

    @PostMapping("/send/{recipientOrgId}")
    public ResponseEntity<String> sendTransfer(
        @PathVariable String recipientOrgId,
        @RequestBody DonationTransfer transfer) {
        try {
            transfer.setDonorOrganizationId(organizationId);
            transfer.setRecipientOrganizationId(recipientOrgId);
            
            // Solo descontar si es transferencia externa
            if (!organizationId.equals(recipientOrgId)) {
                String validationResult = validateAndProcessTransfer(transfer);
                if (validationResult != null) {
                    return ResponseEntity.badRequest().body(validationResult);
                }
            }
            
            producer.sendTransfer(recipientOrgId, transfer);
            return ResponseEntity.ok("Transferencia publicada correctamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al procesar la transferencia: " + e.getMessage());
        }
    }

    private String validateAndProcessTransfer(DonationTransfer transfer) {
        for (DonationTransfer.DonationItem item : transfer.getDonations()) {
            InventarioDeDonaciones.CategoriaEnum categoria;
            try {
                categoria = InventarioDeDonaciones.CategoriaEnum.valueOf(item.getCategory());
            } catch (IllegalArgumentException e) {
                return "Categoría inválida: " + item.getCategory();
            }
            
            int cantidad = extractQuantity(item.getQuantity());
            if (cantidad <= 0) {
                return "Cantidad inválida: " + item.getQuantity();
            }
            
            try {
                InventarioDeDonaciones inventario = inventarioService
                    .buscarPorCategoriaYDescripcion(categoria, item.getDescription())
                    .orElseThrow(() -> new RuntimeException("Inventario no encontrado para categoría: " 
                        + categoria + ", descripción: " + item.getDescription()));
                inventarioService.actualizarCantidad(inventario, -cantidad);
            } catch (Exception e) {
                return "Error al actualizar inventario: " + e.getMessage();
            }
        }
        return null;
    }

    private int extractQuantity(String quantityStr) {
        try {
            String numStr = quantityStr.replaceAll("[^0-9]", "");
            return Integer.parseInt(numStr);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
