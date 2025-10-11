package Distribuidos_GrupoO.ServidorGRPC.controller;

import Distribuidos_GrupoO.ServidorGRPC.model.EventoBaja;
import Distribuidos_GrupoO.ServidorGRPC.repository.EventoBajaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/event-cancellations")
public class EventBajaController {

    @Autowired
    private EventoBajaRepository eventoBajaRepository;

    /**
     * Endpoint de prueba
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("EventBaja Controller funcionando correctamente");
    }

    /**
     * Obtener todas las bajas de eventos
     */
    @GetMapping
    public ResponseEntity<List<EventoBaja>> getAllEventCancellations() {
        List<EventoBaja> bajas = eventoBajaRepository.findAll();
        return ResponseEntity.ok(bajas);
    }

    /**
     * Obtener bajas por organización
     */
    @GetMapping("/organization/{organizacionId}")
    public ResponseEntity<List<EventoBaja>> getByOrganization(@PathVariable String organizacionId) {
        List<EventoBaja> bajas = eventoBajaRepository.findByOrganizacionId(organizacionId);
        return ResponseEntity.ok(bajas);
    }

    /**
     * Obtener bajas por estado
     */
    @GetMapping("/status/{estado}")
    public ResponseEntity<List<EventoBaja>> getByStatus(@PathVariable EventoBaja.EstadoBaja estado) {
        List<EventoBaja> bajas = eventoBajaRepository.findByEstado(estado);
        return ResponseEntity.ok(bajas);
    }

    /**
     * Obtener bajas que existían en nuestra BD (requieren revisión)
     */
    @GetMapping("/requiring-review")
    public ResponseEntity<List<EventoBaja>> getBajasRequiringReview() {
        List<EventoBaja> bajas = eventoBajaRepository.findByExistiaEnBdTrue();
        return ResponseEntity.ok(bajas);
    }

    /**
     * Obtener bajas pendientes de revisión
     */
    @GetMapping("/pending-review")
    public ResponseEntity<List<EventoBaja>> getPendingReview() {
        List<EventoBaja> bajas = eventoBajaRepository.findBajasPendientesRevision();
        return ResponseEntity.ok(bajas);
    }

    /**
     * Obtener últimas bajas (para dashboard)
     */
    @GetMapping("/recent")
    public ResponseEntity<List<EventoBaja>> getRecentCancellations() {
        List<EventoBaja> bajas = eventoBajaRepository.findUltimasBajas();
        return ResponseEntity.ok(bajas.size() > 10 ? bajas.subList(0, 10) : bajas);
    }

    /**
     * Marcar una baja como revisada
     */
    @PutMapping("/{id}/mark-reviewed")
    public ResponseEntity<String> markAsReviewed(@PathVariable Integer id) {
        try {
            EventoBaja baja = eventoBajaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Baja no encontrada"));
            
            baja.setEstado(EventoBaja.EstadoBaja.REVISADO);
            eventoBajaRepository.save(baja);
            
            return ResponseEntity.ok("Baja marcada como revisada correctamente");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Error al marcar como revisada: " + e.getMessage());
        }
    }

    /**
     * Obtener estadísticas de bajas
     */
    @GetMapping("/stats")
    public ResponseEntity<Object> getStats() {
        try {
            long totalBajas = eventoBajaRepository.count();
            long bajasRevisadas = eventoBajaRepository.findByEstado(EventoBaja.EstadoBaja.REVISADO).size();
            long bajasPendientes = eventoBajaRepository.findBajasPendientesRevision().size();
            long bajasConError = eventoBajaRepository.findByEstado(EventoBaja.EstadoBaja.ERROR).size();
            
            return ResponseEntity.ok(new Object() {
                public final long total = totalBajas;
                public final long revisadas = bajasRevisadas;
                public final long pendientes = bajasPendientes;
                public final long errores = bajasConError;
            });
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Error al obtener estadísticas: " + e.getMessage());
        }
    }
}