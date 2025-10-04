package Distribuidos_GrupoO.ServidorGRPC.service.implementation;

import Distribuidos_GrupoO.ServidorGRPC.model.EventoSolidario;
import Distribuidos_GrupoO.ServidorGRPC.model.Usuario;
import Distribuidos_GrupoO.ServidorGRPC.repository.EventoSolidarioRepository;
import Distribuidos_GrupoO.ServidorGRPC.repository.UsuarioRepository;
import Distribuidos_GrupoO.ServidorGRPC.service.IEventoSolidarioService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EventoSolidarioServiceImplementation implements IEventoSolidarioService {

    @Autowired
    private EventoSolidarioRepository eventoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public EventoSolidario crearEvento(EventoSolidario evento) {
        if (evento.getFechaEvento().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("La fecha del evento debe ser futura");
        }
        return eventoRepository.save(evento);
    }

    @Override
    public EventoSolidario modificarEvento(EventoSolidario evento) {
        Optional<EventoSolidario> opt = eventoRepository.findById(evento.getId());
        if (opt.isEmpty()) {
            throw new RuntimeException("Evento no encontrado");
        }
        EventoSolidario existente = opt.get();
        existente.setNombreEvento(evento.getNombreEvento());
        existente.setDescripcionEvento(evento.getDescripcionEvento());
        existente.setFechaEvento(evento.getFechaEvento());
        existente.setMiembros(evento.getMiembros());
        return eventoRepository.save(existente);
    }

    @Override
    public void eliminarEvento(Integer id) {
        Optional<EventoSolidario> opt = eventoRepository.findById(id);
        if (opt.isEmpty()) {
            throw new RuntimeException("Evento no encontrado");
        }
        EventoSolidario evento = opt.get();
        if (evento.getFechaEvento().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("No se pueden eliminar eventos pasados");
        }
        eventoRepository.delete(evento);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventoSolidario> listarEventos() {
        List<EventoSolidario> eventos = eventoRepository.findAll();
        // Forzar carga de miembros para evitar problema de LazyInitializationException
        eventos.forEach(evento -> evento.getMiembros().size());
        return eventos;
    }

    @Override
    public EventoSolidario buscarPorId(Integer id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
    }


    @Override
    @Transactional
    public void agregarParticipante(Integer eventoId, Integer usuarioId) {
        EventoSolidario evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!usuario.getActivo()) {
            throw new RuntimeException("Solo miembros activos pueden participar en eventos");
        }

        if (evento.getMiembros().contains(usuario)) {
            throw new RuntimeException("Ya participas en este evento");
        }

        evento.getMiembros().add(usuario);
        eventoRepository.save(evento);
    }

    @Override
    @Transactional
    public void quitarParticipante(Integer eventoId, Integer usuarioId) {
        EventoSolidario evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!evento.getMiembros().contains(usuario)) {
            throw new RuntimeException("No participas en este evento");
        }

        evento.getMiembros().remove(usuario);
        eventoRepository.save(evento);
    }

    @Override
    public void quitarUsuarioDeEventosFuturos(Integer usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<EventoSolidario> eventosFuturos = eventoRepository.findAll().stream()
                .filter(evento -> evento.getFechaEvento().isAfter(LocalDateTime.now()))
                .toList();

        for (EventoSolidario evento : eventosFuturos) {
            if (evento.getMiembros().contains(usuario)) {
                evento.getMiembros().remove(usuario);
                eventoRepository.save(evento);
            }
        }
    }
}
