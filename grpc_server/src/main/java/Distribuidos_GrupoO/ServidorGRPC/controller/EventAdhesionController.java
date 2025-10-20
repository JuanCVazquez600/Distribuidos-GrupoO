package Distribuidos_GrupoO.ServidorGRPC.controller;

import Distribuidos_GrupoO.ServidorGRPC.service.kafka.adhesion.EventAdhesion;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.adhesion.EventAdhesionProducer;
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

    /**
     * Endpoint para que un voluntario se adhiera a un evento externo
     * POST /adhesions/join-direct
     */
    @PostMapping("/join-direct")
    public ResponseEntity<String> joinEventDirect(@RequestBody Map<String, String> request) {
        try {
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
            return ResponseEntity.internalServerError()
                .body("Error al procesar adhesión: " + e.getMessage());
        }
    }
}