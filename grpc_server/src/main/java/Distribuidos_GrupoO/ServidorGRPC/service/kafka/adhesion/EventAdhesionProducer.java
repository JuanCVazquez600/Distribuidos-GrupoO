package Distribuidos_GrupoO.ServidorGRPC.service.kafka.adhesion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventAdhesionProducer {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Envía una adhesión de voluntario al topic específico del organizador del evento
     * @param organizadorId ID de la organización organizadora del evento
     * @param adhesion Datos de la adhesión del voluntario
     */
    public void sendAdhesion(String organizadorId, EventAdhesion adhesion) {
        try {
            // Construir el topic específico para el organizador
            // Formato: adhesion-evento-{organizadorId}
            String topic = "adhesion-evento-" + organizadorId;
            
            System.out.println("Enviando adhesión a evento: " + adhesion);
            System.out.println("Topic destino: " + topic);
            
            kafkaTemplate.send(topic, adhesion);
            
            System.out.println("✅ Adhesión enviada correctamente al topic: " + topic);
            
        } catch (Exception e) {
            System.err.println("❌ Error al enviar adhesión: " + e.getMessage());
            throw e;
        }
    }
}