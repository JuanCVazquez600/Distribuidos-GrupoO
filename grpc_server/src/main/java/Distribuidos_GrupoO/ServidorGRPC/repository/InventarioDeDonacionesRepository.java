package Distribuidos_GrupoO.ServidorGRPC.repository;

import Distribuidos_GrupoO.ServidorGRPC.model.InventarioDeDonaciones;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventarioDeDonacionesRepository extends JpaRepository<InventarioDeDonaciones, Integer> {

    @Override
    List<InventarioDeDonaciones> findAll();

    List<InventarioDeDonaciones> findByCategoria(InventarioDeDonaciones.CategoriaEnum categoria);

    List<InventarioDeDonaciones> findByEliminadoFalse();

    Optional<InventarioDeDonaciones> findByCategoriaAndDescripcionAndEliminadoFalse(InventarioDeDonaciones.CategoriaEnum categoria, String descripcion);
}
