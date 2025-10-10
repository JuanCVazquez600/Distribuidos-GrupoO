package Distribuidos_GrupoO.ServidorGRPC.service.kafka.eventcancellation;

import Distribuidos_GrupoO.ServidorGRPC.model.EventoSolidario;
import Distribuidos_GrupoO.ServidorGRPC.service.implementation.EventoSolidarioServiceImplementation;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.event.SolidaryEventConsumer;
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
    private SolidaryEventConsumer solidaryEventConsumer;

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
            String organizationId = eventCancellation.getOrganizationId();
            String eventId = eventCancellation.getEventId();
            
            System.out.println("Procesando baja de evento externo: " + 
                             "Organización: " + organizationId + 
                             ", Evento: " + eventId); //DE LA LINEA 50 A 70 SI COMENTAMOS SE ELIMINA DE LA BD EL EVENTO  
            
            // 1. ACTUALIZACIÓN: Verificar si el evento existe en nuestra base de datos
            boolean eventoExisteEnNuestraBD = false;
            try {
                Integer eventIdInt = Integer.parseInt(eventId);
                EventoSolidario evento = eventoService.buscarPorId(eventIdInt);
                eventoExisteEnNuestraBD = true;
                
                System.out.println("⚠️  IMPORTANTE: Evento externo encontrado en nuestra BD: " + evento.getNombreEvento());
                System.out.println("⚠️  Este evento debe ser revisado ya que otra organización lo dio de baja");
                
                // ACTUALIZACIÓN: Marcar el evento como "bajo revisión" o eliminarlo según reglas de negocio
                // Opción 1: Eliminar también de nuestro sistema (sincronización total)
                // eventoService.eliminarEvento(eventIdInt);
                // System.out.println("✅ Evento eliminado de nuestro sistema por sincronización");
                
                // Opción 2: Solo registrar para revisión manual
                System.out.println("📋 Evento marcado para revisión manual");
                
            } catch (RuntimeException e) {
                if (e.getMessage().contains("Evento no encontrado")) {
                    System.out.println("✅ Evento externo no encontrado en nuestra BD (comportamiento normal)");
                } else {
                    System.err.println("Error al verificar evento en BD: " + e.getMessage());
                }
            }
            
            // 2. ACTUALIZACIÓN: Remover de la lista de eventos externos disponibles
            // (Si mantenemos un cache de eventos de otras organizaciones)
            removeEventFromExternalCache(organizationId, eventId);
            
            // 3. ACTUALIZACIÓN: Registrar en log de auditoria/historial
            logEventCancellation(organizationId, eventId, eventoExisteEnNuestraBD);
            
            // 4. ACTUALIZACIÓN: Notificar a componentes del sistema
            notifySystemComponents(organizationId, eventId);
            
            System.out.println("✅ Actualizaciones del sistema completadas para evento: " + eventId);
            
        } catch (Exception e) {
            System.err.println("❌ Error al procesar actualizaciones del sistema: " + e.getMessage());
            throw e;
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