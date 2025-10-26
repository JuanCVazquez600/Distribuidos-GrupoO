package com.ong.donationexcel.controller;

import com.ong.donationexcel.dto.FiltroGuardadoDTO;
import com.ong.donationexcel.model.FiltroGuardado;
import com.ong.donationexcel.service.FiltroGuardadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/filtros")
@Tag(name = "Filtros Guardados", description = "API para gestión de filtros guardados por usuarios")
public class FiltroController {

    @Autowired
    private FiltroGuardadoService filtroService;

    @Operation(summary = "Obtener filtros guardados por usuario",
               description = "Retorna todos los filtros guardados por un usuario específico")
    @ApiResponse(responseCode = "200", description = "Lista de filtros obtenida exitosamente")
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<FiltroGuardadoDTO>> obtenerFiltrosPorUsuario(
            @Parameter(description = "ID del usuario")
            @PathVariable Integer usuarioId) {
        try {
            List<FiltroGuardadoDTO> filtros = filtroService.obtenerFiltrosPorUsuario(usuarioId);
            return ResponseEntity.ok(filtros);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @Operation(summary = "Obtener filtros por usuario y tipo",
               description = "Retorna los filtros guardados por un usuario para un tipo específico")
    @ApiResponse(responseCode = "200", description = "Lista de filtros filtrada por tipo obtenida exitosamente")
    @GetMapping("/usuario/{usuarioId}/tipo/{tipoFiltro}")
    public ResponseEntity<List<FiltroGuardadoDTO>> obtenerFiltrosPorUsuarioYTipo(
            @Parameter(description = "ID del usuario")
            @PathVariable Integer usuarioId,
            @Parameter(description = "Tipo de filtro")
            @PathVariable FiltroGuardado.TipoFiltro tipoFiltro) {
        try {
            List<FiltroGuardadoDTO> filtros = filtroService.obtenerFiltrosPorUsuarioYTipo(usuarioId, tipoFiltro);
            return ResponseEntity.ok(filtros);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @Operation(summary = "Crear nuevo filtro guardado",
               description = "Guarda un nuevo filtro personalizado para un usuario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Filtro creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o filtro ya existe")
    })
    @PostMapping
    public ResponseEntity<FiltroGuardadoDTO> crearFiltro(
            @Parameter(description = "Datos del nuevo filtro")
            @Valid @RequestBody FiltroGuardadoDTO filtroDTO) {
        try {
            FiltroGuardadoDTO nuevoFiltro = filtroService.crearFiltro(filtroDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoFiltro);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @Operation(summary = "Obtener filtro por ID",
               description = "Retorna los detalles de un filtro específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Filtro encontrado"),
        @ApiResponse(responseCode = "404", description = "Filtro no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<FiltroGuardadoDTO> obtenerFiltroPorId(
            @Parameter(description = "ID único del filtro")
            @PathVariable Integer id) {
        Optional<FiltroGuardadoDTO> filtro = filtroService.obtenerFiltroPorId(id);
        return filtro.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Actualizar filtro",
               description = "Actualiza los datos de un filtro existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Filtro actualizado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Filtro no encontrado"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o sin permisos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<FiltroGuardadoDTO> actualizarFiltro(
            @Parameter(description = "ID único del filtro")
            @PathVariable Integer id,
            @Parameter(description = "Nuevos datos del filtro")
            @Valid @RequestBody FiltroGuardadoDTO filtroDTO) {
        try {
            Optional<FiltroGuardadoDTO> filtroActualizado = filtroService.actualizarFiltro(id, filtroDTO);
            return filtroActualizado.map(ResponseEntity::ok)
                                   .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @Operation(summary = "Eliminar filtro",
               description = "Elimina lógicamente un filtro del sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Filtro eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Filtro no encontrado"),
        @ApiResponse(responseCode = "400", description = "Sin permisos para eliminar el filtro")
    })
    @DeleteMapping("/{id}/usuario/{usuarioId}")
    public ResponseEntity<Void> eliminarFiltro(
            @Parameter(description = "ID único del filtro")
            @PathVariable Integer id,
            @Parameter(description = "ID del usuario propietario")
            @PathVariable Integer usuarioId) {
        try {
            boolean eliminado = filtroService.eliminarFiltro(id, usuarioId);
            return eliminado ? ResponseEntity.noContent().build()
                            : ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
