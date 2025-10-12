package Distribuidos_GrupoO.ServidorGRPC.controller;

import Distribuidos_GrupoO.ServidorGRPC.service.kafka.adhesion.EventAdhesion;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.adhesion.EventAdhesionProducer;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.event.SolidaryEventConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/adhesions")
public class EventAdhesionController {

    @Value("${app.organization.id:org-456}")
    private String organizationId;

    @Autowired
    private EventAdhesionProducer adhesionProducer;

    @Autowired
    private SolidaryEventConsumer solidaryEventConsumer;

    /**
     * Endpoint para que un voluntario se adhiera a un evento externo
     * POST /adhesions/join/{eventId}
     */
    @PostMapping("/join/{eventId}")
    public ResponseEntity<String> joinEvent(
            @PathVariable String eventId,
            @RequestBody VolunteerInfo volunteerInfo) {
        
        try {
            // 1. Determinar quién organiza el evento
            String organizadorId = determineEventOrganizer(eventId);
            
            if (organizadorId == null) {
                return ResponseEntity.badRequest()
                    .body("No se pudo determinar el organizador del evento: " + eventId);
            }
            
            if (organizationId.equals(organizadorId)) {
                return ResponseEntity.badRequest()
                    .body("No puedes adherirte a tu propio evento");
            }

            // 2. Crear mensaje de adhesión
            EventAdhesion adhesion = new EventAdhesion(
                eventId,
                organizationId, // Nuestra organización
                volunteerInfo.getIdVoluntario(),
                volunteerInfo.getNombre(),
                volunteerInfo.getApellido(),
                volunteerInfo.getTelefono(),
                volunteerInfo.getEmail()
            );

            // 3. Enviar al topic del organizador
            adhesionProducer.sendAdhesion(organizadorId, adhesion);

            return ResponseEntity.ok(
                "Adhesión enviada correctamente al organizador: " + organizadorId
            );

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Error al procesar adhesión: " + e.getMessage());
        }
    }

    /**
     * Endpoint simplificado para testing con Postman
     * POST /adhesions/join-direct
     */
    @PostMapping("/join-direct")
    public ResponseEntity<String> joinEventDirect(@RequestBody Map<String, String> request) {
        try {
            System.out.println("=== DEBUG: Request recibido ===");
            System.out.println("Request body: " + request);
            
            String eventId = request.get("eventId");
            String organizadorId = request.get("organizadorId");
            String nombre = request.get("nombre");
            String apellido = request.get("apellido");
            String telefono = request.get("telefono");
            String email = request.get("email");
            String idVoluntario = request.get("idVoluntario");

            // Validaciones básicas
            if (eventId == null || organizadorId == null || nombre == null || 
                apellido == null || email == null || idVoluntario == null) {
                return ResponseEntity.badRequest()
                    .body("Campos obligatorios: eventId, organizadorId, nombre, apellido, email, idVoluntario");
            }

            if (organizationId.equals(organizadorId)) {
                return ResponseEntity.badRequest()
                    .body("No puedes adherirte a tu propio evento");
            }

            // Crear adhesión
            EventAdhesion adhesion = new EventAdhesion(
                eventId,
                organizationId,
                idVoluntario,
                nombre,
                apellido,
                telefono != null ? telefono : "",
                email
            );

            // Enviar
            adhesionProducer.sendAdhesion(organizadorId, adhesion);

            return ResponseEntity.ok(
                "Adhesión enviada correctamente al organizador: " + organizadorId
            );

        } catch (Exception e) {
            System.out.println("=== ERROR ===");
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body("Error al procesar adhesión: " + e.getMessage());
        }
    }

    /**
     * Determina qué organización organiza un evento específico
     */
    private String determineEventOrganizer(String eventId) {
        try {
            // Buscar en eventos externos conocidos
            return solidaryEventConsumer.getEventOrganizer(eventId);
                
        } catch (Exception e) {
            System.err.println("Error al determinar organizador del evento: " + e.getMessage());
            return null;
        }
    }

    /**
     * DTO para información del voluntario
     */
    public static class VolunteerInfo {
        private String idVoluntario;
        private String nombre;
        private String apellido;
        private String telefono;
        private String email;

        // Constructores
        public VolunteerInfo() {}

        public VolunteerInfo(String idVoluntario, String nombre, String apellido, String telefono, String email) {
            this.idVoluntario = idVoluntario;
            this.nombre = nombre;
            this.apellido = apellido;
            this.telefono = telefono;
            this.email = email;
        }

        // Getters y Setters
        public String getIdVoluntario() { return idVoluntario; }
        public void setIdVoluntario(String idVoluntario) { this.idVoluntario = idVoluntario; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getApellido() { return apellido; }
        public void setApellido(String apellido) { this.apellido = apellido; }

        public String getTelefono() { return telefono; }
        public void setTelefono(String telefono) { this.telefono = telefono; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}