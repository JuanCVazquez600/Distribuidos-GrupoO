package com.ong.donationexcel.service;

import com.ong.donationexcel.dto.DonacionDTO;
import com.ong.donationexcel.model.CategoriaEnum;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExcelReportService {

    @Autowired
    private DonacionService donacionService;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Genera un reporte Excel con todas las donaciones organizadas por categorías según la consigna
     */
    public byte[] generarReporteExcelPorCategorias() throws IOException {
        List<DonacionDTO> donaciones = donacionService.obtenerDonacionesOrdenadas();
        
        // Agrupar donaciones por categoría
        Map<CategoriaEnum, List<DonacionDTO>> donacionesPorCategoria = 
            donaciones.stream().collect(Collectors.groupingBy(DonacionDTO::getCategoria));

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            // Crear estilos
            CellStyle headerStyle = crearEstiloEncabezado(workbook);
            CellStyle dataStyle = crearEstiloDatos(workbook);
            CellStyle dateStyle = crearEstiloFecha(workbook);

            // Crear una hoja por cada categoría que tenga donaciones
            for (CategoriaEnum categoria : CategoriaEnum.values()) {
                List<DonacionDTO> donacionesCategoria = donacionesPorCategoria.get(categoria);
                if (donacionesCategoria != null && !donacionesCategoria.isEmpty()) {
                    crearHojaCategoria(workbook, categoria, donacionesCategoria, headerStyle, dataStyle, dateStyle);
                }
            }

            // Si no hay donaciones de alguna categoría, crear hoja vacía con mensaje
            for (CategoriaEnum categoria : CategoriaEnum.values()) {
                if (!donacionesPorCategoria.containsKey(categoria)) {
                    crearHojaVacia(workbook, categoria, headerStyle, dataStyle);
                }
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Crea una hoja para una categoría específica según la consigna
     */
    private void crearHojaCategoria(Workbook workbook, CategoriaEnum categoria, List<DonacionDTO> donaciones,
                                   CellStyle headerStyle, CellStyle dataStyle, CellStyle dateStyle) {
        Sheet sheet = workbook.createSheet(categoria.getDescripcion());
        
        // Encabezados según la consigna: Fecha de Alta, Descripcion, Cantidad, Eliminado, Usuario Alta y Usuario Modificación
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Fecha de Alta", "Descripcion", "Cantidad", "Eliminado", "Usuario Alta", "Usuario Modificación"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Datos
        int rowNum = 1;
        for (DonacionDTO donacion : donaciones) {
            Row row = sheet.createRow(rowNum++);
            
            // Fecha de Alta
            Cell fechaCell = row.createCell(0);
            if (donacion.getFechaAlta() != null) {
                fechaCell.setCellValue(donacion.getFechaAlta().format(dateFormatter));
            } else {
                fechaCell.setCellValue("");
            }
            fechaCell.setCellStyle(dateStyle);
            
            // Descripción
            row.createCell(1).setCellValue(donacion.getDescripcion() != null ? donacion.getDescripcion() : "");
            
            // Cantidad
            row.createCell(2).setCellValue(donacion.getCantidad() != null ? donacion.getCantidad() : 0);
            
            // Eliminado
            row.createCell(3).setCellValue(donacion.getEliminado() != null ? (donacion.getEliminado() ? "SI" : "NO") : "NO");
            
            // Usuario Alta
            row.createCell(4).setCellValue(donacion.getUsuarioAlta() != null ? donacion.getUsuarioAlta() : "");
            
            // Usuario Modificación
            row.createCell(5).setCellValue(donacion.getUsuarioModificacion() != null ? donacion.getUsuarioModificacion() : "");

            // Aplicar estilos a todas las celdas excepto la fecha
            for (int i = 1; i < headers.length; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }

        // Ajustar ancho de columnas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Crea una hoja vacía para categorías sin donaciones
     */
    private void crearHojaVacia(Workbook workbook, CategoriaEnum categoria, CellStyle headerStyle, CellStyle dataStyle) {
        Sheet sheet = workbook.createSheet(categoria.getDescripcion());
        
        Row headerRow = sheet.createRow(0);
        Cell headerCell = headerRow.createCell(0);
        headerCell.setCellValue("No hay donaciones registradas para esta categoría");
        headerCell.setCellStyle(headerStyle);
        
        sheet.autoSizeColumn(0);
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