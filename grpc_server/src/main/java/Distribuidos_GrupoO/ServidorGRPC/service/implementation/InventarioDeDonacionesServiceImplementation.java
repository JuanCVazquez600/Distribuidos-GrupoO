package Distribuidos_GrupoO.ServidorGRPC.service.implementation;

import Distribuidos_GrupoO.ServidorGRPC.model.InventarioDeDonaciones;
import Distribuidos_GrupoO.ServidorGRPC.repository.InventarioDeDonacionesRepository;
import Distribuidos_GrupoO.ServidorGRPC.service.IInventarioDeDonacionesService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

public class InventarioDeDonacionesServiceImplementation implements IInventarioDeDonacionesService {


    @Autowired
    private InventarioDeDonacionesRepository inventarioRepository;


    public InventarioDeDonaciones altaInventario(InventarioDeDonaciones inventario) {
        return inventarioRepository.save(inventario);
    }


    public InventarioDeDonaciones modificarInventario(InventarioDeDonaciones inventario) {
        Optional<InventarioDeDonaciones> opt = inventarioRepository.findById(inventario.getId());
        if (opt.isEmpty()) {
            throw new RuntimeException("Inventario no encontrado");
        }
        InventarioDeDonaciones existente = opt.get();
        existente.setCategoria(inventario.getCategoria());
        existente.setDescripcion(inventario.getDescripcion());
        existente.setCantidad(inventario.getCantidad());
        existente.setEliminado(inventario.getEliminado());
        return inventarioRepository.save(existente);
    }


    public void eliminarInventario(Integer id) {
        Optional<InventarioDeDonaciones> opt = inventarioRepository.findById(id);
        if (opt.isEmpty()) {
            throw new RuntimeException("Inventario no encontrado");
        }
        InventarioDeDonaciones inventario = opt.get();
        inventario.setEliminado(true);
        inventarioRepository.save(inventario);
    }

    public List<InventarioDeDonaciones> listarInventarios() {
        return inventarioRepository.findAll();
    }
}