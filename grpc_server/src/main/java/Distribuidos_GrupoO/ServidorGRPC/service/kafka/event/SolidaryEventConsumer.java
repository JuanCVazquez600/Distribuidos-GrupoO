package Distribuidos_GrupoO.ServidorGRPC.service.kafka.event;

import Distribuidos_GrupoO.ServidorGRPC.model.EventoSolidario;
import Distribuidos_GrupoO.ServidorGRPC.repository.EventoSolidarioRepository;
import Distribuidos_GrupoO.ServidorGRPC.util.SolidaryEventMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SolidaryEventConsumer {
    
    // ID de nuestra organización (configurable)
    @Value("${app.organization.id:org-local}")
    private String ourOrganizationId;
    
    @Autowired
    private EventoSolidarioRepository eventoSolidarioRepository;
    
    // Lista de eventos externos (de otras organizaciones) - en memoria para consulta rápida
    private final List<SolidaryEvent> externalEvents = new ArrayList<>();

    @KafkaListener(topics = "eventos-solidarios", groupId = "eventos-group", containerFactory = "solidaryEventKafkaListenerContainerFactory")
    public void listen(SolidaryEvent event) {
        processEvent(event);
    }

    private void processEvent(SolidaryEvent event) {
        try {
            // 1. Descartar eventos propios
            if (isOwnEvent(event)) {
                System.out.println("Evento propio descartado: " + event.getEventId());
                return;
            }

            // 2. Verificar que el evento esté vigente (fecha futura)
            if (!isEventValid(event)) {
                System.out.println("Evento no vigente descartado: " + event.getEventId() + 
                                 " - Fecha: " + event.getDateTime());
                return;
            }

            // 3. Validar usando el mapper
            if (!SolidaryEventMapper.isValidForPersistence(event)) {
                System.out.println("Evento no válido según mapper: " + event.getEventId());
                return;
            }

            // 4. Verificar que no sea un evento que debe descartarse
            if (SolidaryEventMapper.shouldDiscard(event, ourOrganizationId)) {
                System.out.println("Evento descartado por organización: " + event.getEventId());
                return;
            }

            // 5. Guardar evento en base de datos usando el mapper
            EventoSolidario eventoEntity = SolidaryEventMapper.toEntity(event);
            
            // Verificar si ya existe en la BD para evitar duplicados (buscar por nombre y fecha)
            List<EventoSolidario> existingEvents = eventoSolidarioRepository.findAll().stream()
                    .filter(e -> e.getNombreEvento().equals(eventoEntity.getNombreEvento()) &&
                               e.getFechaEvento().equals(eventoEntity.getFechaEvento()))
                    .toList();
            
            if (existingEvents.isEmpty()) {
                eventoSolidarioRepository.save(eventoEntity);
                System.out.println("Evento externo persistido en BD: " + 
                                 "Organización: " + event.getOrganizationId() + 
                                 ", Evento: " + event.getEventName() + 
                                 ", Fecha: " + event.getDateTime());
            } else {
                System.out.println("Evento ya existe en BD: " + event.getEventName());
            }

            // 6. También mantener en memoria para consulta rápida
            synchronized (externalEvents) {
                // Evitar duplicados en memoria
                if (externalEvents.stream().noneMatch(e -> 
                    e.getOrganizationId().equals(event.getOrganizationId()) && 
                    e.getEventId().equals(event.getEventId()))) {
                    
                    externalEvents.add(event);
                    System.out.println("Evento externo agregado a memoria: " + 
                                     "Organización: " + event.getOrganizationId() + 
                                     ", Evento: " + event.getEventName() + 
                                     ", Fecha: " + event.getDateTime());
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error procesando evento: " + event.getEventId() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isOwnEvent(SolidaryEvent event) {
        return ourOrganizationId.equals(event.getOrganizationId());
    }

    private boolean isEventValid(SolidaryEvent event) {
        // El evento debe ser en el futuro para considerarse vigente
        return event.getDateTime() != null && 
               event.getDateTime().isAfter(LocalDateTime.now());
    }

    public List<SolidaryEvent> getExternalEvents() {
        synchronized (externalEvents) {
            // Filtrar eventos que siguen siendo vigentes al momento de la consulta
            return externalEvents.stream()
                    .filter(this::isEventValid)
                    .toList();
        }
    }

    public List<SolidaryEvent> getAllExternalEvents() {
        synchronized (externalEvents) {
            return new ArrayList<>(externalEvents);
        }
    }

    /**
     * Obtiene el organizador de un evento específico
     * @param eventId ID del evento
     * @return ID de la organización organizadora, o null si no se encuentra
     */
    public String getEventOrganizer(String eventId) {
        synchronized (externalEvents) {
            return externalEvents.stream()
                    .filter(event -> eventId.equals(event.getEventId()))
                    .map(SolidaryEvent::getOrganizationId)
                    .findFirst()
                    .orElse(null);
        }
    }

    /**
     * Obtiene eventos externos persistidos desde la base de datos
     * Útil para obtener eventos que han sido guardados a través de Kafka
     * @return Lista de eventos solidarios vigentes desde BD
     */
    public List<EventoSolidario> getPersistedExternalEvents() {
        // Filtrar solo eventos futuros
        return eventoSolidarioRepository.findByFechaEventoAfter(LocalDateTime.now());
    }

    /**
     * Convierte eventos persistidos a SolidaryEvents para compatibilidad
     * @return Lista de eventos externos como SolidaryEvent desde BD
     */
    public List<SolidaryEvent> getPersistedExternalEventsAsSolidaryEvents() {
        List<EventoSolidario> eventosFromDB = getPersistedExternalEvents();
        return SolidaryEventMapper.fromEntityList(eventosFromDB, "external");
    }
}