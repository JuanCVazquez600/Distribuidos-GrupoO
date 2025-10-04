package Distribuidos_GrupoO.ServidorGRPC.repository;

import Distribuidos_GrupoO.ServidorGRPC.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    //USUARIOS
    @Query("SELECT e FROM Usuario e")
    public List<Usuario> getAllUsuario();

    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    Optional<Usuario> findByEmail(String email);
}
