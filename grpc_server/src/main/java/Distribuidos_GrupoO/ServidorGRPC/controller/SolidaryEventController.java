package Distribuidos_GrupoO.ServidorGRPC.controller;

import Distribuidos_GrupoO.ServidorGRPC.service.kafka.event.SolidaryEvent;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.event.SolidaryEventConsumer;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.event.SolidaryEventProducer;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.eventcancellation.EventCancellation;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.eventcancellation.EventCancellationProducer;
import Distribuidos_GrupoO.ServidorGRPC.model.EventoBaja;
import Distribuidos_GrupoO.ServidorGRPC.model.EventoSolidario;
import Distribuidos_GrupoO.ServidorGRPC.repository.EventoBajaRepository;
import Distribuidos_GrupoO.ServidorGRPC.repository.EventoSolidarioRepository;
import Distribuidos_GrupoO.ServidorGRPC.util.SolidaryEventMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@RestController
@RequestMapping("/events")
public class SolidaryEventController {

    @Autowired
    private SolidaryEventProducer eventProducer;

    @Autowired
    private SolidaryEventConsumer eventConsumer;

    @Autowired
    private EventCancellationProducer eventCancellationProducer;

    @Autowired
    private EventoBajaRepository eventoBajaRepository;

    @Autowired
    private EventoSolidarioRepository eventoSolidarioRepository;

    @PostMapping("/publish")
    public ResponseEntity<String> publishEvent(@RequestBody SolidaryEvent event) {
        try {
            // 1. Publicar en Kafka para otras organizaciones
            eventProducer.publishEvent(event);
            
            // 2. Guardar en BD local para que miembros de otras org puedan unirse
            EventoSolidario eventoLocal = SolidaryEventMapper.toEntity(event);
            eventoSolidarioRepository.save(eventoLocal);
            
            return ResponseEntity.ok("Evento solidario publicado y guardado correctamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Error al publicar evento: " + e.getMessage());
        }
    }

    @GetMapping("/external")
    public ResponseEntity<List<SolidaryEvent>> getExternalEvents() {
        // Combinar eventos de memoria y BD
        List<SolidaryEvent> memoryEvents = eventConsumer.getExternalEvents();
        List<SolidaryEvent> dbEvents = eventConsumer.getPersistedExternalEventsAsSolidaryEvents();
        
        // Unir ambas listas sin duplicados
        List<SolidaryEvent> allExternalEvents = new ArrayList<>(memoryEvents);
        for (SolidaryEvent dbEvent : dbEvents) {
            if (allExternalEvents.stream().noneMatch(e -> 
                e.getEventId().equals(dbEvent.getEventId()) && 
                e.getOrganizationId().equals(dbEvent.getOrganizationId()))) {
                allExternalEvents.add(dbEvent);
            }
        }
        
        return ResponseEntity.ok(allExternalEvents);
    }

    @GetMapping("/external/all")
    public ResponseEntity<List<SolidaryEvent>> getAllExternalEvents() {
        List<SolidaryEvent> allExternalEvents = eventConsumer.getAllExternalEvents();
        return ResponseEntity.ok(allExternalEvents);
    }

    @GetMapping("/my-events")
    public ResponseEntity<List<EventoSolidario>> getMyEvents() {
        // Eventos que publiqué desde mi organización (disponibles para que otros se unan)
        List<EventoSolidario> myEvents = eventoSolidarioRepository.findAll();
        return ResponseEntity.ok(myEvents);
    }

    @PostMapping("/force-save-external")
    public ResponseEntity<String> forceSaveExternalEvent(@RequestBody SolidaryEvent event) {
        try {
            // Forzar guardado de evento externo (para testing/recuperación)
            EventoSolidario eventoEntity = SolidaryEventMapper.toEntity(event);
            eventoSolidarioRepository.save(eventoEntity);
            return ResponseEntity.ok("Evento externo guardado forzosamente en BD");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }



 //prueba del punto 6
 @PostMapping("/baja")
 public ResponseEntity<String> publishBajaEvento(@RequestBody Map<String, String> body) {
     String organizationId = body.get("organizationId");
     String eventId = body.get("eventId");

     if (organizationId == null || eventId == null) {
         return ResponseEntity.badRequest().body("organizationId y eventId son obligatorios");
     }

     try {
         // 1. Enviar notificación de baja a Kafka
         EventCancellation eventCancellation = new EventCancellation(organizationId, eventId);
         eventCancellationProducer.sendEventCancellation(eventCancellation);
         
         // 2. Eliminar evento de BD local (si es nuestro evento)
         try {
             int eventIdInt = Integer.parseInt(eventId);
             eventoSolidarioRepository.deleteById(eventIdInt);
             System.out.println("Evento eliminado de BD local: " + eventId);
         } catch (NumberFormatException e) {
             System.out.println("EventId no es numérico, no se elimina de BD local: " + eventId);
         } catch (Exception e) {
             System.out.println("Error eliminando evento de BD local: " + e.getMessage());
         }
         
         return ResponseEntity.ok("Evento dado de baja y eliminado de BD local");
     } catch (Exception e) {
         return ResponseEntity.internalServerError().body("Error al enviar evento de baja: " + e.getMessage());
     }
 }

 /**
  * Endpoint para consultar bajas de eventos persistidas
  */
 @GetMapping("/bajas")
 public ResponseEntity<List<EventoBaja>> getBajasEventos() {
     try {
         List<EventoBaja> bajas = eventoBajaRepository.findAll();
         return ResponseEntity.ok(bajas);
     } catch (Exception e) {
         return ResponseEntity.internalServerError().build();
     }
 }

}