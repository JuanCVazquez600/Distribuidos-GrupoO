package Distribuidos_GrupoO.ServidorGRPC.controller;

import Distribuidos_GrupoO.ServidorGRPC.service.IInventarioDeDonacionesService;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.transfer.DonationTransfer;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.transfer.DonationTransferProducer;
import Distribuidos_GrupoO.ServidorGRPC.model.InventarioDeDonaciones;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transfers")
public class DonationTransferController {
    @Autowired
    private DonationTransferProducer producer;

    @Autowired
    private IInventarioDeDonacionesService inventarioService;

    @PostMapping("/send/{recipientOrgId}")
    public ResponseEntity<String> sendTransfer(
        @PathVariable String recipientOrgId,
        @RequestBody DonationTransfer transfer) {
        try {
            // Descontar del inventario local la cantidad donada
            for (DonationTransfer.DonationItem item : transfer.getDonations()) {
                InventarioDeDonaciones.CategoriaEnum categoria;
                try {
                    categoria = InventarioDeDonaciones.CategoriaEnum.valueOf(item.getCategory());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body("Categoría inválida: " + item.getCategory());
                }
                String descripcion = item.getDescription();
                int cantidad;
                try {
                    // Asumimos que la cantidad es un número entero seguido opcionalmente de unidad, ej: "2kg"
                    String cantidadStr = item.getQuantity().replaceAll("[^0-9]", "");
                    cantidad = Integer.parseInt(cantidadStr);
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest().body("Cantidad inválida: " + item.getQuantity());
                }
                // Buscar inventario y descontar cantidad
                InventarioDeDonaciones inventario = inventarioService.buscarPorCategoriaYDescripcion(categoria, descripcion)
                        .orElseThrow(() -> new RuntimeException("Inventario no encontrado para categoría: " + categoria + ", descripción: " + descripcion));
                inventarioService.actualizarCantidad(inventario, -cantidad);
            }
            // Enviar transferencia
            producer.sendTransfer(recipientOrgId, transfer);
            return ResponseEntity.ok("Transferencia publicada correctamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al procesar la transferencia: " + e.getMessage());
        }
    }
}
