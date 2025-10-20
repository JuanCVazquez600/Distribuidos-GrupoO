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
     * Obtener bajas con filtros opcionales
     */
    @GetMapping
    public ResponseEntity<List<EventoBaja>> getEventCancellations(
            @RequestParam(required = false) String organizacionId,
            @RequestParam(required = false) EventoBaja.EstadoBaja estado,
            @RequestParam(required = false, defaultValue = "false") boolean pendingReview,
            @RequestParam(required = false, defaultValue = "false") boolean requiresReview,
            @RequestParam(required = false, defaultValue = "false") boolean recent) {
        
        List<EventoBaja> bajas;
        
        if (pendingReview) {
            bajas = eventoBajaRepository.findBajasPendientesRevision();
        } else if (requiresReview) {
            bajas = eventoBajaRepository.findByExistiaEnBdTrue();
        } else if (recent) {
            bajas = eventoBajaRepository.findUltimasBajas();
            bajas = bajas.size() > 10 ? bajas.subList(0, 10) : bajas;
        } else if (organizacionId != null) {
            bajas = eventoBajaRepository.findByOrganizacionId(organizacionId);
        } else if (estado != null) {
            bajas = eventoBajaRepository.findByEstado(estado);
        } else {
            bajas = eventoBajaRepository.findAll();
        }
        
        return ResponseEntity.ok(bajas);
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
    public ResponseEntity<BajaStats> getStats() {
        try {
            long totalBajas = eventoBajaRepository.count();
            long bajasRevisadas = eventoBajaRepository.findByEstado(EventoBaja.EstadoBaja.REVISADO).size();
            long bajasPendientes = eventoBajaRepository.findBajasPendientesRevision().size();
            long bajasConError = eventoBajaRepository.findByEstado(EventoBaja.EstadoBaja.ERROR).size();
            
            return ResponseEntity.ok(new BajaStats(totalBajas, bajasRevisadas, bajasPendientes, bajasConError));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Clase para estadísticas de bajas
     */
    public static class BajaStats {
        public final long total;
        public final long revisadas;
        public final long pendientes;
        public final long errores;

        public BajaStats(long total, long revisadas, long pendientes, long errores) {
            this.total = total;
            this.revisadas = revisadas;
            this.pendientes = pendientes;
            this.errores = errores;
        }
    }
}