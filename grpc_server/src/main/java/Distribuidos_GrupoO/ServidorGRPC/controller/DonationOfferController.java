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
    
    @Autowired
    private Distribuidos_GrupoO.ServidorGRPC.repository.DonationOfferRepository repository;
    
    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    /**
     * Publica una oferta de donación en Kafka
     */
    @PostMapping("/publish")
    public ResponseEntity<String> publishOffer(@RequestBody DonationOffer offer) {
        producer.sendOffer(offer);
        return ResponseEntity.ok("Oferta publicada correctamente");
    }

    /**
     * Lista ofertas desde la base de datos
     */
    @GetMapping("/list")
    public List<DonationOffer> listOffers() {
        return repository.findAll().stream()
            .map(entity -> {
                try {
                    List<Distribuidos_GrupoO.ServidorGRPC.service.kafka.DonationItem> donations = null;
                    if (entity.getDonationsJson() != null && !entity.getDonationsJson().isEmpty()) {
                        donations = objectMapper.readValue(entity.getDonationsJson(), 
                            new com.fasterxml.jackson.core.type.TypeReference<List<Distribuidos_GrupoO.ServidorGRPC.service.kafka.DonationItem>>() {});
                    }
                    return new DonationOffer(entity.getOfferId(), entity.getOrganizationId(), donations);
                } catch (Exception e) {
                    e.printStackTrace();
                    return new DonationOffer(entity.getOfferId(), entity.getOrganizationId(), null);
                }
            })
            .collect(java.util.stream.Collectors.toList());
    }
}
