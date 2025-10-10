package Distribuidos_GrupoO.ServidorGRPC.controller;

import Distribuidos_GrupoO.ServidorGRPC.service.kafka.event.SolidaryEvent;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.event.SolidaryEventConsumer;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.event.SolidaryEventProducer;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.eventcancellation.EventCancellation;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.eventcancellation.EventCancellationProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/events")
public class SolidaryEventController {

    @Autowired
    private SolidaryEventProducer eventProducer;

    @Autowired
    private SolidaryEventConsumer eventConsumer;

    @Autowired
    private EventCancellationProducer eventCancellationProducer;

    @PostMapping("/publish")
    public ResponseEntity<String> publishEvent(@RequestBody SolidaryEvent event) {
        eventProducer.publishEvent(event);
        return ResponseEntity.ok("Evento solidario publicado correctamente");
    }

    @GetMapping("/external")
    public ResponseEntity<List<SolidaryEvent>> getExternalEvents() {
        List<SolidaryEvent> externalEvents = eventConsumer.getExternalEvents();
        return ResponseEntity.ok(externalEvents);
    }

    @GetMapping("/external/all")
    public ResponseEntity<List<SolidaryEvent>> getAllExternalEvents() {
        List<SolidaryEvent> allExternalEvents = eventConsumer.getAllExternalEvents();
        return ResponseEntity.ok(allExternalEvents);
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
         EventCancellation eventCancellation = new EventCancellation(organizationId, eventId);
         eventCancellationProducer.sendEventCancellation(eventCancellation);
         return ResponseEntity.ok("Evento dado de baja enviado al topic correctamente");
     } catch (Exception e) {
         return ResponseEntity.internalServerError().body("Error al enviar evento de baja: " + e.getMessage());
     }
 }



}