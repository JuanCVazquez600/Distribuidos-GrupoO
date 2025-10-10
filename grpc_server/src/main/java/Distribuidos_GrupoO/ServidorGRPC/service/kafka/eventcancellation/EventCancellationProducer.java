package Distribuidos_GrupoO.ServidorGRPC.service.kafka.eventcancellation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventCancellationProducer {
    private static final String TOPIC = "baja-evento-solidario";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendEventCancellation(EventCancellation eventCancellation) {
        try {
            System.out.println("Enviando mensaje de baja de evento: " + eventCancellation);
            kafkaTemplate.send(TOPIC, eventCancellation);
            System.out.println("Mensaje de baja de evento enviado correctamente al topic: " + TOPIC);
        } catch (Exception e) {
            System.err.println("Error al enviar mensaje de baja de evento: " + e.getMessage());
            throw e;
        }
    }
}