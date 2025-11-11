package Distribuidos_GrupoO.ServidorGRPC.controller;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.offerrequest.OfferRequest;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.offerrequest.OfferRequestConsumer;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.offerrequest.OfferRequestProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/offers-request")
public class OfferRequestController {

    @Autowired
    private OfferRequestProducer producer;

    @Autowired
    private OfferRequestConsumer consumer;




        @PostMapping("/send")
        public ResponseEntity<String> sendOfferRequest (@RequestBody OfferRequest offerRequest) {
            try {
                producer.sendOfferRequest(offerRequest);
                return ResponseEntity.ok("Solicitud de oferta enviada correctamente");


            } catch (Exception e) {
                return ResponseEntity.internalServerError()
                        .body("Error al procesar la solicitud" + e.getMessage());
            }
        }
}