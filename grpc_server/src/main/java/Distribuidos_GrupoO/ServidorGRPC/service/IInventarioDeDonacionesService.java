package Distribuidos_GrupoO.ServidorGRPC.service;

import Distribuidos_GrupoO.ServidorGRPC.model.InventarioDeDonaciones;
import java.util.List;
import java.util.Optional;

public interface IInventarioDeDonacionesService {

    InventarioDeDonaciones altaInventario(InventarioDeDonaciones inventario);

    InventarioDeDonaciones modificarInventario(InventarioDeDonaciones inventario);

    void eliminarInventario(Integer id);

    List<InventarioDeDonaciones> listarInventarios();

    // Métodos para transferencia de donaciones
    Optional<InventarioDeDonaciones> buscarPorCategoriaYDescripcion(InventarioDeDonaciones.CategoriaEnum categoria, String descripcion);

    InventarioDeDonaciones actualizarCantidad(InventarioDeDonaciones inventario, int cantidadADiferencia);

    InventarioDeDonaciones crearOActualizarInventario(InventarioDeDonaciones.CategoriaEnum categoria, String descripcion, int cantidad);
}

