package com.ong.donationexcel.repository;

import com.ong.donationexcel.model.CategoriaEnum;
import com.ong.donationexcel.model.InventarioDeDonaciones;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DonacionRepository extends JpaRepository<InventarioDeDonaciones, Integer> {

    /**
     * Busca todas las donaciones que no estén eliminadas
     */
    List<InventarioDeDonaciones> findByEliminadoFalse();

    /**
     * Busca donaciones por categoría que no estén eliminadas
     */
    List<InventarioDeDonaciones> findByCategoriaAndEliminadoFalse(CategoriaEnum categoria);

    /**
     * Busca donaciones en un rango de fechas que no estén eliminadas
     */
    @Query("SELECT i FROM InventarioDeDonaciones i WHERE i.fechaAlta >= :fechaInicio AND i.fechaAlta <= :fechaFin AND i.eliminado = false ORDER BY i.fechaAlta DESC")
    List<InventarioDeDonaciones> findByFechaAltaBetweenAndEliminadoFalse(
        @Param("fechaInicio") LocalDateTime fechaInicio, 
        @Param("fechaFin") LocalDateTime fechaFin
    );

    /**
     * Cuenta las donaciones por categoría que no estén eliminadas
     */
    @Query("SELECT COUNT(i) FROM InventarioDeDonaciones i WHERE i.categoria = :categoria AND i.eliminado = false")
    Long countByCategoriaAndEliminadoFalse(@Param("categoria") CategoriaEnum categoria);

    /**
     * Busca las últimas donaciones ordenadas por fecha de alta
     */
    @Query("SELECT i FROM InventarioDeDonaciones i WHERE i.eliminado = false ORDER BY i.fechaAlta DESC")
    List<InventarioDeDonaciones> findRecentDonations();

    /**
     * Busca donaciones por descripción que no estén eliminadas
     */
    List<InventarioDeDonaciones> findByDescripcionContainingIgnoreCaseAndEliminadoFalse(String descripcion);

    /**
     * Busca todas las donaciones ordenadas según la consigna: por categoría y fecha de alta
     */
    @Query("SELECT i FROM InventarioDeDonaciones i WHERE i.eliminado = false ORDER BY i.categoria ASC, i.fechaAlta DESC")
    List<InventarioDeDonaciones> findAllOrderedForExcel();
}