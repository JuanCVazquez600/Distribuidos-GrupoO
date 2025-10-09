package Distribuidos_GrupoO.ServidorGRPC.service.kafka.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SolidaryEventConsumer {
    
    // ID de nuestra organización (configurable)
    @Value("${app.organization.id:org-local}")
    private String ourOrganizationId;
    
    // Lista de eventos externos (de otras organizaciones)
    private final List<SolidaryEvent> externalEvents = new ArrayList<>();

    @KafkaListener(topics = "eventos-solidarios", groupId = "eventos-group", containerFactory = "solidaryEventKafkaListenerContainerFactory")
    public void listen(SolidaryEvent event) {
        processEvent(event);
    }

    private void processEvent(SolidaryEvent event) {
        // 1. Descartar eventos propios
        if (isOwnEvent(event)) {
            System.out.println("Evento propio descartado: " + event.getEventId());
            return;
        }

        // 2. Verificar que el evento esté vigente (fecha futura)
        if (!isEventValid(event)) {
            System.out.println("Evento no vigente descartado: " + event.getEventId() + 
                             " - Fecha: " + event.getDateTime());
            return;
        }

        // 3. Guardar evento externo válido
        synchronized (externalEvents) {
            // Evitar duplicados
            if (externalEvents.stream().noneMatch(e -> 
                e.getOrganizationId().equals(event.getOrganizationId()) && 
                e.getEventId().equals(event.getEventId()))) {
                
                externalEvents.add(event);
                System.out.println("Evento externo agregado: " + 
                                 "Organización: " + event.getOrganizationId() + 
                                 ", Evento: " + event.getEventName() + 
                                 ", Fecha: " + event.getDateTime());
            }
        }
    }

    private boolean isOwnEvent(SolidaryEvent event) {
        return ourOrganizationId.equals(event.getOrganizationId());
    }

    private boolean isEventValid(SolidaryEvent event) {
        // El evento debe ser en el futuro para considerarse vigente
        return event.getDateTime() != null && 
               event.getDateTime().isAfter(LocalDateTime.now());
    }

    public List<SolidaryEvent> getExternalEvents() {
        synchronized (externalEvents) {
            // Filtrar eventos que siguen siendo vigentes al momento de la consulta
            return externalEvents.stream()
                    .filter(this::isEventValid)
                    .toList();
        }
    }

    public List<SolidaryEvent> getAllExternalEvents() {
        synchronized (externalEvents) {
            return new ArrayList<>(externalEvents);
        }
    }
}