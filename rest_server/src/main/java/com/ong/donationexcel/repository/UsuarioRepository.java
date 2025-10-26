package com.ong.donationexcel.repository;

import com.ong.donationexcel.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    /**
     * Busca usuario por nombre de usuario
     */
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    /**
     * Busca usuario por email
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Busca usuarios por nombre (contiene)
     */
    List<Usuario> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Busca usuarios por apellido (contiene)
     */
    List<Usuario> findByApellidoContainingIgnoreCase(String apellido);

    /**
     * Busca usuarios por rol
     */
    List<Usuario> findByRol(String rol);

    /**
     * Busca usuarios activos
     */
    List<Usuario> findByActivoTrue();

    /**
     * Verifica si existe un usuario con el nombre de usuario especificado
     */
    boolean existsByNombreUsuario(String nombreUsuario);

    /**
     * Verifica si existe un usuario con el email especificado
     */
    boolean existsByEmail(String email);
}
