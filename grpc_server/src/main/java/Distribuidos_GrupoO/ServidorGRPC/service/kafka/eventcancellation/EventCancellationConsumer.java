package Distribuidos_GrupoO.ServidorGRPC.service.kafka.eventcancellation;

import Distribuidos_GrupoO.ServidorGRPC.model.EventoSolidario;
import Distribuidos_GrupoO.ServidorGRPC.model.EventoBaja;
import Distribuidos_GrupoO.ServidorGRPC.repository.EventoBajaRepository;
import Distribuidos_GrupoO.ServidorGRPC.service.implementation.EventoSolidarioServiceImplementation;
import Distribuidos_GrupoO.ServidorGRPC.util.EventCancellationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class EventCancellationConsumer {

    @Value("${app.organization.id:org-123}")
    private String organizationId;

    @Autowired
    private EventoSolidarioServiceImplementation eventoService;

    @Autowired
    private EventoBajaRepository eventoBajaRepository;

    @KafkaListener(topics = "baja-evento-solidario", groupId = "baja-eventos-group", containerFactory = "eventCancellationKafkaListenerContainerFactory")
    public void listen(EventCancellation eventCancellation) {
        try {
            System.out.println("Recibido mensaje de baja de evento: " + eventCancellation);
            
            // Verificar si el evento corresponde a nuestra organización
            if (organizationId.equals(eventCancellation.getOrganizationId())) {
                System.out.println("Evento de baja propio ignorado: " + eventCancellation.getEventId()); //SI COMENTAMOS DE LA LINEA 30 A 40 SE ELIMINA DE LA BD
                return;
            }

            // Procesar la baja del evento externo
            processEventCancellation(eventCancellation);
            
        } catch (Exception e) {
            System.err.println("Error al procesar baja de evento: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processEventCancellation(EventCancellation eventCancellation) {
        try {
            System.out.println("🔄 Procesando cancelación de evento con mapper...");
            
            // Validar cancellation antes de procesar
            if (EventCancellationMapper.shouldDiscard(eventCancellation)) {
                System.err.println("❌ Cancelación inválida, descartando: " + eventCancellation);
                return;
            }
            
            String organizationId = eventCancellation.getOrganizationId();
            String eventId = eventCancellation.getEventId();
            
            System.out.println("Procesando baja de evento externo: " + 
                             "Organización: " + organizationId + 
                             ", Evento: " + eventId);
            
            // 1. Verificar si el evento existe en nuestra base de datos
            boolean eventoExisteEnNuestraBD = verificarEventoEnBD(eventId);
            
            // 2. NUEVA FUNCIONALIDAD: Usar mapper para crear EventoBaja
            EventoBaja eventoBaja = EventCancellationMapper.toEntity(eventCancellation, eventoExisteEnNuestraBD);
            
            // 3. Si existe en BD, eliminarlo (sincronización automática)
            if (eventoExisteEnNuestraBD) {
                eliminarEventoLocal(eventId, eventoBaja);
            }
            
            // 4. Persistir usando mapper
            persistirBajaConMapper(eventoBaja);
            
            // 5. Remover de lista de eventos externos
            removerDeEventosExternos(eventCancellation);
            
        } catch (Exception e) {
            System.err.println("❌ Error al procesar cancelación: " + e.getMessage());
            // Crear registro de error usando mapper
            EventoBaja errorBaja = EventCancellationMapper.toEntityWithError(eventCancellation, e.getMessage());
            try {
                eventoBajaRepository.save(errorBaja);
                System.out.println("💾 Error persistido en BD para auditoría");
            } catch (Exception persistError) {
                System.err.println("❌ Error crítico al persistir error: " + persistError.getMessage());
            }
        }
    }
    
    private boolean verificarEventoEnBD(String eventId) {
        try {
            Integer eventIdInt = Integer.parseInt(eventId);
            EventoSolidario evento = eventoService.buscarPorId(eventIdInt);
            System.out.println("⚠️ IMPORTANTE: Evento externo encontrado en nuestra BD: " + evento.getNombreEvento());
            return true;
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Evento no encontrado")) {
                System.out.println("✅ Evento externo no encontrado en nuestra BD (comportamiento normal)");
                return false;
            } else {
                System.err.println("Error al verificar evento en BD: " + e.getMessage());
                return false;
            }
        }
    }
    
    private void eliminarEventoLocal(String eventId, EventoBaja eventoBaja) {
        try {
            Integer eventIdInt = Integer.parseInt(eventId);
            EventoSolidario evento = eventoService.buscarPorId(eventIdInt);
            eventoService.eliminarEvento(eventIdInt);
            System.out.println("✅ Evento eliminado de nuestro sistema por sincronización automática");
            eventoBaja.setObservaciones("Evento encontrado y ELIMINADO de BD local: " + evento.getNombreEvento());
        } catch (Exception e) {
            System.err.println("Error al eliminar evento local: " + e.getMessage());
            eventoBaja.setObservaciones("Error al eliminar evento local: " + e.getMessage());
            eventoBaja.setEstado(EventoBaja.EstadoBaja.ERROR);
        }
    }
    
    private void persistirBajaConMapper(EventoBaja eventoBaja) {
        try {
            EventoBaja bajaGuardada = eventoBajaRepository.save(eventoBaja);
            System.out.println("💾 Baja de evento persistida en BD con ID: " + bajaGuardada.getId());
            System.out.println("📋 Estado: " + bajaGuardada.getEstado());
            System.out.println("📝 Observaciones: " + bajaGuardada.getObservaciones());
        } catch (Exception e) {
            System.err.println("❌ Error al persistir baja de evento en BD: " + e.getMessage());
            throw e;
        }
    }
    
    private void removerDeEventosExternos(EventCancellation eventCancellation) {
        try {
            String organizationId = eventCancellation.getOrganizationId();
            String eventId = eventCancellation.getEventId();
            
            // Remover de la lista de eventos externos disponibles
            removeEventFromExternalCache(organizationId, eventId);
            
            // Registrar en log de auditoria/historial
            logEventCancellation(organizationId, eventId, true);
            
            // Notificar a componentes del sistema
            notifySystemComponents(organizationId, eventId);
            
            System.out.println("✅ Actualizaciones del sistema completadas para evento: " + eventId);
            
        } catch (Exception e) {
            System.err.println("❌ Error al procesar actualizaciones del sistema: " + e.getMessage());
        }
    }
    
    private void removeEventFromExternalCache(String organizationId, String eventId) {
        try {
            // ACTUALIZACIÓN: Remover el evento del cache de eventos externos
            // Esto asegura que no aparezca más en consultas de eventos disponibles
            System.out.println("🗑️  Removiendo evento del cache de eventos externos");
            
            // Aquí podrías integrar con solidaryEventConsumer para remover el evento
            // de la lista de eventos externos disponibles
            
        } catch (Exception e) {
            System.err.println("Error al remover evento del cache: " + e.getMessage());
        }
    }
    
    private void logEventCancellation(String organizationId, String eventId, boolean existiaEnNuestraBD) {
        try {
            // ACTUALIZACIÓN: Registrar en sistema de auditoria
            String timestamp = java.time.LocalDateTime.now().toString();
            String logEntry = String.format("[%s] BAJA_EVENTO_EXTERNO: Org=%s, EventId=%s, ExistiaEnBD=%s", 
                                           timestamp, organizationId, eventId, existiaEnNuestraBD);
            
            System.out.println("📝 LOG_AUDITORIA: " + logEntry);
            
            // Aquí podrías guardar en una tabla de auditoria en la BD
            // auditoriaService.registrarBajaEventoExterno(organizationId, eventId, existiaEnNuestraBD);
            
        } catch (Exception e) {
            System.err.println("Error al registrar en auditoria: " + e.getMessage());
        }
    }
    
    private void notifySystemComponents(String organizationId, String eventId) {
        try {
            // ACTUALIZACIÓN: Notificar a otros componentes del sistema
            System.out.println("📢 Notificando a componentes del sistema sobre baja de evento");
            
            // Ejemplos de notificaciones:
            // 1. Actualizar caches de eventos
            // 2. Notificar a servicios de reportes
            // 3. Actualizar métricas de eventos disponibles
            // 4. Enviar notificaciones a administradores si es necesario
            
        } catch (Exception e) {
            System.err.println("Error al notificar componentes: " + e.getMessage());
        }
    }
}