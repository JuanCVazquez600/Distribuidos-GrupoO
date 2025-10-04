package com.grpc.eventos;

import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

import Distribuidos_GrupoO.ServidorGRPC.model.EventoSolidario;
import Distribuidos_GrupoO.ServidorGRPC.model.Usuario;
import Distribuidos_GrupoO.ServidorGRPC.service.implementation.EventoSolidarioServiceImplementation;
import Distribuidos_GrupoO.ServidorGRPC.service.implementation.UsuarioServiceImplementation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@GRpcService
public class EventoServiceGrpcImpl extends com.grpc.eventos.EventoServiceGrpc.EventoServiceImplBase {

    @Autowired
    private EventoSolidarioServiceImplementation eventoService;

    @Autowired
    private UsuarioServiceImplementation usuarioService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Override
    public void crearEvento(com.grpc.eventos.EventosProto.EventoRequest request, StreamObserver<com.grpc.eventos.EventosProto.Respuesta> responseObserver) {
        try {
            if (!hasPermission(request.getUserId(), "GESTIONAR_EVENTOS")) {
                com.grpc.eventos.EventosProto.Respuesta respuesta = com.grpc.eventos.EventosProto.Respuesta.newBuilder()
                        .setExito(false)
                        .setMensaje("Permiso denegado: Solo PRESIDENTE o COORDINADOR pueden crear eventos")
                        .build();
                responseObserver.onNext(respuesta);
                responseObserver.onCompleted();
                return;
            }
            EventoSolidario evento = mapProtoToEvento(request);
            eventoService.crearEvento(evento);
            com.grpc.eventos.EventosProto.Respuesta respuesta = com.grpc.eventos.EventosProto.Respuesta.newBuilder()
                    .setExito(true)
                    .setMensaje("Evento creado correctamente")
                    .build();
            responseObserver.onNext(respuesta);
        } catch (Exception e) {
            com.grpc.eventos.EventosProto.Respuesta respuesta = com.grpc.eventos.EventosProto.Respuesta.newBuilder()
                    .setExito(false)
                    .setMensaje("Error: " + e.getMessage())
                    .build();
            responseObserver.onNext(respuesta);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void modificarEvento(com.grpc.eventos.EventosProto.EventoRequest request, StreamObserver<com.grpc.eventos.EventosProto.Respuesta> responseObserver) {
        try {
            if (!hasPermission(request.getUserId(), "GESTIONAR_EVENTOS")) {
                com.grpc.eventos.EventosProto.Respuesta respuesta = com.grpc.eventos.EventosProto.Respuesta.newBuilder()
                        .setExito(false)
                        .setMensaje("Permiso denegado: Solo PRESIDENTE o COORDINADOR pueden modificar eventos")
                        .build();
                responseObserver.onNext(respuesta);
                responseObserver.onCompleted();
                return;
            }
            EventoSolidario evento = mapProtoToEvento(request);
            eventoService.modificarEvento(evento);
            com.grpc.eventos.EventosProto.Respuesta respuesta = com.grpc.eventos.EventosProto.Respuesta.newBuilder()
                    .setExito(true)
                    .setMensaje("Evento modificado correctamente")
                    .build();
            responseObserver.onNext(respuesta);
        } catch (Exception e) {
            com.grpc.eventos.EventosProto.Respuesta respuesta = com.grpc.eventos.EventosProto.Respuesta.newBuilder()
                    .setExito(false)
                    .setMensaje("Error: " + e.getMessage())
                    .build();
            responseObserver.onNext(respuesta);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void listarEventos(com.grpc.eventos.EventosProto.Empty request, StreamObserver<com.grpc.eventos.EventosProto.EventosResponse> responseObserver) {
        try {
            List<EventoSolidario> eventos = eventoService.listarEventos();
            com.grpc.eventos.EventosProto.EventosResponse.Builder responseBuilder = com.grpc.eventos.EventosProto.EventosResponse.newBuilder();
            for (EventoSolidario e : eventos) {
                responseBuilder.addEventos(mapEventoToProto(e));
            }
            responseObserver.onNext(responseBuilder.build());
        } catch (Exception e) {
            responseObserver.onNext(com.grpc.eventos.EventosProto.EventosResponse.newBuilder().build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void bajaEvento(com.grpc.eventos.EventosProto.EventoIdRequest request, StreamObserver<com.grpc.eventos.EventosProto.Respuesta> responseObserver) {
        try {
            if (!hasPermission(request.getUserId(), "GESTIONAR_EVENTOS")) {
                com.grpc.eventos.EventosProto.Respuesta respuesta = com.grpc.eventos.EventosProto.Respuesta.newBuilder()
                        .setExito(false)
                        .setMensaje("Permiso denegado: Solo PRESIDENTE o COORDINADOR pueden dar de baja eventos")
                        .build();
                responseObserver.onNext(respuesta);
                responseObserver.onCompleted();
                return;
            }

            // Validar que el evento no sea pasado
            EventoSolidario evento = eventoService.buscarPorId(request.getId());
            if (evento == null) {
                com.grpc.eventos.EventosProto.Respuesta respuesta = com.grpc.eventos.EventosProto.Respuesta.newBuilder()
                        .setExito(false)
                        .setMensaje("Evento no encontrado")
                        .build();
                responseObserver.onNext(respuesta);
                responseObserver.onCompleted();
                return;
            }

            if (evento.getFechaEvento().isBefore(LocalDateTime.now())) {
                com.grpc.eventos.EventosProto.Respuesta respuesta = com.grpc.eventos.EventosProto.Respuesta.newBuilder()
                        .setExito(false)
                        .setMensaje("No se puede dar de baja un evento que ya ha pasado")
                        .build();
                responseObserver.onNext(respuesta);
                responseObserver.onCompleted();
                return;
            }

            eventoService.eliminarEvento(request.getId());
            com.grpc.eventos.EventosProto.Respuesta respuesta = com.grpc.eventos.EventosProto.Respuesta.newBuilder()
                    .setExito(true)
                    .setMensaje("Evento dado de baja correctamente")
                    .build();
            responseObserver.onNext(respuesta);
        } catch (Exception e) {
            com.grpc.eventos.EventosProto.Respuesta respuesta = com.grpc.eventos.EventosProto.Respuesta.newBuilder()
                    .setExito(false)
                    .setMensaje("Error: " + e.getMessage())
                    .build();
            responseObserver.onNext(respuesta);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void asignarMiembro(com.grpc.eventos.EventosProto.AsignarMiembroRequest request, StreamObserver<com.grpc.eventos.EventosProto.Respuesta> responseObserver) {
        try {
            Usuario usuarioSolicitante = usuarioService.buscarPorId(request.getUserId());
            Usuario usuarioMiembro = usuarioService.buscarPorId(request.getMiembroId());

            if (usuarioMiembro == null || !usuarioMiembro.getActivo()) {
                com.grpc.eventos.EventosProto.Respuesta respuesta = com.grpc.eventos.EventosProto.Respuesta.newBuilder()
                        .setExito(false)
                        .setMensaje("Solo miembros activos pueden ser asignados")
                        .build();
                responseObserver.onNext(respuesta);
                responseObserver.onCompleted();
                return;
            }

            String rolSolicitante = usuarioSolicitante.getRol();
            if ("VOLUNTARIO".equals(rolSolicitante)) {
                if (!usuarioSolicitante.getId().equals(usuarioMiembro.getId())) {
                    com.grpc.eventos.EventosProto.Respuesta respuesta = com.grpc.eventos.EventosProto.Respuesta.newBuilder()
                            .setExito(false)
                            .setMensaje("El rol VOLUNTARIO solo puede agregarse o quitarse a sí mismo")
                            .build();
                    responseObserver.onNext(respuesta);
                    responseObserver.onCompleted();
                    return;
                }
            } else if (!("PRESIDENTE".equals(rolSolicitante) || "COORDINADOR".equals(rolSolicitante))) {
                com.grpc.eventos.EventosProto.Respuesta respuesta = com.grpc.eventos.EventosProto.Respuesta.newBuilder()
                        .setExito(false)
                        .setMensaje("Permiso denegado: Solo PRESIDENTE o COORDINADOR pueden asignar miembros")
                        .build();
                responseObserver.onNext(respuesta);
                responseObserver.onCompleted();
                return;
            }

            eventoService.agregarParticipante(request.getEventoId(), request.getMiembroId());
            com.grpc.eventos.EventosProto.Respuesta respuesta = com.grpc.eventos.EventosProto.Respuesta.newBuilder()
                    .setExito(true)
                    .setMensaje("Miembro asignado correctamente")
                    .build();
            responseObserver.onNext(respuesta);
        } catch (Exception e) {
            com.grpc.eventos.EventosProto.Respuesta respuesta = com.grpc.eventos.EventosProto.Respuesta.newBuilder()
                    .setExito(false)
                    .setMensaje("Error: " + e.getMessage())
                    .build();
            responseObserver.onNext(respuesta);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void quitarMiembro(com.grpc.eventos.EventosProto.QuitarMiembroRequest request, StreamObserver<com.grpc.eventos.EventosProto.Respuesta> responseObserver) {
        try {
            Usuario usuarioSolicitante = usuarioService.buscarPorId(request.getUserId());

            String rolSolicitante = usuarioSolicitante.getRol();
            if ("VOLUNTARIO".equals(rolSolicitante)) {
                if (!usuarioSolicitante.getId().equals(request.getMiembroId())) {
                    com.grpc.eventos.EventosProto.Respuesta respuesta = com.grpc.eventos.EventosProto.Respuesta.newBuilder()
                            .setExito(false)
                            .setMensaje("El rol VOLUNTARIO solo puede agregarse o quitarse a sí mismo")
                            .build();
                    responseObserver.onNext(respuesta);
                    responseObserver.onCompleted();
                    return;
                }
            } else if (!("PRESIDENTE".equals(rolSolicitante) || "COORDINADOR".equals(rolSolicitante))) {
                com.grpc.eventos.EventosProto.Respuesta respuesta = com.grpc.eventos.EventosProto.Respuesta.newBuilder()
                        .setExito(false)
                        .setMensaje("Permiso denegado: Solo PRESIDENTE o COORDINADOR pueden quitar miembros")
                        .build();
                responseObserver.onNext(respuesta);
                responseObserver.onCompleted();
                return;
            }

            eventoService.quitarParticipante(request.getEventoId(), request.getMiembroId());
            com.grpc.eventos.EventosProto.Respuesta respuesta = com.grpc.eventos.EventosProto.Respuesta.newBuilder()
                    .setExito(true)
                    .setMensaje("Miembro removido correctamente")
                    .build();
            responseObserver.onNext(respuesta);
        } catch (Exception e) {
            com.grpc.eventos.EventosProto.Respuesta respuesta = com.grpc.eventos.EventosProto.Respuesta.newBuilder()
                    .setExito(false)
                    .setMensaje("Error: " + e.getMessage())
                    .build();
            responseObserver.onNext(respuesta);
        }
        responseObserver.onCompleted();
    }

    // Auxiliares

    private EventoSolidario mapProtoToEvento(com.grpc.eventos.EventosProto.EventoRequest proto) {
        EventoSolidario evento = new EventoSolidario();
        evento.setId(proto.getId());
        evento.setNombreEvento(proto.getNombre());
        evento.setDescripcionEvento(proto.getDescripcion());
        evento.setFechaEvento(LocalDateTime.parse(proto.getFechaHora(), FORMATTER));

        // Convierto miembros protobuf a lista de Usuario
        List<Usuario> miembros = proto.getMiembrosList().stream()
                .map(this::mapProtoToUsuario)
                .collect(Collectors.toList());
        evento.setMiembros(new java.util.HashSet<>(miembros));
        return evento;
    }

    private com.grpc.eventos.EventosProto.EventoRequest mapEventoToProto(EventoSolidario evento) {
        com.grpc.eventos.EventosProto.EventoRequest.Builder builder = com.grpc.eventos.EventosProto.EventoRequest.newBuilder()
                .setId(evento.getId())
                .setNombre(evento.getNombreEvento())
                .setDescripcion(evento.getDescripcionEvento())
                .setFechaHora(evento.getFechaEvento().format(FORMATTER));

        // Miembros a protobuf
        for (Usuario u : evento.getMiembros()) {
            builder.addMiembros(mapUsuarioToProto(u));
        }
        return builder.build();
    }

    private Usuario mapProtoToUsuario(com.grpc.eventos.EventosProto.MiembroRequest proto) {
        Usuario usuario = new Usuario();
        usuario.setId(proto.getId());
        usuario.setNombre(proto.getNombre());
        usuario.setApellido(proto.getApellido());
        return usuario;
    }

    private com.grpc.eventos.EventosProto.MiembroRequest mapUsuarioToProto(Usuario usuario) {
        return com.grpc.eventos.EventosProto.MiembroRequest.newBuilder()
                .setId(usuario.getId())
                .setNombre(usuario.getNombre())
                .setApellido(usuario.getApellido())
                .build();
    }

    private boolean hasPermission(int userId, String action) {
        try {
            Usuario usuario = usuarioService.buscarPorId(userId);
            if (usuario == null) return false;
            String rol = usuario.getRol();
            switch (action) {
                case "GESTIONAR_EVENTOS":
                    return "PRESIDENTE".equals(rol) || "COORDINADOR".equals(rol);
                case "VER_EVENTOS":
                    return true; // Todos pueden ver
                default:
                    return true; // Para acciones no especificadas, permitir
            }
        } catch (Exception e) {
            return false;
        }
    }
}
