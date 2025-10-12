package Distribuidos_GrupoO.ServidorGRPC.util;

import Distribuidos_GrupoO.ServidorGRPC.model.EventoBaja;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.eventcancellation.EventCancellation;
import java.time.LocalDateTime;

/**
 * Mapper para convertir entre EventCancellation (Kafka DTO) y EventoBaja (Entity)
 * Siguiendo el patrón de SolidaryEventMapper y DonationOfferMapper
 */
public class EventCancellationMapper {

    /**
     * Convierte EventCancellation (DTO de Kafka) a EventoBaja (Entity JPA)
     * Para persistir cancelaciones recibidas por Kafka
     */
    public static EventoBaja toEntity(EventCancellation cancellation, boolean existiaEnBd) {
        if (cancellation == null) {
            return null;
        }

        EventoBaja eventoBaja = new EventoBaja();
        eventoBaja.setOrganizacionId(cancellation.getOrganizationId());
        eventoBaja.setEventoId(cancellation.getEventId());
        eventoBaja.setFechaBaja(LocalDateTime.now());
        eventoBaja.setExistiaEnBd(existiaEnBd);
        eventoBaja.setEstado(EventoBaja.EstadoBaja.PROCESADO);
        eventoBaja.setFechaProcesamiento(LocalDateTime.now());
        eventoBaja.setObservaciones("Cancelación procesada automáticamente");
        
        return eventoBaja;
    }

    /**
     * Convierte EventoBaja (Entity JPA) a EventCancellation (DTO de Kafka)
     * Para republicar cancelaciones si es necesario
     */
    public static EventCancellation fromEntity(EventoBaja eventoBaja) {
        if (eventoBaja == null) {
            return null;
        }

        return new EventCancellation(
            eventoBaja.getOrganizacionId(),
            eventoBaja.getEventoId()
        );
    }

    /**
     * Valida si un EventCancellation es válido para persistencia
     */
    public static boolean isValidForPersistence(EventCancellation cancellation) {
        return cancellation != null 
            && cancellation.getOrganizationId() != null && !cancellation.getOrganizationId().trim().isEmpty()
            && cancellation.getEventId() != null && !cancellation.getEventId().trim().isEmpty();
    }

    /**
     * Determina si una cancelación debe ser descartada
     */
    public static boolean shouldDiscard(EventCancellation cancellation) {
        return !isValidForPersistence(cancellation);
    }

    /**
     * Crea EventoBaja con estado de error para cancelaciones fallidas
     */
    public static EventoBaja toEntityWithError(EventCancellation cancellation, String errorMessage) {
        if (cancellation == null) {
            return null;
        }

        EventoBaja eventoBaja = new EventoBaja();
        eventoBaja.setOrganizacionId(cancellation.getOrganizationId());
        eventoBaja.setEventoId(cancellation.getEventId());
        eventoBaja.setFechaBaja(LocalDateTime.now());
        eventoBaja.setExistiaEnBd(false);
        eventoBaja.setEstado(EventoBaja.EstadoBaja.ERROR);
        eventoBaja.setFechaProcesamiento(LocalDateTime.now());
        eventoBaja.setObservaciones("Error al procesar: " + errorMessage);
        
        return eventoBaja;
    }
}