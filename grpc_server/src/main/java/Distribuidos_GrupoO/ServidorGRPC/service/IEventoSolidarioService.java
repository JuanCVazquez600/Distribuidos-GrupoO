package Distribuidos_GrupoO.ServidorGRPC.service;

import Distribuidos_GrupoO.ServidorGRPC.model.EventoSolidario;
import java.util.List;

public interface IEventoSolidarioService {

    EventoSolidario crearEvento(EventoSolidario evento);

    EventoSolidario modificarEvento(EventoSolidario evento);

    void eliminarEvento(Integer id);

    List<EventoSolidario> listarEventos();

    EventoSolidario buscarPorId(Integer id);

    void agregarParticipante(Integer eventoId, String nombreUsuario);

    void quitarParticipante(Integer eventoId, String nombreUsuario);
}
