package com.grpc.usuarios;

import Distribuidos_GrupoO.ServidorGRPC.model.Usuario;
import Distribuidos_GrupoO.ServidorGRPC.service.implementation.UsuarioServiceImplementation;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

@GRpcService
public class UsuarioServiceGrpcImpl extends com.grpc.usuarios.UsuarioServiceGrpc.UsuarioServiceImplBase {

    @Autowired
    private UsuarioServiceImplementation usuarioService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void crearUsuario(com.grpc.usuarios.UsuariosProto.UsuarioRequest request, StreamObserver<com.grpc.usuarios.UsuariosProto.Respuesta> responseObserver) {
        try {
             if (!hasPermission(request.getUserId(), "CREAR_USUARIO")) {
                 com.grpc.usuarios.UsuariosProto.Respuesta respuesta = com.grpc.usuarios.UsuariosProto.Respuesta.newBuilder()
                         .setExito(false)
                         .setMensaje("Permiso denegado: Solo el PRESIDENTE puede crear usuarios")
                         .build();
                 responseObserver.onNext(respuesta);
                 responseObserver.onCompleted();
                 return;
             }
            Usuario usuario = mapProtoToUsuario(request);
            Usuario usuarioCreado = usuarioService.crearUsuario(usuario);
            com.grpc.usuarios.UsuariosProto.Respuesta respuesta = com.grpc.usuarios.UsuariosProto.Respuesta.newBuilder()
                    .setExito(true)
                    .setMensaje("Usuario creado correctamente. Contraseña: " + usuarioCreado.getClave()) // Temporal para testing
                    .build();
            responseObserver.onNext(respuesta);
        } catch (Exception e) {
            com.grpc.usuarios.UsuariosProto.Respuesta respuesta = com.grpc.usuarios.UsuariosProto.Respuesta.newBuilder()
                    .setExito(false)
                    .setMensaje("Error: " + e.getMessage())
                    .build();
            responseObserver.onNext(respuesta);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void modificarUsuario(com.grpc.usuarios.UsuariosProto.UsuarioRequest request, StreamObserver<com.grpc.usuarios.UsuariosProto.Respuesta> responseObserver) {
        try {
            if (!hasPermission(request.getUserId(), "MODIFICAR_USUARIO")) {
                com.grpc.usuarios.UsuariosProto.Respuesta respuesta = com.grpc.usuarios.UsuariosProto.Respuesta.newBuilder()
                        .setExito(false)
                        .setMensaje("Permiso denegado: Solo el PRESIDENTE puede modificar usuarios")
                        .build();
                responseObserver.onNext(respuesta);
                responseObserver.onCompleted();
                return;
            }
            Usuario usuario = mapProtoToUsuario(request);
            usuarioService.modificarUsuario(usuario);
            com.grpc.usuarios.UsuariosProto.Respuesta respuesta = com.grpc.usuarios.UsuariosProto.Respuesta.newBuilder()
                    .setExito(true)
                    .setMensaje("Usuario modificado correctamente")
                    .build();
            responseObserver.onNext(respuesta);
        } catch (Exception e) {
            com.grpc.usuarios.UsuariosProto.Respuesta respuesta = com.grpc.usuarios.UsuariosProto.Respuesta.newBuilder()
                    .setExito(false)
                    .setMensaje("Error: " + e.getMessage())
                    .build();
            responseObserver.onNext(respuesta);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void listarUsuarios(com.grpc.usuarios.UsuariosProto.Empty request, StreamObserver<com.grpc.usuarios.UsuariosProto.UsuariosResponse> responseObserver) {
        try {
            List<Usuario> usuarios = usuarioService.listarUsuarios();
            com.grpc.usuarios.UsuariosProto.UsuariosResponse.Builder responseBuilder = com.grpc.usuarios.UsuariosProto.UsuariosResponse.newBuilder();
            for (Usuario u : usuarios) {
                responseBuilder.addUsuarios(mapUsuarioToProto(u));
            }
            responseObserver.onNext(responseBuilder.build());
        } catch (Exception e) {
            // En caso de error, enviamos respuesta vacía o manejar error según convenga
            responseObserver.onNext(com.grpc.usuarios.UsuariosProto.UsuariosResponse.newBuilder().build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void bajaUsuario(com.grpc.usuarios.UsuariosProto.UsuarioIdRequest request, StreamObserver<com.grpc.usuarios.UsuariosProto.Respuesta> responseObserver) {
        try {
            if (!hasPermission(request.getUserId(), "BAJA_USUARIO")) {
                com.grpc.usuarios.UsuariosProto.Respuesta respuesta = com.grpc.usuarios.UsuariosProto.Respuesta.newBuilder()
                        .setExito(false)
                        .setMensaje("Permiso denegado: Solo el PRESIDENTE puede dar de baja usuarios")
                        .build();
                responseObserver.onNext(respuesta);
                responseObserver.onCompleted();
                return;
            }
            usuarioService.bajaUsuario(request.getId());
            com.grpc.usuarios.UsuariosProto.Respuesta respuesta = com.grpc.usuarios.UsuariosProto.Respuesta.newBuilder()
                    .setExito(true)
                    .setMensaje("Usuario dado de baja correctamente")
                    .build();
            responseObserver.onNext(respuesta);
        } catch (Exception e) {
            com.grpc.usuarios.UsuariosProto.Respuesta respuesta = com.grpc.usuarios.UsuariosProto.Respuesta.newBuilder()
                    .setExito(false)
                    .setMensaje("Error: " + e.getMessage())
                    .build();
            responseObserver.onNext(respuesta);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void login(com.grpc.usuarios.UsuariosProto.LoginRequest request, StreamObserver<com.grpc.usuarios.UsuariosProto.LoginResponse> responseObserver) {
        String email = request.getUsuarioEmail();
        String clave = request.getClave();

        boolean exito = false;
        String mensaje = "Usuario o clave incorrectos";
        com.grpc.usuarios.UsuariosProto.UsuarioRequest usuarioProto = null;

        try {
            Usuario usuario = usuarioService.buscarPorEmail(email); // Cambiado para buscar por email
            // Prueba
            if (usuario != null && passwordEncoder.matches(clave, usuario.getClave())) {
                exito = true;
                mensaje = "Login exitoso";
                usuarioProto = mapUsuarioToProto(usuario);
            }
        } catch (Exception e) {
            mensaje = e.getMessage();
        }

        com.grpc.usuarios.UsuariosProto.LoginResponse.Builder responseBuilder = com.grpc.usuarios.UsuariosProto.LoginResponse.newBuilder()
                .setExito(exito)
                .setMensaje(mensaje);

        if (usuarioProto != null) {
            responseBuilder.setUsuario(usuarioProto);
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    // --- Métodos auxiliares para mapear entre modelo y protobuf ---

    private Usuario mapProtoToUsuario(com.grpc.usuarios.UsuariosProto.UsuarioRequest proto) {
        Usuario usuario = new Usuario();
        usuario.setId(proto.getId());
        usuario.setNombreUsuario(proto.getNombreUsuario());
        usuario.setNombre(proto.getNombre());
        usuario.setApellido(proto.getApellido());
        usuario.setTelefono(proto.getTelefono());
        usuario.setClave(proto.getClave());
        usuario.setEmail(proto.getEmail());
        usuario.setRol(proto.getRol());
        usuario.setActivo(proto.getActivo());
        return usuario;
    }

    private com.grpc.usuarios.UsuariosProto.UsuarioRequest mapUsuarioToProto(Usuario usuario) {
        return com.grpc.usuarios.UsuariosProto.UsuarioRequest.newBuilder()
                .setId(usuario.getId())
                .setNombreUsuario(usuario.getNombreUsuario())
                .setNombre(usuario.getNombre())
                .setApellido(usuario.getApellido())
                .setTelefono(usuario.getTelefono())
                .setClave(usuario.getClave() != null ? usuario.getClave() : "")
                .setEmail(usuario.getEmail())
                .setRol(usuario.getRol())
                .setActivo(usuario.getActivo())
                .build();
    }

    private boolean hasPermission(int userId, String action) {
        try {
            Usuario usuario = usuarioService.buscarPorId(userId);
            if (usuario == null) return false;
            String rol = usuario.getRol();
            switch (action) {
                case "CREAR_USUARIO":
                case "MODIFICAR_USUARIO":
                case "BAJA_USUARIO":
                    return "PRESIDENTE".equals(rol);
                case "GESTIONAR_EVENTOS":
                    return "PRESIDENTE".equals(rol) || "COORDINADOR".equals(rol);
                case "GESTIONAR_INVENTARIO":
                    return "PRESIDENTE".equals(rol) || "VOCAL".equals(rol);
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
