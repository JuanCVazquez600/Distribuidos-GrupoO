package com.ong.donationexcel.controller;

import com.ong.donationexcel.dto.FiltroGuardadoDTO;
import com.ong.donationexcel.model.FiltroGuardado;
import com.ong.donationexcel.service.FiltroGuardadoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/filtros-guardados")
@Tag(name = "Filtros Guardados", description = "API para gestionar filtros guardados de reportes")
public class FiltroGuardadoController {

    @Autowired
    private FiltroGuardadoService filtroService;

    @Autowired
    private ObjectMapper objectMapper;

    @Operation(summary = "Guardar un nuevo filtro",
               description = "Guarda un conjunto de filtros con un nombre específico para el usuario")
    @PostMapping
    public ResponseEntity<?> guardarFiltro(
            @Parameter(description = "ID del usuario") @RequestParam Integer usuarioId,
            @Parameter(description = "Nombre del filtro") @RequestParam String nombre,
            @Parameter(description = "Tipo de filtro") @RequestParam FiltroGuardado.TipoFiltro tipoFiltro,
            @Parameter(description = "Filtros a guardar") @RequestBody Map<String, Object> filtros) {
        try {
            FiltroGuardadoDTO dto = new FiltroGuardadoDTO();
            dto.setUsuarioId(usuarioId);
            dto.setNombre(nombre);
            dto.setTipoFiltro(tipoFiltro);
            dto.setFiltrosJson(objectMapper.writeValueAsString(filtros));
            
            FiltroGuardadoDTO filtroGuardado = filtroService.crearFiltro(dto);
            return ResponseEntity.ok(filtroGuardado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al guardar el filtro: " + e.getMessage());
        }
    }

    @Operation(summary = "Actualizar un filtro existente",
               description = "Modifica un filtro guardado existente")
    @PutMapping("/{filtroId}")
    public ResponseEntity<?> actualizarFiltro(
            @Parameter(description = "ID del filtro") @PathVariable Integer filtroId,
            @Parameter(description = "ID del usuario") @RequestParam Integer usuarioId,
            @Parameter(description = "Nuevo nombre del filtro") @RequestParam String nombre,
            @Parameter(description = "Nuevos filtros") @RequestBody Map<String, Object> filtros) {
        try {
            FiltroGuardadoDTO dto = new FiltroGuardadoDTO();
            dto.setUsuarioId(usuarioId);
            dto.setNombre(nombre);
            dto.setFiltrosJson(objectMapper.writeValueAsString(filtros));
            
            return filtroService.actualizarFiltro(filtroId, dto)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al actualizar el filtro: " + e.getMessage());
        }
    }

    @Operation(summary = "Eliminar un filtro",
               description = "Marca como eliminado un filtro guardado")
    @DeleteMapping("/{filtroId}")
    public ResponseEntity<?> eliminarFiltro(
            @Parameter(description = "ID del filtro") @PathVariable Integer filtroId,
            @Parameter(description = "ID del usuario") @RequestParam Integer usuarioId) {
        try {
            boolean eliminado = filtroService.eliminarFiltro(filtroId, usuarioId);
            return eliminado ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al eliminar el filtro: " + e.getMessage());
        }
    }

    @Operation(summary = "Obtener filtros de un usuario",
               description = "Retorna todos los filtros guardados de un usuario para un tipo específico")
    @GetMapping
    public ResponseEntity<List<FiltroGuardadoDTO>> obtenerFiltros(
            @Parameter(description = "ID del usuario") @RequestParam Integer usuarioId,
            @Parameter(description = "Tipo de filtro") @RequestParam FiltroGuardado.TipoFiltro tipoFiltro) {
        return ResponseEntity.ok(filtroService.obtenerFiltrosPorUsuarioYTipo(usuarioId, tipoFiltro));
    }

    @Operation(summary = "Obtener un filtro específico",
               description = "Retorna los filtros guardados en un formato aplicable")
    @GetMapping("/{filtroId}")
    public ResponseEntity<?> obtenerFiltro(
            @Parameter(description = "ID del filtro") @PathVariable Integer filtroId) {
        return filtroService.obtenerFiltroPorId(filtroId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}