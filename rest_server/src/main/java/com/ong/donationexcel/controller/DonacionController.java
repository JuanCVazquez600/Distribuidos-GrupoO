package com.ong.donationexcel.controller;

import com.ong.donationexcel.dto.DonacionDTO;
import com.ong.donationexcel.model.CategoriaEnum;
import com.ong.donationexcel.service.DonacionService;
import com.ong.donationexcel.service.ExcelReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/donaciones")
@Tag(name = "Donaciones", description = "API para gestión de donaciones y generación de reportes Excel")
public class DonacionController {

    @Autowired
    private DonacionService donacionService;

    @Autowired
    private ExcelReportService excelReportService;

    @Operation(summary = "Descargar reporte Excel de donaciones por categorías", 
               description = "Genera y descarga un archivo Excel con todas las donaciones organizadas por categorías en hojas separadas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reporte Excel generado exitosamente",
                    content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor al generar el reporte",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/reporte/excel")
    public ResponseEntity<byte[]> descargarReporteExcel() {
        try {
            byte[] excelData = excelReportService.generarReporteExcelPorCategorias();
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "reporte_donaciones_" + timestamp + ".xlsx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(excelData.length);
            
            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
            
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Obtener todas las donaciones", 
               description = "Retorna una lista de todas las donaciones activas en el sistema")
    @ApiResponse(responseCode = "200", description = "Lista de donaciones obtenida exitosamente")
    @GetMapping
    public ResponseEntity<List<DonacionDTO>> obtenerTodasLasDonaciones() {
        List<DonacionDTO> donaciones = donacionService.obtenerTodasLasDonaciones();
        return ResponseEntity.ok(donaciones);
    }

    @Operation(summary = "Obtener donaciones por categoría", 
               description = "Retorna una lista de donaciones filtradas por la categoría especificada")
    @ApiResponse(responseCode = "200", description = "Lista de donaciones filtrada por categoría")
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<DonacionDTO>> obtenerDonacionesPorCategoria(
            @Parameter(description = "Categoría de las donaciones a obtener") 
            @PathVariable CategoriaEnum categoria) {
        List<DonacionDTO> donaciones = donacionService.obtenerDonacionesPorCategoria(categoria);
        return ResponseEntity.ok(donaciones);
    }

    @Operation(summary = "Crear nueva donación", 
               description = "Registra una nueva donación en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Donación creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PostMapping
    public ResponseEntity<DonacionDTO> crearDonacion(
            @Parameter(description = "Datos de la nueva donación") 
            @Valid @RequestBody DonacionDTO donacionDTO) {
        DonacionDTO nuevaDonacion = donacionService.crearDonacion(donacionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaDonacion);
    }

    @Operation(summary = "Obtener donación por ID", 
               description = "Retorna los detalles de una donación específica")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Donación encontrada"),
        @ApiResponse(responseCode = "404", description = "Donación no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DonacionDTO> obtenerDonacionPorId(
            @Parameter(description = "ID único de la donación") 
            @PathVariable Integer id) {
        Optional<DonacionDTO> donacion = donacionService.obtenerDonacionPorId(id);
        return donacion.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Actualizar donación", 
               description = "Actualiza los datos de una donación existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Donación actualizada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Donación no encontrada"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<DonacionDTO> actualizarDonacion(
            @Parameter(description = "ID único de la donación") 
            @PathVariable Integer id,
            @Parameter(description = "Nuevos datos de la donación") 
            @Valid @RequestBody DonacionDTO donacionDTO) {
        Optional<DonacionDTO> donacionActualizada = donacionService.actualizarDonacion(id, donacionDTO);
        return donacionActualizada.map(ResponseEntity::ok)
                                 .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Eliminar donación", 
               description = "Elimina lógicamente una donación del sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Donación eliminada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Donación no encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarDonacion(
            @Parameter(description = "ID único de la donación") 
            @PathVariable Integer id) {
        boolean eliminada = donacionService.eliminarDonacion(id);
        return eliminada ? ResponseEntity.noContent().build() 
                        : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Buscar donaciones por descripción", 
               description = "Busca donaciones que contengan la descripción especificada")
    @ApiResponse(responseCode = "200", description = "Lista de donaciones que coinciden con la búsqueda")
    @GetMapping("/buscar/descripcion")
    public ResponseEntity<List<DonacionDTO>> buscarPorDescripcion(
            @Parameter(description = "Descripción o parte de la descripción") 
            @RequestParam String descripcion) {
        List<DonacionDTO> donaciones = donacionService.buscarPorDescripcion(descripcion);
        return ResponseEntity.ok(donaciones);
    }

    @Operation(summary = "Obtener donaciones por rango de fechas", 
               description = "Retorna donaciones realizadas entre las fechas especificadas")
    @ApiResponse(responseCode = "200", description = "Lista de donaciones en el rango de fechas")
    @GetMapping("/fechas")
    public ResponseEntity<List<DonacionDTO>> obtenerDonacionesPorRangoFechas(
            @Parameter(description = "Fecha de inicio (formato: yyyy-MM-dd'T'HH:mm:ss)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @Parameter(description = "Fecha de fin (formato: yyyy-MM-dd'T'HH:mm:ss)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        List<DonacionDTO> donaciones = donacionService.obtenerDonacionesPorRangoFechas(fechaInicio, fechaFin);
        return ResponseEntity.ok(donaciones);
    }
}