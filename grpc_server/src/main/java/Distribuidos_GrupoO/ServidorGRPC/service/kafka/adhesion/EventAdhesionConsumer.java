package Distribuidos_GrupoO.ServidorGRPC.service.kafka.adhesion;

import Distribuidos_GrupoO.ServidorGRPC.model.EventoSolidario;
import Distribuidos_GrupoO.ServidorGRPC.model.AdhesionEvento;
import Distribuidos_GrupoO.ServidorGRPC.repository.AdhesionEventoRepository;
import Distribuidos_GrupoO.ServidorGRPC.service.implementation.EventoSolidarioServiceImplementation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EventAdhesionConsumer {

    @Value("${app.organization.id:org-456}")
    private String organizationId;

    @Autowired
    private EventoSolidarioServiceImplementation eventoService;

    @Autowired
    private AdhesionEventoRepository adhesionRepository;

    /**
     * Escucha adhesiones dirigidas a nuestra organización
     * Topic: adhesion-evento-org-456 (nuestro org ID)
     */
    @KafkaListener(topics = "adhesion-evento-org-456", groupId = "adhesiones-group", containerFactory = "eventAdhesionKafkaListenerContainerFactory")
    public void listen(EventAdhesion adhesion) {
        try {
            System.out.println("📥 Recibida adhesión de voluntario externo: " + adhesion);
            
            // Procesar la adhesión
            processVolunteerAdhesion(adhesion);
            
        } catch (Exception e) {
            System.err.println("❌ Error al procesar adhesión: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processVolunteerAdhesion(EventAdhesion adhesion) {
        try {
            String eventId = adhesion.getEventId();
            String volunteerOrg = adhesion.getIdOrganizacion();
            String volunteerName = adhesion.getNombre() + " " + adhesion.getApellido();
            
            System.out.println("🔄 Procesando adhesión al evento: " + eventId);
            System.out.println("👤 Voluntario: " + volunteerName + " (" + volunteerOrg + ")");
            
            // 1. VERIFICAR que el evento existe y está activo
            boolean eventoValido = verifyEventExists(eventId);
            
            if (!eventoValido) {
                System.out.println("❌ Evento no encontrado o no válido: " + eventId);
                return;
            }
            
            // 2. REGISTRAR la adhesión en el sistema
            registerVolunteerAdhesion(adhesion);
            
            // 3. ENVIAR confirmación por email (simulado)
            sendConfirmationEmail(adhesion);
            
            // 4. ACTUALIZAR métricas y logs
            updateMetrics(adhesion);
            
            // 5. LOG de auditoria
            logAdhesionAuditoria(adhesion);
            
            System.out.println("✅ Adhesión procesada exitosamente para evento: " + eventId);
            
        } catch (Exception e) {
            System.err.println("❌ Error al procesar adhesión del voluntario: " + e.getMessage());
            throw e;
        }
    }

    private boolean verifyEventExists(String eventId) {
        try {
            Integer eventIdInt = Integer.parseInt(eventId);
            EventoSolidario evento = eventoService.buscarPorId(eventIdInt);
            
            // Verificar que el evento sea futuro
            if (evento.getFechaEvento().isBefore(LocalDateTime.now())) {
                System.out.println("⚠️ Evento ya pasado, no acepta adhesiones: " + eventId);
                return false;
            }
            
            System.out.println("✅ Evento válido encontrado: " + evento.getNombreEvento());
            return true;
            
        } catch (Exception e) {
            System.out.println("❌ Evento no encontrado: " + eventId);
            return false;
        }
    }

    private void registerVolunteerAdhesion(EventAdhesion adhesion) {
        try {
            // ACTUALIZACIÓN: Registrar al voluntario externo en el evento
            System.out.println("📝 Registrando voluntario externo en evento");
            
            // Verificar si ya existe una adhesión
            if (adhesionRepository.findByEventIdAndIdVoluntario(adhesion.getEventId(), adhesion.getIdVoluntario()).isPresent()) {
                System.out.println("⚠️ Adhesión duplicada ignorada para voluntario: " + adhesion.getIdVoluntario());
                return;
            }
            
            // Crear entidad de adhesión
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
            
            // Guardar en BD
            adhesionRepository.save(adhesionEntity);
            
            System.out.println("✅ Adhesión guardada en BD con ID: " + adhesionEntity.getId());
            System.out.println("📋 Voluntario registrado: " + adhesion.getNombre() + " " + adhesion.getApellido());
            System.out.println("📧 Email: " + adhesion.getEmail());
            System.out.println("📞 Teléfono: " + adhesion.getTelefono());
            
        } catch (Exception e) {
            System.err.println("Error al registrar voluntario: " + e.getMessage());
            throw e;
        }
    }

    private void sendConfirmationEmail(EventAdhesion adhesion) {
        try {
            // ACTUALIZACIÓN: Enviar email de confirmación
            System.out.println("📧 Enviando confirmación por email a: " + adhesion.getEmail());
            
            // Aquí integrarías con el EmailService existente
            // emailService.enviarConfirmacionAdhesion(adhesion.getEmail(), adhesion.getEventId());
            
            System.out.println("✅ Email de confirmación enviado");
            
        } catch (Exception e) {
            System.err.println("Error al enviar email: " + e.getMessage());
        }
    }

    private void updateMetrics(EventAdhesion adhesion) {
        try {
            // ACTUALIZACIÓN: Actualizar métricas del sistema
            System.out.println("📊 Actualizando métricas de adhesiones");
            
            // Ejemplos de métricas:
            // - Contador de adhesiones por evento
            // - Voluntarios externos por organización
            // - Tendencias de participación
            
        } catch (Exception e) {
            System.err.println("Error al actualizar métricas: " + e.getMessage());
        }
    }

    private void logAdhesionAuditoria(EventAdhesion adhesion) {
        try {
            // ACTUALIZACIÓN: Log de auditoria
            String timestamp = LocalDateTime.now().toString();
            String logEntry = String.format(
                "[%s] ADHESION_EXTERNA: EventId=%s, Voluntario=%s %s, Org=%s, Email=%s",
                timestamp,
                adhesion.getEventId(),
                adhesion.getNombre(),
                adhesion.getApellido(),
                adhesion.getIdOrganizacion(),
                adhesion.getEmail()
            );
            
            System.out.println("📝 LOG_AUDITORIA: " + logEntry);
            
        } catch (Exception e) {
            System.err.println("Error en log de auditoria: " + e.getMessage());
        }
    }
}