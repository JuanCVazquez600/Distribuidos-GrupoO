package Distribuidos_GrupoO.ServidorGRPC.controller;

import Distribuidos_GrupoO.ServidorGRPC.service.kafka.event.SolidaryEvent;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.event.SolidaryEventConsumer;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.event.SolidaryEventProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
public class SolidaryEventController {

    @Autowired
    private SolidaryEventProducer eventProducer;

    @Autowired
    private SolidaryEventConsumer eventConsumer;

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
/*
    @PostMapping("/baja")
   public ResponseEntity<String> getDeletedEvents(@RequestBody SolidaryEvent event) {
        // BajaEventosRequest puede contener organizationId, from, to, page, size
        eventProducer.deleteEvent(event);
        return ResponseEntity.ok("Eventos que se eliminaron");
    }

*/



}