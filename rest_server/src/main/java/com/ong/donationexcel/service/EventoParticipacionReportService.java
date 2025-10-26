package com.ong.donationexcel.service;

import com.ong.donationexcel.dto.EventoSolidarioDTO;
import com.ong.donationexcel.model.EventoSolidario;
import com.ong.donationexcel.model.Usuario;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EventoParticipacionReportService {

    @Autowired
    private EventoSolidarioService eventoService;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Genera un reporte Excel de participación en eventos con filtros aplicados
     */
    public byte[] generarReporteParticipacionEventos(Map<String, Object> filtros) throws IOException {
        List<EventoSolidarioDTO> eventos = aplicarFiltros(filtros);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Crear estilos
            CellStyle headerStyle = crearEstiloEncabezado(workbook);
            CellStyle dataStyle = crearEstiloDatos(workbook);
            CellStyle dateStyle = crearEstiloFecha(workbook);

            // Crear hoja de resumen
            crearHojaResumen(workbook, eventos, headerStyle, dataStyle, dateStyle);

            // Crear hoja detallada de participación
            crearHojaParticipacionDetallada(workbook, eventos, headerStyle, dataStyle, dateStyle);

            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Aplica los filtros a la lista de eventos
     */
    public List<EventoSolidarioDTO> aplicarFiltros(Map<String, Object> filtros) {
    List<EventoSolidarioDTO> eventos = eventoService.obtenerTodosLosEventos();
        System.out.println("Total eventos antes de filtrar: " + eventos.size());
        System.out.println("Filtros recibidos: " + filtros);

        // Imprimir los eventos iniciales
        eventos.forEach(e -> System.out.println("Evento: " + e.getNombreEvento() + 
                                              ", Fecha: " + e.getFechaEvento() + 
                                              ", Participantes: " + e.getNumeroParticipantes()));


        // Filtro por rango de fechas (entre fechaInicio y fechaFin)
        LocalDateTime fechaInicioDateTime = null;
        LocalDateTime fechaFinDateTime = null;
        try {
            if (filtros.containsKey("fechaInicio") && !filtros.get("fechaInicio").toString().isEmpty()) {
                String fechaStr = filtros.get("fechaInicio").toString();
                if (fechaStr.contains("T")) {
                    fechaInicioDateTime = LocalDateTime.parse(fechaStr);
                } else {
                    fechaInicioDateTime = LocalDate.parse(fechaStr).atStartOfDay();
                }
            }
            if (filtros.containsKey("fechaFin") && !filtros.get("fechaFin").toString().isEmpty()) {
                String fechaStr = filtros.get("fechaFin").toString();
                if (fechaStr.contains("T")) {
                    fechaFinDateTime = LocalDateTime.parse(fechaStr);
                } else {
                    fechaFinDateTime = LocalDate.parse(fechaStr).atTime(23, 59, 59);
                }
            }
        } catch (Exception e) {
            System.out.println("Error al procesar fechas: " + e.getMessage());
            e.printStackTrace();
        }

        if (fechaInicioDateTime != null && fechaFinDateTime != null) {
            final LocalDateTime inicioFinal = fechaInicioDateTime;
            final LocalDateTime finFinal = fechaFinDateTime;
            eventos = eventos.stream()
                    .filter(e -> {
                        boolean dentro = (e.getFechaEvento().isEqual(inicioFinal) || e.getFechaEvento().isAfter(inicioFinal))
                                && (e.getFechaEvento().isEqual(finFinal) || e.getFechaEvento().isBefore(finFinal));
                        System.out.println("Comparando fecha evento: " + e.getFechaEvento() + " entre " + inicioFinal + " y " + finFinal + ": " + dentro);
                        return dentro;
                    })
                    .collect(Collectors.toList());
            System.out.println("Eventos después de filtro rango fechas: " + eventos.size());
        } else if (fechaInicioDateTime != null) {
            final LocalDateTime inicioFinal = fechaInicioDateTime;
            eventos = eventos.stream()
                    .filter(e -> e.getFechaEvento().isEqual(inicioFinal) || e.getFechaEvento().isAfter(inicioFinal))
                    .collect(Collectors.toList());
            System.out.println("Eventos después de filtro fechaInicio: " + eventos.size());
        } else if (fechaFinDateTime != null) {
            final LocalDateTime finFinal = fechaFinDateTime;
            eventos = eventos.stream()
                    .filter(e -> e.getFechaEvento().isEqual(finFinal) || e.getFechaEvento().isBefore(finFinal))
                    .collect(Collectors.toList());
            System.out.println("Eventos después de filtro fechaFin: " + eventos.size());
        }



        // Filtro por número mínimo de participantes
        if (filtros.containsKey("minParticipantes") && !filtros.get("minParticipantes").toString().isEmpty()) {
            try {
                Integer minParticipantes = Integer.parseInt(filtros.get("minParticipantes").toString());
                if (minParticipantes > 0) {
                    eventos = eventos.stream()
                            .filter(e -> e.getNumeroParticipantes() >= minParticipantes)
                            .collect(Collectors.toList());
                    System.out.println("Eventos después de filtro minParticipantes: " + eventos.size());
                } else {
                    System.out.println("Ignorando filtro minParticipantes porque es 0 o negativo");
                }
            } catch (Exception e) {
                System.out.println("Error al procesar minParticipantes: " + e.getMessage());
            }
        }

        // Filtro por número máximo de participantes
        if (filtros.containsKey("maxParticipantes") && !filtros.get("maxParticipantes").toString().isEmpty()) {
            try {
                Integer maxParticipantes = Integer.parseInt(filtros.get("maxParticipantes").toString());
                if (maxParticipantes > 0) {
                    eventos = eventos.stream()
                            .filter(e -> e.getNumeroParticipantes() <= maxParticipantes)
                            .collect(Collectors.toList());
                    System.out.println("Eventos después de filtro maxParticipantes: " + eventos.size());
                } else {
                    System.out.println("Ignorando filtro maxParticipantes porque es 0 o negativo");
                }
            } catch (Exception e) {
                System.out.println("Error al procesar maxParticipantes: " + e.getMessage());
            }
        }

        // Filtro por nombre del evento
        if (filtros.containsKey("nombreEvento") && !filtros.get("nombreEvento").toString().isEmpty()) {
            String nombre = filtros.get("nombreEvento").toString().toLowerCase();
            eventos = eventos.stream()
                    .filter(e -> e.getNombreEvento().toLowerCase().contains(nombre))
                    .collect(Collectors.toList());
            System.out.println("Eventos después de filtro nombreEvento: " + eventos.size());
        }

        // Filtro por eventos propios (creados por el usuario actual)
        if (filtros.containsKey("eventosPropios") && filtros.get("eventosPropios").toString().equals("true")) {
            if (filtros.containsKey("usuarioId") && !filtros.get("usuarioId").toString().isEmpty()) {
                try {
                    Integer usuarioId = Integer.parseInt(filtros.get("usuarioId").toString());
                    eventos = eventos.stream()
                            .filter(e -> e.getUsuarioAltaId() != null && e.getUsuarioAltaId().equals(usuarioId))
                            .collect(Collectors.toList());
                    System.out.println("Eventos después de filtro usuarioId: " + eventos.size());
                } catch (Exception e) {
                    // Ignorar filtro si hay error de parsing
                }
            }
        }

        System.out.println("Total eventos después de aplicar todos los filtros: " + eventos.size());
        return eventos;
    }

    /**
     * Crea la hoja de resumen
     */
    private void crearHojaResumen(Workbook workbook, List<EventoSolidarioDTO> eventos,
                                 CellStyle headerStyle, CellStyle dataStyle, CellStyle dateStyle) {
        Sheet sheet = workbook.createSheet("Resumen Participación");

        // Encabezados
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Nombre Evento", "Fecha Evento", "N° Participantes", "Participantes"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Datos
        int rowNum = 1;
        for (EventoSolidarioDTO evento : eventos) {
            Row row = sheet.createRow(rowNum++);

            // Nombre del evento
            row.createCell(0).setCellValue(evento.getNombreEvento());

            // Fecha del evento
            Cell fechaCell = row.createCell(1);
            fechaCell.setCellValue(evento.getFechaEvento().format(dateFormatter));
            fechaCell.setCellStyle(dateStyle);

            // Número de participantes
            row.createCell(2).setCellValue(evento.getNumeroParticipantes());

            // Lista de participantes
            String participantesStr = String.join(", ", evento.getParticipantes());
            row.createCell(3).setCellValue(participantesStr);

            // Aplicar estilos
            for (int i = 0; i < headers.length; i++) {
                if (i != 1) { // La fecha ya tiene estilo
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }
        }

        // Ajustar ancho de columnas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Crea la hoja detallada de participación
     */
    private void crearHojaParticipacionDetallada(Workbook workbook, List<EventoSolidarioDTO> eventos,
                                                CellStyle headerStyle, CellStyle dataStyle, CellStyle dateStyle) {
        Sheet sheet = workbook.createSheet("Participación Detallada");

        // Encabezados
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Evento", "Fecha Evento", "Participante", "Fecha Alta Evento"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Datos
        int rowNum = 1;
        for (EventoSolidarioDTO evento : eventos) {
            for (String participante : evento.getParticipantes()) {
                Row row = sheet.createRow(rowNum++);

                // Nombre del evento
                row.createCell(0).setCellValue(evento.getNombreEvento());

                // Fecha del evento
                Cell fechaCell = row.createCell(1);
                fechaCell.setCellValue(evento.getFechaEvento().format(dateFormatter));
                fechaCell.setCellStyle(dateStyle);

                // Participante
                row.createCell(2).setCellValue(participante);

                // Fecha de alta del evento (aproximada)
                Cell fechaAltaCell = row.createCell(3);
                fechaAltaCell.setCellValue("N/A");
                fechaAltaCell.setCellStyle(dateStyle);

                // Aplicar estilos
                row.getCell(0).setCellStyle(dataStyle);
                row.getCell(2).setCellStyle(dataStyle);
            }
        }

        // Ajustar ancho de columnas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Crea el estilo para los encabezados
     */
    private CellStyle crearEstiloEncabezado(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    /**
     * Crea el estilo para los datos
     */
    private CellStyle crearEstiloDatos(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * Crea el estilo para las fechas
     */
    private CellStyle crearEstiloFecha(Workbook workbook) {
        CellStyle style = crearEstiloDatos(workbook);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
}
