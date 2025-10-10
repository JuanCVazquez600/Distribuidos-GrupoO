package Distribuidos_GrupoO.ServidorGRPC.service.kafka.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class SolidaryEventProducer {
    private static final String TOPIC = "eventos-solidarios";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void publishEvent(SolidaryEvent event) {
        kafkaTemplate.send(TOPIC, event);
    }

    //prueba del punto 6
/*
    public void deleteEvent(SolidaryEvent event) {
        kafkaTemplate.send(TOPIC, event);
    }

    */
}