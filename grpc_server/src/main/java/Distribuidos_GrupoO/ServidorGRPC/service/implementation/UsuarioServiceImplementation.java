package Distribuidos_GrupoO.ServidorGRPC.service.implementation;

import Distribuidos_GrupoO.ServidorGRPC.model.Usuario;
import Distribuidos_GrupoO.ServidorGRPC.repository.UsuarioRepository;
import Distribuidos_GrupoO.ServidorGRPC.service.IUsuarioService;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Optional;

@GRpcService
public class UsuarioServiceImplementation implements IUsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    //@Autowired
    //private PasswordEncoder passwordEncoder;

    //@Override
    //public Usuario crearUsuario(Usuario usuario) {
    //    usuario.setActivo(true);
    //    usuario.setClaveEncriptada(passwordEncoder.encode(usuario.getClaveEncriptada()));
    //    return usuarioRepository.save(usuario);
    //}

    //@Override
    //public Usuario modificarUsuario(Usuario usuario) {
    //    Usuario existente = usuarioRepository.findById(usuario.getId())
    //            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

      //  existente.setNombre(usuario.getNombre());
      //  existente.setApellido(usuario.getApellido());
      //  existente.setTelefono(usuario.getTelefono());
      //  existente.setEmail(usuario.getEmail());
      //  existente.setNombreUsuario(usuario.getNombreUsuario());
      //  existente.setRol(usuario.getRol());
      //  existente.setActivo(usuario.getActivo());

      //  if (usuario.getClaveEncriptada() != null && !usuario.getClaveEncriptada().isEmpty()) {
      //      existente.setClaveEncriptada(passwordEncoder.encode(usuario.getClaveEncriptada()));
      //  }
      //  return usuarioRepository.save(existente);
    //}


    @Override
    public Usuario modificarUsuario(Usuario usuario) {
        Usuario existente = usuarioRepository.findById(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        existente.setNombre(usuario.getNombre());
        existente.setApellido(usuario.getApellido());
        existente.setTelefono(usuario.getTelefono());
        existente.setEmail(usuario.getEmail());
        existente.setNombreUsuario(usuario.getNombreUsuario());
        existente.setRol(usuario.getRol());
        existente.setActivo(usuario.getActivo());

        if (usuario.getClave() != null && !usuario.getClave().isEmpty()) {
            existente.setClave(usuario.getClave());
        }
        return usuarioRepository.save(existente);
    }

    @Override
    public Usuario crearUsuario(Usuario usuario) {
        usuario.setActivo(true);
        usuario.setClave(usuario.getClave());
        return usuarioRepository.save(usuario);
    }


    @Override
    public void bajaUsuario(Integer id) {
        Optional<Usuario> opt = usuarioRepository.findById(id);
        if (opt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }
        Usuario usuario = opt.get();
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    @Override
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    @Override
    public Usuario buscarPorNombreUsuario(String nombreUsuario) {
        return usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @Override
    public Usuario guardarUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

}
