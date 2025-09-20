package com.grpc.usuarios;

import Distribuidos_GrupoO.ServidorGRPC.model.Usuario;
import Distribuidos_GrupoO.ServidorGRPC.service.implementation.UsuarioServiceImplementation;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@GRpcService
public class UsuarioServiceGrpcImpl extends com.grpc.usuarios.UsuarioServiceGrpc.UsuarioServiceImplBase {

    @Autowired
    private UsuarioServiceImplementation usuarioService;

    @Override
    public void crearUsuario(com.grpc.usuarios.UsuariosProto.UsuarioRequest request, StreamObserver<com.grpc.usuarios.UsuariosProto.Respuesta> responseObserver) {
        try {
            Usuario usuario = mapProtoToUsuario(request);
            usuarioService.crearUsuario(usuario);
            com.grpc.usuarios.UsuariosProto.Respuesta respuesta = com.grpc.usuarios.UsuariosProto.Respuesta.newBuilder()
                    .setExito(true)
                    .setMensaje("Usuario creado correctamente")
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
            Usuario usuario = usuarioService.buscarPorNombreUsuario(email); // O método login si lo tenés
            // Prueba
            if (usuario != null && clave.equals(usuario.getClave())) {
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
}
