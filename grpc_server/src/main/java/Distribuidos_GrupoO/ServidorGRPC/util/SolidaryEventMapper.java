package Distribuidos_GrupoO.ServidorGRPC.util;

import Distribuidos_GrupoO.ServidorGRPC.model.EventoSolidario;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.event.SolidaryEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Mapper para convertir entre SolidaryEvent (Kafka DTO) y EventoSolidario (Entity)
 * Siguiendo el patrón de DonationOfferMapper
 */
public class SolidaryEventMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Convierte SolidaryEvent (DTO de Kafka) a EventoSolidario (Entity JPA)
     * Para persistir eventos externos recibidos por Kafka
     */
    public static EventoSolidario toEntity(SolidaryEvent solidaryEvent) {
        if (solidaryEvent == null) {
            return null;
        }

        EventoSolidario evento = new EventoSolidario();
        evento.setNombreEvento(solidaryEvent.getEventName());
        evento.setDescripcionEvento(solidaryEvent.getDescription());
        evento.setFechaEvento(solidaryEvent.getDateTime());
        
        // Nota: miembros se inicializa como set vacío (eventos externos no tienen nuestros miembros)
        evento.setMiembros(new HashSet<>());
        
        return evento;
    }

    /**
     * Convierte EventoSolidario (Entity JPA) a SolidaryEvent (DTO de Kafka)
     * Para publicar nuestros eventos en Kafka
     */
    public static SolidaryEvent fromEntity(EventoSolidario evento, String organizationId) {
        if (evento == null) {
            return null;
        }

        return new SolidaryEvent(
            organizationId,
            String.valueOf(evento.getId()),
            evento.getNombreEvento(),
            evento.getDescripcionEvento(),
            evento.getFechaEvento()
        );
    }

    /**
     * Convierte lista de EventoSolidario a lista de SolidaryEvent
     * Para publicar múltiples eventos
     */
    public static List<SolidaryEvent> fromEntityList(List<EventoSolidario> eventos, String organizationId) {
        List<SolidaryEvent> solidaryEvents = new ArrayList<>();
        if (eventos != null) {
            for (EventoSolidario evento : eventos) {
                SolidaryEvent solidaryEvent = fromEntity(evento, organizationId);
                if (solidaryEvent != null) {
                    solidaryEvents.add(solidaryEvent);
                }
            }
        }
        return solidaryEvents;
    }

    /**
     * Crea SolidaryEvent desde datos raw del POST request
     * Para cuando se publica via REST API
     */
    public static SolidaryEvent fromPostRequest(String organizationId, String eventId, 
                                               String eventName, String description, String dateTime) {
        try {
            LocalDateTime parsedDateTime = LocalDateTime.parse(dateTime, FORMATTER);
            return new SolidaryEvent(organizationId, eventId, eventName, description, parsedDateTime);
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de fecha inválido. Use: YYYY-MM-DDTHH:MM:SS", e);
        }
    }

    /**
     * Valida si un SolidaryEvent es válido para persistir
     */
    public static boolean isValidForPersistence(SolidaryEvent event) {
        return event != null &&
               event.getOrganizationId() != null && !event.getOrganizationId().trim().isEmpty() &&
               event.getEventId() != null && !event.getEventId().trim().isEmpty() &&
               event.getEventName() != null && !event.getEventName().trim().isEmpty() &&
               event.getDateTime() != null &&
               event.getDateTime().isAfter(LocalDateTime.now()); // Solo eventos futuros
    }

    /**
     * Valida si un evento debe ser descartado (es propio de la organización)
     */
    public static boolean shouldDiscard(SolidaryEvent event, String currentOrganizationId) {
        return event.getOrganizationId().equals(currentOrganizationId);
    }
}