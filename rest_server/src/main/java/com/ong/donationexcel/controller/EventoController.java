package com.ong.donationexcel.controller;

import com.ong.donationexcel.dto.EventoSolidarioDTO;
import com.ong.donationexcel.service.EventoParticipacionReportService;
import com.ong.donationexcel.service.EventoSolidarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/eventos")
@Tag(name = "Eventos", description = "API para gestión de eventos y reportes de participación")
public class EventoController {

    @Autowired
    private EventoParticipacionReportService participacionReportService;

    @Autowired
    private EventoSolidarioService eventoService;

    @Operation(summary = "Obtener todos los eventos",
               description = "Retorna la lista completa de eventos activos")
    @ApiResponse(responseCode = "200", description = "Lista de eventos obtenida exitosamente")
    @GetMapping
    public ResponseEntity<List<EventoSolidarioDTO>> obtenerTodosLosEventos() {
        List<EventoSolidarioDTO> eventos = eventoService.obtenerTodosLosEventos();
        return ResponseEntity.ok(eventos);
    }

    @Operation(summary = "Aplicar filtros al reporte de participación en eventos",
               description = "Retorna la lista de eventos filtrados según los criterios especificados")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de eventos filtrados obtenida exitosamente",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = EventoSolidarioDTO.class))),
        @ApiResponse(responseCode = "400", description = "Error en los parámetros de filtro",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/reporte/participacion/filtrar")
    public ResponseEntity<List<EventoSolidarioDTO>> filtrarEventosParticipacion(
            @Parameter(description = "Filtros a aplicar (JSON con campos opcionales: fechaInicio, fechaFin, nombreEvento, minParticipantes, maxParticipantes)")
            @RequestBody Map<String, Object> filtros) {
        try {
            List<EventoSolidarioDTO> eventosFiltrados = participacionReportService.aplicarFiltros(filtros);
            return ResponseEntity.ok(eventosFiltrados);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }


}
