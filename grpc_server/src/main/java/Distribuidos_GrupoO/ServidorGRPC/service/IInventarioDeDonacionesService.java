package Distribuidos_GrupoO.ServidorGRPC.service;

import Distribuidos_GrupoO.ServidorGRPC.model.InventarioDeDonaciones;
import java.util.List;

public interface IInventarioDeDonacionesService {

    InventarioDeDonaciones altaInventario(InventarioDeDonaciones inventario);

    InventarioDeDonaciones modificarInventario(InventarioDeDonaciones inventario);

    void eliminarInventario(Integer id);

    List<InventarioDeDonaciones> listarInventarios();
}

