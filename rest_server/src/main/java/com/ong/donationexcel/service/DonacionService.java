package com.ong.donationexcel.service;

import com.ong.donationexcel.dto.DonacionDTO;
import com.ong.donationexcel.model.CategoriaEnum;
import com.ong.donationexcel.model.InventarioDeDonaciones;
import com.ong.donationexcel.repository.DonacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DonacionService {

    @Autowired
    private DonacionRepository donacionRepository;

    /**
     * Obtiene todas las donaciones activas
     */
    public List<DonacionDTO> obtenerTodasLasDonaciones() {
        return donacionRepository.findByEliminadoFalse()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las donaciones ordenadas para el Excel según la consigna
     */
    public List<DonacionDTO> obtenerDonacionesOrdenadas() {
        return donacionRepository.findAllOrderedForExcel()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene donaciones por categoría
     */
    public List<DonacionDTO> obtenerDonacionesPorCategoria(CategoriaEnum categoria) {
        return donacionRepository.findByCategoriaAndEliminadoFalse(categoria)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Crea una nueva donación
     */
    public DonacionDTO crearDonacion(DonacionDTO donacionDTO) {
        InventarioDeDonaciones entity = convertirAEntity(donacionDTO);
        entity.setFechaAlta(LocalDateTime.now());
        entity.setEliminado(false);
        
        InventarioDeDonaciones savedEntity = donacionRepository.save(entity);
        return convertirADTO(savedEntity);
    }

    /**
     * Busca una donación por ID
     */
    public Optional<DonacionDTO> obtenerDonacionPorId(Integer id) {
        return donacionRepository.findById(id)
                .filter(donacion -> !donacion.getEliminado())
                .map(this::convertirADTO);
    }

    /**
     * Actualiza una donación existente
     */
    public Optional<DonacionDTO> actualizarDonacion(Integer id, DonacionDTO donacionDTO) {
        return donacionRepository.findById(id)
                .filter(donacion -> !donacion.getEliminado())
                .map(existingDonacion -> {
                    actualizarCamposEntity(existingDonacion, donacionDTO);
                    existingDonacion.setFechaModificacion(LocalDateTime.now());
                    InventarioDeDonaciones updatedEntity = donacionRepository.save(existingDonacion);
                    return convertirADTO(updatedEntity);
                });
    }

    /**
     * Elimina lógicamente una donación
     */
    public boolean eliminarDonacion(Integer id) {
        Optional<InventarioDeDonaciones> donacion = donacionRepository.findById(id);
        if (donacion.isPresent() && !donacion.get().getEliminado()) {
            donacion.get().setEliminado(true);
            donacion.get().setFechaModificacion(LocalDateTime.now());
            donacionRepository.save(donacion.get());
            return true;
        }
        return false;
    }

    /**
     * Busca donaciones por descripción
     */
    public List<DonacionDTO> buscarPorDescripcion(String descripcion) {
        return donacionRepository.findByDescripcionContainingIgnoreCaseAndEliminadoFalse(descripcion)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene donaciones en un rango de fechas
     */
    public List<DonacionDTO> obtenerDonacionesPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return donacionRepository.findByFechaAltaBetweenAndEliminadoFalse(fechaInicio, fechaFin)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Convierte Entity a DTO
     */
    private DonacionDTO convertirADTO(InventarioDeDonaciones entity) {
        DonacionDTO dto = new DonacionDTO();
        dto.setId(entity.getId());
        dto.setCategoria(entity.getCategoria());
        dto.setDescripcion(entity.getDescripcion());
        dto.setCantidad(entity.getCantidad());
        dto.setEliminado(entity.getEliminado());
        dto.setFechaAlta(entity.getFechaAlta());
        dto.setFechaModificacion(entity.getFechaModificacion());
        
        // Convertir usuarios a strings para el DTO
        if (entity.getUsuarioAlta() != null) {
            dto.setUsuarioAlta(entity.getUsuarioAlta().getNombreCompleto());
        }
        if (entity.getUsuarioModificacion() != null) {
            dto.setUsuarioModificacion(entity.getUsuarioModificacion().getNombreCompleto());
        }
        
        return dto;
    }

    /**
     * Convierte DTO a Entity
     */
    private InventarioDeDonaciones convertirAEntity(DonacionDTO dto) {
        InventarioDeDonaciones entity = new InventarioDeDonaciones();
        entity.setCategoria(dto.getCategoria());
        entity.setDescripcion(dto.getDescripcion());
        entity.setCantidad(dto.getCantidad());
        if (dto.getFechaAlta() != null) {
            entity.setFechaAlta(dto.getFechaAlta());
        }
        return entity;
    }

    /**
     * Actualiza los campos de una entity existente con los datos del DTO
     */
    private void actualizarCamposEntity(InventarioDeDonaciones entity, DonacionDTO dto) {
        entity.setCategoria(dto.getCategoria());
        entity.setDescripcion(dto.getDescripcion());
        entity.setCantidad(dto.getCantidad());
    }
}