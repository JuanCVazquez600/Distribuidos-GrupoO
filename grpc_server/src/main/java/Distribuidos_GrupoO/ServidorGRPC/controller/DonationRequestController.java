package Distribuidos_GrupoO.ServidorGRPC.controller;

import Distribuidos_GrupoO.ServidorGRPC.service.kafka.DonationRequest;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.DonationRequestConsumer;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.DonationRequestProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/requests")
public class DonationRequestController {
    @Autowired
    private DonationRequestProducer producer;

    @Autowired
    private DonationRequestConsumer consumer;

    @PostMapping("/publish")
    public ResponseEntity<String> publishRequest(@RequestBody DonationRequest request) {
        producer.sendRequest(request);
        return ResponseEntity.ok("Solicitud publicada correctamente");
    }

    @GetMapping("/list")
    public List<DonationRequest> listRequests() {
        return consumer.getRequests();
    }
}
