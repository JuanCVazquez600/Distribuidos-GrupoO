package Distribuidos_GrupoO.ServidorGRPC.service.implementation;

import Distribuidos_GrupoO.ServidorGRPC.model.EventoSolidario;
import Distribuidos_GrupoO.ServidorGRPC.model.Usuario;
import Distribuidos_GrupoO.ServidorGRPC.repository.EventoSolidarioRepository;
import Distribuidos_GrupoO.ServidorGRPC.repository.UsuarioRepository;
import Distribuidos_GrupoO.ServidorGRPC.service.IEventoSolidarioService;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@GRpcService
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
    public List<EventoSolidario> listarEventos() {
        return eventoRepository.findAll();
    }

    @Override
    public EventoSolidario buscarPorId(Integer id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
    }


    @Override
    public void agregarParticipante(Integer eventoId, String nombreUsuario) {
        EventoSolidario evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        if (evento.getFechaEvento().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("No se puede participar en eventos pasados");
        }
        Usuario usuario = usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (evento.getMiembros().contains(usuario)) {
            throw new RuntimeException("Ya participas en este evento");
        }
        evento.getMiembros().add(usuario);
        eventoRepository.save(evento);
    }

    @Override
    public void quitarParticipante(Integer eventoId, String nombreUsuario) {
        EventoSolidario evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        if (evento.getFechaEvento().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("No se puede modificar participación en eventos pasados");
        }
        Usuario usuario = usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (!evento.getMiembros().contains(usuario)) {
            throw new RuntimeException("No participas en este evento");
        }
        evento.getMiembros().remove(usuario);
        eventoRepository.save(evento);
    }
}