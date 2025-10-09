package Distribuidos_GrupoO.ServidorGRPC.controller;

import Distribuidos_GrupoO.ServidorGRPC.service.kafka.request.DonationRequest;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.request.DonationRequestConsumer;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.request.DonationRequestProducer;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.cancellation.DonationCancellation;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.cancellation.DonationCancellationProducer;
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

    @Autowired
    private DonationCancellationProducer cancellationProducer;

    @PostMapping("/publish")
    public ResponseEntity<String> publishRequest(@RequestBody DonationRequest request) {
        producer.sendRequest(request);
        return ResponseEntity.ok("Solicitud publicada correctamente");
    }

    @GetMapping("/list")
    public List<DonationRequest> listRequests() {
        return consumer.getRequests();
    }


    @PostMapping("/cancel")
    public ResponseEntity<String> cancelRequest(@RequestBody DonationCancellation cancellation) {
        cancellationProducer.sendCancellation(cancellation);
        return ResponseEntity.ok("Solicitud dada de baja correctamente");
    }
}
