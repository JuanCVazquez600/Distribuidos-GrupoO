package Distribuidos_GrupoO.ServidorGRPC.service;

import Distribuidos_GrupoO.ServidorGRPC.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface IUsuarioService {
    Usuario crearUsuario(Usuario usuario);
    Usuario modificarUsuario(Usuario usuario);
    List<Usuario> listarUsuarios();
    void bajaUsuario(Integer id);
    Usuario buscarPorNombreUsuario(String nombreUsuario);
    Usuario buscarPorId(Integer id);
    Usuario guardarUsuario(Usuario usuario);

}
