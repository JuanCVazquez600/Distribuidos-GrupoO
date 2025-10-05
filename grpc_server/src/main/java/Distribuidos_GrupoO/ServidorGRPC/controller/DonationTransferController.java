package Distribuidos_GrupoO.ServidorGRPC.controller;

import Distribuidos_GrupoO.ServidorGRPC.service.kafka.DonationTransfer;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.DonationTransferProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transfers")
public class DonationTransferController {
    @Autowired
    private DonationTransferProducer producer;

    @PostMapping("/send/{recipientOrgId}")
    public ResponseEntity<String> sendTransfer(
        @PathVariable String recipientOrgId,
        @RequestBody DonationTransfer transfer) {
        producer.sendTransfer(recipientOrgId, transfer);
        // Aquí deberías descontar del inventario local la cantidad donada
        return ResponseEntity.ok("Transferencia publicada correctamente");
    }
}
