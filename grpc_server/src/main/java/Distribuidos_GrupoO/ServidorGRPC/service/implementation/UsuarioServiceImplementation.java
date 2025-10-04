package Distribuidos_GrupoO.ServidorGRPC.service.implementation;

import Distribuidos_GrupoO.ServidorGRPC.model.Usuario;
import Distribuidos_GrupoO.ServidorGRPC.repository.UsuarioRepository;
import Distribuidos_GrupoO.ServidorGRPC.service.IUsuarioService;
import Distribuidos_GrupoO.ServidorGRPC.service.EmailService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.List;
import java.util.Optional;
import java.security.SecureRandom;

@Service
public class UsuarioServiceImplementation implements IUsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EventoSolidarioServiceImplementation eventoService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    private String generarContrasenaAleatoria() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }


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
            existente.setClave(passwordEncoder.encode(usuario.getClave()));
        }
        return usuarioRepository.save(existente);
    }

    @Override
    public Usuario crearUsuario(Usuario usuario) {
        String contrasenaAleatoria = generarContrasenaAleatoria();
        emailService.enviarContrasena(usuario.getEmail(), contrasenaAleatoria);
        usuario.setActivo(true);
        usuario.setClave(passwordEncoder.encode(contrasenaAleatoria));
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
        // Quitar al usuario de eventos futuros
        eventoService.quitarUsuarioDeEventosFuturos(id);
    }

    @Override
    public List<Usuario> listarUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        System.out.println("Usuarios encontrados: " + usuarios.size());
        return usuarios;
    }

    @Override
    public Usuario buscarPorNombreUsuario(String nombreUsuario) {
        return usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @Override
    public Usuario buscarPorId(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @Override
    public Usuario guardarUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
}
