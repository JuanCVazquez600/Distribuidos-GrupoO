package Distribuidos_GrupoO.ServidorGRPC.util;

import Distribuidos_GrupoO.ServidorGRPC.model.AdhesionEvento;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.adhesion.EventAdhesion;
import java.time.LocalDateTime;

/**
 * Mapper para convertir entre EventAdhesion (Kafka DTO) y AdhesionEvento (Entity)
 * Siguiendo el patrón de EventCancellationMapper y SolidaryEventMapper
 */
public class EventAdhesionMapper {

    /**
     * Convierte EventAdhesion (DTO de Kafka) a AdhesionEvento (Entity JPA)
     * Para persistir adhesiones recibidas por Kafka
     */
    public static AdhesionEvento toEntity(EventAdhesion adhesion) {
        if (adhesion == null) {
            return null;
        }

        AdhesionEvento adhesionEntity = new AdhesionEvento();
        adhesionEntity.setEventId(adhesion.getEventId());
        adhesionEntity.setIdOrganizacionVoluntario(adhesion.getIdOrganizacion());
        adhesionEntity.setIdVoluntario(adhesion.getIdVoluntario());
        adhesionEntity.setNombre(adhesion.getNombre());
        adhesionEntity.setApellido(adhesion.getApellido());
        adhesionEntity.setTelefono(adhesion.getTelefono());
        adhesionEntity.setEmail(adhesion.getEmail());
        adhesionEntity.setEstado(AdhesionEvento.EstadoAdhesion.CONFIRMADA);
        adhesionEntity.setFechaProcesamiento(LocalDateTime.now());
        adhesionEntity.setObservaciones("Adhesión procesada automáticamente");
        
        return adhesionEntity;
    }

    /**
     * Convierte AdhesionEvento (Entity JPA) a EventAdhesion (DTO de Kafka)
     * Para republicar adhesiones si es necesario
     */
    public static EventAdhesion fromEntity(AdhesionEvento adhesionEntity) {
        if (adhesionEntity == null) {
            return null;
        }

        return new EventAdhesion(
            adhesionEntity.getEventId(),
            adhesionEntity.getIdOrganizacionVoluntario(),
            adhesionEntity.getIdVoluntario(),
            adhesionEntity.getNombre(),
            adhesionEntity.getApellido(),
            adhesionEntity.getTelefono(),
            adhesionEntity.getEmail()
        );
    }

    /**
     * Valida si un EventAdhesion es válido para persistencia
     */
    public static boolean isValidForPersistence(EventAdhesion adhesion) {
        return adhesion != null 
            && adhesion.getEventId() != null && !adhesion.getEventId().trim().isEmpty()
            && adhesion.getIdOrganizacion() != null && !adhesion.getIdOrganizacion().trim().isEmpty()
            && adhesion.getIdVoluntario() != null && !adhesion.getIdVoluntario().trim().isEmpty()
            && adhesion.getNombre() != null && !adhesion.getNombre().trim().isEmpty()
            && adhesion.getApellido() != null && !adhesion.getApellido().trim().isEmpty()
            && adhesion.getEmail() != null && !adhesion.getEmail().trim().isEmpty();
    }

    /**
     * Determina si una adhesión debe ser descartada
     */
    public static boolean shouldDiscard(EventAdhesion adhesion) {
        return !isValidForPersistence(adhesion);
    }

    /**
     * Crea AdhesionEvento con estado de error para adhesiones fallidas
     */
    public static AdhesionEvento toEntityWithError(EventAdhesion adhesion, String errorMessage) {
        if (adhesion == null) {
            return null;
        }

        AdhesionEvento adhesionEntity = new AdhesionEvento();
        adhesionEntity.setEventId(adhesion.getEventId() != null ? adhesion.getEventId() : "UNKNOWN");
        adhesionEntity.setIdOrganizacionVoluntario(adhesion.getIdOrganizacion() != null ? 
                                                   adhesion.getIdOrganizacion() : "UNKNOWN");
        adhesionEntity.setIdVoluntario(adhesion.getIdVoluntario() != null ? 
                                       adhesion.getIdVoluntario() : "UNKNOWN");
        adhesionEntity.setNombre(adhesion.getNombre() != null ? adhesion.getNombre() : "ERROR");
        adhesionEntity.setApellido(adhesion.getApellido() != null ? adhesion.getApellido() : "ERROR");
        adhesionEntity.setTelefono(adhesion.getTelefono());
        adhesionEntity.setEmail(adhesion.getEmail() != null ? adhesion.getEmail() : "error@unknown.com");
        adhesionEntity.setEstado(AdhesionEvento.EstadoAdhesion.RECHAZADA);
        adhesionEntity.setFechaProcesamiento(LocalDateTime.now());
        adhesionEntity.setObservaciones("Error al procesar: " + errorMessage);
        
        return adhesionEntity;
    }

    /**
     * Verifica si ya existe una adhesión duplicada basada en los campos clave
     */
    public static boolean isDuplicate(EventAdhesion adhesion, AdhesionEvento existing) {
        if (adhesion == null || existing == null) {
            return false;
        }
        
        return adhesion.getEventId().equals(existing.getEventId()) &&
               adhesion.getIdOrganizacion().equals(existing.getIdOrganizacionVoluntario()) &&
               adhesion.getIdVoluntario().equals(existing.getIdVoluntario());
    }
}