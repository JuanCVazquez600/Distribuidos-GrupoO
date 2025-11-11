package Distribuidos_GrupoO.ServidorGRPC.service.kafka.offerrequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
public class OfferRequestProducer {
    
    // Topic único global según enunciado del recuperatorio
    private static final String TOPIC = "oferta-solicitud";
    private static final Logger logger = LoggerFactory.getLogger(OfferRequestProducer.class);


    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendOfferRequest(OfferRequest offerRequest) {
        try {
            System.out.println("Enviando solicitud de oferta: " + offerRequest);
            kafkaTemplate.send(TOPIC, offerRequest);
            logger.info(" Solicitud de oferta enviada correctamente al topic: " + TOPIC);
        } catch (Exception e) {
           logger.error(" Error al enviar solicitud de oferta: " + e.getMessage());
            throw e;
        }
    }
}
