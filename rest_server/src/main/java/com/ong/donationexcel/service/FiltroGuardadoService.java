package com.ong.donationexcel.service;

import com.ong.donationexcel.dto.FiltroGuardadoDTO;
import com.ong.donationexcel.model.FiltroGuardado;
import com.ong.donationexcel.model.Usuario;
import com.ong.donationexcel.repository.FiltroGuardadoRepository;
import com.ong.donationexcel.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FiltroGuardadoService {

    @Autowired
    private FiltroGuardadoRepository filtroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Obtiene todos los filtros guardados por un usuario
     */
    public List<FiltroGuardadoDTO> obtenerFiltrosPorUsuario(Integer usuarioId) {
        Optional<Usuario> usuario = usuarioRepository.findById(usuarioId);
        if (usuario.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        return filtroRepository.findByUsuarioAndEliminadoFalseAndActivoTrue(usuario.get())
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene filtros por usuario y tipo
     */
    public List<FiltroGuardadoDTO> obtenerFiltrosPorUsuarioYTipo(Integer usuarioId, FiltroGuardado.TipoFiltro tipoFiltro) {
        Optional<Usuario> usuario = usuarioRepository.findById(usuarioId);
        if (usuario.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        return filtroRepository.findByUsuarioAndTipoFiltroAndEliminadoFalseAndActivoTrue(usuario.get(), tipoFiltro)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Crea un nuevo filtro guardado
     */
    public FiltroGuardadoDTO crearFiltro(FiltroGuardadoDTO filtroDTO) {
        Optional<Usuario> usuario = usuarioRepository.findById(filtroDTO.getUsuarioId());
        if (usuario.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        // Verificar que no exista un filtro con el mismo nombre para este usuario
        if (filtroRepository.existsByNombreAndUsuarioAndEliminadoFalseAndActivoTrue(filtroDTO.getNombre(), usuario.get())) {
            throw new RuntimeException("Ya existe un filtro con este nombre para este usuario");
        }

        FiltroGuardado entity = convertirAEntity(filtroDTO, usuario.get());
        entity.setFechaCreacion(LocalDateTime.now());
        // Si no se especifica, por defecto el filtro estará activo y no eliminado
        entity.setEliminado(filtroDTO.getEliminado() != null ? filtroDTO.getEliminado() : false);
        entity.setActivo(filtroDTO.getActivo() != null ? filtroDTO.getActivo() : true);

        FiltroGuardado savedEntity = filtroRepository.save(entity);
        return convertirADTO(savedEntity);
    }

    /**
     * Actualiza un filtro existente
     */
    public Optional<FiltroGuardadoDTO> actualizarFiltro(Integer id, FiltroGuardadoDTO filtroDTO) {
        return filtroRepository.findById(id)
                .filter(filtro -> !filtro.getEliminado())
                .map(existingFiltro -> {
                    // Verificar que el usuario sea el propietario
                    if (!existingFiltro.getUsuario().getId().equals(filtroDTO.getUsuarioId())) {
                        throw new RuntimeException("No tienes permisos para modificar este filtro");
                    }

                    // Verificar nombre único si cambió
                    if (!existingFiltro.getNombre().equals(filtroDTO.getNombre())) {
                        if (filtroRepository.existsByNombreAndUsuarioAndEliminadoFalseAndActivoTrue(filtroDTO.getNombre(), existingFiltro.getUsuario())) {
                            throw new RuntimeException("Ya existe un filtro con este nombre para este usuario");
                        }
                    }

                    actualizarCamposEntity(existingFiltro, filtroDTO);
                    existingFiltro.setFechaModificacion(LocalDateTime.now());
                    FiltroGuardado updatedEntity = filtroRepository.save(existingFiltro);
                    return convertirADTO(updatedEntity);
                });
    }

    /**
     * Elimina un filtro (lógicamente)
     */
    public boolean eliminarFiltro(Integer id, Integer usuarioId) {
        Optional<FiltroGuardado> filtro = filtroRepository.findById(id);
        if (filtro.isPresent() && !filtro.get().getEliminado()) {
            // Verificar que el usuario sea el propietario
            if (!filtro.get().getUsuario().getId().equals(usuarioId)) {
                throw new RuntimeException("No tienes permisos para eliminar este filtro");
            }

            // Marcar como inactivo (no eliminado físicamente)
            filtro.get().setActivo(false);
            filtro.get().setFechaModificacion(LocalDateTime.now());
            filtroRepository.save(filtro.get());
            return true;
        }
        return false;
    }

    /**
     * Obtiene un filtro por ID
     */
    public Optional<FiltroGuardadoDTO> obtenerFiltroPorId(Integer id) {
        return filtroRepository.findById(id)
                .filter(filtro -> !filtro.getEliminado())
                .map(this::convertirADTO);
    }

    /**
     * Convierte Entity a DTO
     */
    private FiltroGuardadoDTO convertirADTO(FiltroGuardado entity) {
        FiltroGuardadoDTO dto = new FiltroGuardadoDTO();
        dto.setId(entity.getId());
        dto.setNombre(entity.getNombre());
        dto.setTipoFiltro(entity.getTipoFiltro());
        dto.setFiltrosJson(entity.getFiltrosJson());
        dto.setActivo(entity.getActivo());
        dto.setFechaCreacion(entity.getFechaCreacion());
        dto.setFechaModificacion(entity.getFechaModificacion());
        dto.setEliminado(entity.getEliminado());

        if (entity.getUsuario() != null) {
            dto.setUsuarioId(entity.getUsuario().getId());
            dto.setUsuarioNombre(entity.getUsuario().getNombreCompleto());
        }

        return dto;
    }

    /**
     * Convierte DTO a Entity
     */
    private FiltroGuardado convertirAEntity(FiltroGuardadoDTO dto, Usuario usuario) {
        FiltroGuardado entity = new FiltroGuardado();
        entity.setNombre(dto.getNombre());
        entity.setTipoFiltro(dto.getTipoFiltro());
        entity.setFiltrosJson(dto.getFiltrosJson());
        entity.setUsuario(usuario);
        // mantener el flag activo según el DTO o true por defecto
        entity.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        if (dto.getFechaCreacion() != null) {
            entity.setFechaCreacion(dto.getFechaCreacion());
        }
        return entity;
    }

    /**
     * Actualiza los campos de una entity existente con los datos del DTO
     */
    private void actualizarCamposEntity(FiltroGuardado entity, FiltroGuardadoDTO dto) {
        entity.setNombre(dto.getNombre());
        entity.setTipoFiltro(dto.getTipoFiltro());
        entity.setFiltrosJson(dto.getFiltrosJson());
        if (dto.getActivo() != null) {
            entity.setActivo(dto.getActivo());
        }
    }
}
