package com.grpc.inventario;

import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

import Distribuidos_GrupoO.ServidorGRPC.model.InventarioDeDonaciones;
import Distribuidos_GrupoO.ServidorGRPC.service.implementation.InventarioDeDonacionesServiceImplementation;

import java.util.List;
import com.grpc.inventario.InventarioProto;
import com.grpc.inventario.InventarioServiceGrpc;

@GRpcService
public class InventarioServiceGrpcImpl extends InventarioServiceGrpc.InventarioServiceImplBase {

    @Autowired
    private InventarioDeDonacionesServiceImplementation inventarioService;

    @Override
    public void agregarDonacion(InventarioProto.DonacionRequest request, StreamObserver<InventarioProto.Respuesta> responseObserver) {
        try {
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

    // Aauxiliares

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
}