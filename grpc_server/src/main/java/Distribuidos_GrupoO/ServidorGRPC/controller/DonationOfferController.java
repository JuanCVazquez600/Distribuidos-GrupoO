package Distribuidos_GrupoO.ServidorGRPC.controller;

import Distribuidos_GrupoO.ServidorGRPC.service.kafka.offer.DonationOffer;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.offer.DonationOfferConsumer;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.offer.DonationOfferProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/offers")
public class DonationOfferController {
    @Autowired
    private DonationOfferProducer producer;
    @Autowired
    private DonationOfferConsumer consumer;

    @PostMapping("/publish")
    public ResponseEntity<String> publishOffer(@RequestBody DonationOffer offer) {
        producer.sendOffer(offer);
        return ResponseEntity.ok("Oferta publicada correctamente");
    }

    @GetMapping("/list")
    public List<DonationOffer> listOffers() {
        return consumer.getOffers();
    }
}
