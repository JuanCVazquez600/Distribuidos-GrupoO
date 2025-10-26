package com.ong.donationexcel.service;

import com.ong.donationexcel.dto.EventoSolidarioDTO;
import com.ong.donationexcel.model.EventoSolidario;
import com.ong.donationexcel.model.Usuario;
import com.ong.donationexcel.repository.EventoSolidarioRepository;
import com.ong.donationexcel.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EventoSolidarioService {

    @Autowired
    private EventoSolidarioRepository eventoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Obtiene todos los eventos activos
     */
    public List<EventoSolidarioDTO> obtenerTodosLosEventos() {
    return eventoRepository.findByEliminadoFalse()
        .stream()
        .distinct()
        .map(this::convertirADTO)
        .collect(Collectors.toList());
    }

    /**
     * Crea un nuevo evento
     */
    public EventoSolidarioDTO crearEvento(EventoSolidarioDTO eventoDTO) {
        if (eventoDTO.getFechaEvento().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("La fecha del evento debe ser futura");
        }

        EventoSolidario entity = convertirAEntity(eventoDTO);
        entity.setEliminado(false);

        EventoSolidario savedEntity = eventoRepository.save(entity);
        return convertirADTO(savedEntity);
    }

    /**
     * Busca un evento por ID
     */
    public Optional<EventoSolidarioDTO> obtenerEventoPorId(Integer id) {
        return eventoRepository.findById(id)
                .filter(evento -> !evento.getEliminado())
                .map(this::convertirADTO);
    }

    /**
     * Actualiza un evento existente
     */
    public Optional<EventoSolidarioDTO> actualizarEvento(Integer id, EventoSolidarioDTO eventoDTO) {
        return eventoRepository.findById(id)
                .filter(evento -> !evento.getEliminado())
                .map(existingEvento -> {
                    actualizarCamposEntity(existingEvento, eventoDTO);
                    existingEvento.setFechaModificacion(LocalDateTime.now());
                    EventoSolidario updatedEntity = eventoRepository.save(existingEvento);
                    return convertirADTO(updatedEntity);
                });
    }

    /**
     * Elimina lógicamente un evento
     */
    public boolean eliminarEvento(Integer id) {
        Optional<EventoSolidario> evento = eventoRepository.findById(id);
        if (evento.isPresent() && !evento.get().getEliminado()) {
            evento.get().setEliminado(true);
            evento.get().setFechaModificacion(LocalDateTime.now());
            eventoRepository.save(evento.get());
            return true;
        }
        return false;
    }

    /**
     * Busca eventos por rango de fechas
     */
    public List<EventoSolidarioDTO> obtenerEventosPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return eventoRepository.findByFechaEventoBetweenAndEliminadoFalse(fechaInicio, fechaFin)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca eventos por nombre
     */
    public List<EventoSolidarioDTO> buscarPorNombre(String nombre) {
        return eventoRepository.findByNombreEventoContainingIgnoreCaseAndEliminadoFalse(nombre)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Agrega un participante a un evento
     */
    public boolean agregarParticipante(Integer eventoId, Integer usuarioId) {
        Optional<EventoSolidario> eventoOpt = eventoRepository.findById(eventoId);
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);

        if (eventoOpt.isPresent() && usuarioOpt.isPresent()) {
            EventoSolidario evento = eventoOpt.get();
            Usuario usuario = usuarioOpt.get();

            if (!usuario.getActivo()) {
                throw new RuntimeException("Solo usuarios activos pueden participar en eventos");
            }

            if (evento.getParticipantes().contains(usuario)) {
                throw new RuntimeException("El usuario ya participa en este evento");
            }

            evento.getParticipantes().add(usuario);
            eventoRepository.save(evento);
            return true;
        }
        return false;
    }

    /**
     * Quita un participante de un evento
     */
    public boolean quitarParticipante(Integer eventoId, Integer usuarioId) {
        Optional<EventoSolidario> eventoOpt = eventoRepository.findById(eventoId);
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);

        if (eventoOpt.isPresent() && usuarioOpt.isPresent()) {
            EventoSolidario evento = eventoOpt.get();
            Usuario usuario = usuarioOpt.get();

            if (!evento.getParticipantes().contains(usuario)) {
                throw new RuntimeException("El usuario no participa en este evento");
            }

            evento.getParticipantes().remove(usuario);
            eventoRepository.save(evento);
            return true;
        }
        return false;
    }

    /**
     * Obtiene eventos con participantes (para reportes de participación)
     */
    public List<EventoSolidarioDTO> obtenerEventosConParticipantes() {
        return eventoRepository.findEventosConParticipantes()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene eventos por participante
     */
    public List<EventoSolidarioDTO> obtenerEventosPorParticipante(Integer usuarioId) {
        return eventoRepository.findByParticipanteId(usuarioId)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Convierte Entity a DTO
     */
    private EventoSolidarioDTO convertirADTO(EventoSolidario entity) {
        // Log de participantes para depuración
        if (entity.getParticipantes() != null) {
            System.out.println("Evento: " + entity.getNombreEvento() + " - Participantes: " + entity.getParticipantes().stream().map(u -> u.getId() + ":" + u.getNombreCompleto()).collect(java.util.stream.Collectors.toList()));
        }
        // Forzar la carga de participantes (inicializa la colección si es LAZY)
        if (entity.getParticipantes() != null) {
            entity.getParticipantes().size();
        }
        EventoSolidarioDTO dto = new EventoSolidarioDTO();
        dto.setId(entity.getId());
        dto.setNombreEvento(entity.getNombreEvento());
        dto.setDescripcionEvento(entity.getDescripcionEvento());
        dto.setFechaEvento(entity.getFechaEvento());
        dto.setEliminado(entity.getEliminado());

        dto.setFechaModificacion(entity.getFechaModificacion());

        // Convertir usuarios a strings
        if (entity.getUsuarioAlta() != null) {
            dto.setUsuarioAltaId(entity.getUsuarioAlta().getId());
            dto.setUsuarioAlta(entity.getUsuarioAlta().getNombreCompleto());
        }
        if (entity.getUsuarioModificacion() != null) {
            dto.setUsuarioModificacion(entity.getUsuarioModificacion().getNombreCompleto());
        }

        // Información de participantes
        dto.setNumeroParticipantes(entity.getParticipantes().size());
        dto.setParticipantes(entity.getParticipantes().stream()
                .map(Usuario::getNombreCompleto)
                .collect(Collectors.toSet()));

        return dto;
    }

    /**
     * Convierte DTO a Entity
     */
    private EventoSolidario convertirAEntity(EventoSolidarioDTO dto) {
        EventoSolidario entity = new EventoSolidario();
        entity.setNombreEvento(dto.getNombreEvento());
        entity.setDescripcionEvento(dto.getDescripcionEvento());
        entity.setFechaEvento(dto.getFechaEvento());
        return entity;
    }

    /**
     * Actualiza los campos de una entity existente con los datos del DTO
     */
    private void actualizarCamposEntity(EventoSolidario entity, EventoSolidarioDTO dto) {
        entity.setNombreEvento(dto.getNombreEvento());
        entity.setDescripcionEvento(dto.getDescripcionEvento());
        entity.setFechaEvento(dto.getFechaEvento());
    }
}
