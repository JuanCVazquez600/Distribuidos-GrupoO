package com.grpc.inventario;

import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

import Distribuidos_GrupoO.ServidorGRPC.model.InventarioDeDonaciones;
import Distribuidos_GrupoO.ServidorGRPC.model.Usuario;
import Distribuidos_GrupoO.ServidorGRPC.service.implementation.InventarioDeDonacionesServiceImplementation;
import Distribuidos_GrupoO.ServidorGRPC.service.implementation.UsuarioServiceImplementation;

import java.util.List;

@GRpcService
public class InventarioServiceGrpcImpl extends InventarioServiceGrpc.InventarioServiceImplBase {

    @Autowired
    private InventarioDeDonacionesServiceImplementation inventarioService;

    @Autowired
    private UsuarioServiceImplementation usuarioService;

    @Override
    public void agregarDonacion(InventarioProto.DonacionRequest request, StreamObserver<InventarioProto.Respuesta> responseObserver) {
        try {
            if (!hasPermission(request.getUserId(), "GESTIONAR_INVENTARIO")) {
                InventarioProto.Respuesta respuesta = InventarioProto.Respuesta.newBuilder()
                        .setExito(false)
                        .setMensaje("Permiso denegado: Solo PRESIDENTE o VOCAL pueden agregar donaciones")
                        .build();
                responseObserver.onNext(respuesta);
                responseObserver.onCompleted();
                return;
            }
            InventarioDeDonaciones inventario = mapProtoToInventario(request);
            inventarioService.altaInventario(inventario);
            InventarioProto.Respuesta respuesta = InventarioProto.Respuesta.newBuilder()
                    .setExito(true)
                    .setMensaje("Donación agregada correctamente")
                    .build();
            responseObserver.onNext(respuesta);
        } catch (Exception e) {
            InventarioProto.Respuesta respuesta = InventarioProto.Respuesta.newBuilder()
                    .setExito(false)
                    .setMensaje("Error: " + e.getMessage())
                    .build();
            responseObserver.onNext(respuesta);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void modificarDonacion(InventarioProto.DonacionRequest request, StreamObserver<InventarioProto.Respuesta> responseObserver) {
        try {
            if (!hasPermission(request.getUserId(), "GESTIONAR_INVENTARIO")) {
                InventarioProto.Respuesta respuesta = InventarioProto.Respuesta.newBuilder()
                        .setExito(false)
                        .setMensaje("Permiso denegado: Solo PRESIDENTE o VOCAL pueden modificar donaciones")
                        .build();
                responseObserver.onNext(respuesta);
                responseObserver.onCompleted();
                return;
            }
            InventarioDeDonaciones inventario = mapProtoToInventario(request);
            inventarioService.modificarInventario(inventario);
            InventarioProto.Respuesta respuesta = InventarioProto.Respuesta.newBuilder()
                    .setExito(true)
                    .setMensaje("Donación modificada correctamente")
                    .build();
            responseObserver.onNext(respuesta);
        } catch (Exception e) {
            InventarioProto.Respuesta respuesta = InventarioProto.Respuesta.newBuilder()
                    .setExito(false)
                    .setMensaje("Error: " + e.getMessage())
                    .build();
            responseObserver.onNext(respuesta);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void listarDonaciones(InventarioProto.Empty request, StreamObserver<InventarioProto.DonacionesResponse> responseObserver) {
        try {
            List<InventarioDeDonaciones> donaciones = inventarioService.listarInventarios();
            InventarioProto.DonacionesResponse.Builder responseBuilder = InventarioProto.DonacionesResponse.newBuilder();
            for (InventarioDeDonaciones d : donaciones) {
                responseBuilder.addDonaciones(mapInventarioToProto(d));
            }
            responseObserver.onNext(responseBuilder.build());
        } catch (Exception e) {
            responseObserver.onNext(InventarioProto.DonacionesResponse.newBuilder().build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void bajaDonacion(InventarioProto.DonacionIdRequest request, StreamObserver<InventarioProto.Respuesta> responseObserver) {
        try {
            if (!hasPermission(request.getUserId(), "GESTIONAR_INVENTARIO")) {
                InventarioProto.Respuesta respuesta = InventarioProto.Respuesta.newBuilder()
                        .setExito(false)
                        .setMensaje("Permiso denegado: Solo PRESIDENTE o VOCAL pueden dar de baja donaciones")
                        .build();
                responseObserver.onNext(respuesta);
                responseObserver.onCompleted();
                return;
            }
            inventarioService.eliminarInventario(request.getId());
            InventarioProto.Respuesta respuesta = InventarioProto.Respuesta.newBuilder()
                    .setExito(true)
                    .setMensaje("Donación dada de baja correctamente")
                    .build();
            responseObserver.onNext(respuesta);
        } catch (Exception e) {
            InventarioProto.Respuesta respuesta = InventarioProto.Respuesta.newBuilder()
                    .setExito(false)
                    .setMensaje("Error: " + e.getMessage())
                    .build();
            responseObserver.onNext(respuesta);
        }
        responseObserver.onCompleted();
    }

    // Auxiliares

    private InventarioDeDonaciones mapProtoToInventario(InventarioProto.DonacionRequest proto) {
        InventarioDeDonaciones inventario = new InventarioDeDonaciones();
        inventario.setId(proto.getId());
        inventario.setCategoria(InventarioDeDonaciones.CategoriaEnum.valueOf(proto.getCategoria().name())); //ajustar
        inventario.setDescripcion(proto.getDescripcion());
        inventario.setCantidad(proto.getCantidad());
        inventario.setEliminado(proto.getEliminado());
        return inventario;
    }

    private InventarioProto.DonacionRequest mapInventarioToProto(InventarioDeDonaciones inventario) {
        return InventarioProto.DonacionRequest.newBuilder()
                .setId(inventario.getId())
                .setCategoria(InventarioProto.Categoria.valueOf(inventario.getCategoria().name()))// ajustar
                .setDescripcion(inventario.getDescripcion())
                .setCantidad(inventario.getCantidad())
                .setEliminado(inventario.getEliminado())
                .build();
    }

    private boolean hasPermission(int userId, String action) {
        try {
            Usuario usuario = usuarioService.buscarPorId(userId);
            if (usuario == null) return false;
            String rol = usuario.getRol();
            switch (action) {
                case "GESTIONAR_INVENTARIO":
                    return "PRESIDENTE".equals(rol) || "VOCAL".equals(rol);
                case "VER_INVENTARIO":
                    return true; // Todos pueden ver
                default:
                    return true; // Para acciones no especificadas, permitir
            }
        } catch (Exception e) {
            return false;
        }
    }
}
