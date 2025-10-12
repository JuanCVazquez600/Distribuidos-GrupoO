package Distribuidos_GrupoO.ServidorGRPC.service.kafka.adhesion;

import Distribuidos_GrupoO.ServidorGRPC.model.AdhesionEvento;
import Distribuidos_GrupoO.ServidorGRPC.repository.AdhesionEventoRepository;
import Distribuidos_GrupoO.ServidorGRPC.service.implementation.EventoSolidarioServiceImplementation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class EventAdhesionConsumer {

    @Autowired
    private AdhesionEventoRepository adhesionEventoRepository;

    @Autowired
    private EventoSolidarioServiceImplementation eventoService;

    @Value("${app.organizacion.id:org-456}")
    private String organizationId;

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
            System.out.println("🔄 Procesando adhesión para eventId: " + adhesion.getEventId());
            
            // Verificar que el evento existe
            if (!doesEventExist(adhesion.getEventId())) {
                System.err.println("❌ Evento no encontrado: " + adhesion.getEventId());
                return;
            }
            
            // Registrar la adhesión directamente en la tabla adhesion_evento
            registerVolunteerAdhesion(adhesion);
            
        } catch (Exception e) {
            System.err.println("❌ Error al procesar adhesión: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean doesEventExist(String eventId) {
        try {
            Integer eventIdInt = Integer.parseInt(eventId);
            return eventoService.buscarPorId(eventIdInt) != null;
        } catch (Exception e) {
            System.out.println("❌ Evento no encontrado: " + eventId);
            return false;
        }
    }

    private void registerVolunteerAdhesion(EventAdhesion adhesion) {
        try {
            System.out.println("🔄 Registrando adhesión en tabla adhesion_evento");
            
            // Verificar si ya existe una adhesión para este evento y voluntario
            Optional<AdhesionEvento> existingAdhesion = adhesionEventoRepository.findByEventIdAndIdVoluntario(
                adhesion.getEventId(), generateVolunteerId(adhesion)
            );
            
            if (existingAdhesion.isPresent()) {
                System.out.println("⚠️ Ya existe una adhesión para este evento y voluntario");
                return;
            }
            
            // Crear nueva adhesión
            AdhesionEvento nuevaAdhesion = new AdhesionEvento();
            nuevaAdhesion.setEventId(adhesion.getEventId());
            nuevaAdhesion.setIdOrganizacionVoluntario(adhesion.getIdOrganizacion());
            nuevaAdhesion.setIdVoluntario(generateVolunteerId(adhesion));
            nuevaAdhesion.setNombre(adhesion.getNombre());
            nuevaAdhesion.setApellido(adhesion.getApellido());
            nuevaAdhesion.setEmail(adhesion.getEmail());
            nuevaAdhesion.setTelefono(adhesion.getTelefono());
            nuevaAdhesion.setEstado(AdhesionEvento.EstadoAdhesion.PENDIENTE);
            nuevaAdhesion.setFechaAdhesion(LocalDateTime.now());
            
            // Guardar en base de datos
            AdhesionEvento adhesionGuardada = adhesionEventoRepository.save(nuevaAdhesion);
            
            System.out.println("✅ Adhesión registrada correctamente");
            System.out.println("📋 ID: " + adhesionGuardada.getId());
            System.out.println("🎯 Evento: " + adhesionGuardada.getEventId());
            System.out.println("📧 Email: " + adhesionGuardada.getEmail());
            
            // Enviar confirmación
            sendConfirmationEmail(adhesion);
            
        } catch (Exception e) {
            System.err.println("❌ Error al registrar adhesión: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String generateVolunteerId(EventAdhesion adhesion) {
        // Generar un ID único para el voluntario basado en email
        return adhesion.getIdOrganizacion() + "-" + adhesion.getEmail().hashCode();
    }

    private void sendConfirmationEmail(EventAdhesion adhesion) {
        try {
            // Enviar email de confirmación
            System.out.println("📧 Enviando confirmación por email a: " + adhesion.getEmail());
            // Aquí iría la lógica de envío de email
            System.out.println("✅ Email de confirmación enviado");
        } catch (Exception e) {
            System.err.println("❌ Error al enviar email: " + e.getMessage());
        }
    }
}