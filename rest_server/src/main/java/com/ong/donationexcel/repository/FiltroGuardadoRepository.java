package com.ong.donationexcel.repository;

import com.ong.donationexcel.model.FiltroGuardado;
import com.ong.donationexcel.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FiltroGuardadoRepository extends JpaRepository<FiltroGuardado, Integer> {

    /**
     * Busca filtros por usuario que no estén eliminados
     */
    List<FiltroGuardado> findByUsuarioAndEliminadoFalse(Usuario usuario);

    /**
     * Busca filtros por usuario que no estén eliminados y que estén activos
     */
    List<FiltroGuardado> findByUsuarioAndEliminadoFalseAndActivoTrue(Usuario usuario);

    /**
     * Busca filtros por usuario y tipo que no estén eliminados
     */
    List<FiltroGuardado> findByUsuarioAndTipoFiltroAndEliminadoFalse(Usuario usuario, FiltroGuardado.TipoFiltro tipoFiltro);

    /**
     * Busca filtros por usuario y tipo que no estén eliminados y que estén activos
     */
    List<FiltroGuardado> findByUsuarioAndTipoFiltroAndEliminadoFalseAndActivoTrue(Usuario usuario, FiltroGuardado.TipoFiltro tipoFiltro);

    /**
     * Busca filtro por nombre y usuario que no esté eliminado
     */
    FiltroGuardado findByNombreAndUsuarioAndEliminadoFalse(String nombre, Usuario usuario);

    /**
     * Busca filtro por nombre y usuario que no esté eliminado y que esté activo
     */
    FiltroGuardado findByNombreAndUsuarioAndEliminadoFalseAndActivoTrue(String nombre, Usuario usuario);

    /**
     * Busca filtros por tipo que no estén eliminados
     */
    List<FiltroGuardado> findByTipoFiltroAndEliminadoFalse(FiltroGuardado.TipoFiltro tipoFiltro);

    /**
     * Verifica si existe un filtro con el mismo nombre para el usuario
     */
    boolean existsByNombreAndUsuarioAndEliminadoFalse(String nombre, Usuario usuario);

    /**
     * Verifica si existe un filtro con el mismo nombre para el usuario que no esté eliminado y esté activo
     */
    boolean existsByNombreAndUsuarioAndEliminadoFalseAndActivoTrue(String nombre, Usuario usuario);

    /**
     * Busca filtros eliminados por usuario (para recuperación si es necesario)
     */
    List<FiltroGuardado> findByUsuarioAndEliminadoTrue(Usuario usuario);
}
