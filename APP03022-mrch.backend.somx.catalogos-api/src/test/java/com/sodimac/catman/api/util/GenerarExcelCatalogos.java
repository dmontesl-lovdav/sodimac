package com.sodimac.catman.api.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

/**
 * Clase utilitaria para generar el Excel de catálogos desde la base de datos.
 * Ejecutar el test generarExcelCatalogos() para generar el archivo.
 *
 * El archivo generado contiene:
 * - Hoja RESUMEN con todos los catálogos y su estado
 * - Una hoja por cada catálogo con sus detalles en 3 idiomas
 * - Hoja RELACIONES con dependencias entre catálogos
 *
 * Conexión: PostgreSQL localhost:5434/b2b_portal schema=shared_catalogs
 *
 * @version 2.1 - Genera desde base de datos con todos los campos
 */
public class GenerarExcelCatalogos {

    // Configuración de conexión a BD
    private static final String DB_URL = "jdbc:postgresql://localhost:5434/b2b_portal";
    private static final String DB_USER = "wwwb2bportal";
    private static final String DB_PASSWORD = "b8@qU0YM1HU>";
    private static final String SCHEMA = "shared_catalogs";

    @Disabled("Utilidad de generacion Excel - ejecutar manualmente cuando sea necesario")
    @Test
    public void generarExcelCatalogos() throws IOException, SQLException {
        String outputPath = "C:\\workspace-fbc\\catalogos_fbc_revision.xlsx";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Workbook workbook = new XSSFWorkbook()) {

            // Establecer schema
            conn.createStatement().execute("SET search_path TO " + SCHEMA);

            // Estilos
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle satHeaderStyle = createSatHeaderStyle(workbook);
            CellStyle yellowStyle = createYellowStyle(workbook);
            CellStyle normalStyle = createNormalStyle(workbook);

            // Hoja resumen desde BD
            createResumenSheetFromDB(workbook, conn, headerStyle, yellowStyle, normalStyle);

            // Hojas por cada catálogo desde BD
            createCatalogSheetsFromDB(workbook, conn, headerStyle, satHeaderStyle, normalStyle);

            // Hoja de relaciones desde BD
            createRelacionesSheetFromDB(workbook, conn, headerStyle, normalStyle);

            // Guardar archivo
            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                workbook.write(fos);
            }

            System.out.println("Excel generado exitosamente en: " + outputPath);
        }
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createSatHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createYellowStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createNormalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * Crea la hoja RESUMEN con todos los catálogos desde la BD
     */
    private static void createResumenSheetFromDB(Workbook workbook, Connection conn,
            CellStyle headerStyle, CellStyle yellowStyle, CellStyle normalStyle) throws SQLException {

        Sheet sheet = workbook.createSheet("RESUMEN");

        // Headers
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Código", "Prefijo", "Nombre", "Descripción", "Módulo", "Tipo Catálogo", "Status", "Detalles", "Creado", "Actualizado"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Query para obtener catálogos con conteo de detalles
        String sql = "SELECT h.id, h.code, h.prefix, h.name, h.description, h.module, h.catalog_type, h.status, " +
                "(SELECT COUNT(*) FROM catalog_detail d WHERE d.header_id = h.id AND d.status = 1) as detail_count, " +
                "h.created_at, h.updated_at " +
                "FROM catalog_header h ORDER BY h.id";

        int rowNum = 1;
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Row row = sheet.createRow(rowNum++);
                int status = rs.getInt("status");
                int detailCount = rs.getInt("detail_count");
                boolean needsAttention = status != 1 || detailCount == 0;

                // ID
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(rs.getInt("id"));
                cell0.setCellStyle(needsAttention ? yellowStyle : normalStyle);

                // Código
                Cell cell1 = row.createCell(1);
                cell1.setCellValue(rs.getString("code"));
                cell1.setCellStyle(needsAttention ? yellowStyle : normalStyle);

                // Prefijo
                Cell cell2 = row.createCell(2);
                cell2.setCellValue(rs.getString("prefix"));
                cell2.setCellStyle(needsAttention ? yellowStyle : normalStyle);

                // Nombre
                Cell cell3 = row.createCell(3);
                cell3.setCellValue(rs.getString("name"));
                cell3.setCellStyle(needsAttention ? yellowStyle : normalStyle);

                // Descripción
                Cell cell4 = row.createCell(4);
                cell4.setCellValue(rs.getString("description"));
                cell4.setCellStyle(needsAttention ? yellowStyle : normalStyle);

                // Módulo
                Cell cell5 = row.createCell(5);
                cell5.setCellValue(rs.getString("module"));
                cell5.setCellStyle(needsAttention ? yellowStyle : normalStyle);

                // Tipo Catálogo
                Cell cell6 = row.createCell(6);
                cell6.setCellValue(rs.getString("catalog_type"));
                cell6.setCellStyle(needsAttention ? yellowStyle : normalStyle);

                // Status
                Cell cell7 = row.createCell(7);
                cell7.setCellValue(status == 1 ? "ACTIVO" : "INACTIVO");
                cell7.setCellStyle(needsAttention ? yellowStyle : normalStyle);

                // Detalles count
                Cell cell8 = row.createCell(8);
                cell8.setCellValue(detailCount);
                cell8.setCellStyle(needsAttention ? yellowStyle : normalStyle);

                // Creado
                Cell cell9 = row.createCell(9);
                Timestamp createdAt = rs.getTimestamp("created_at");
                cell9.setCellValue(createdAt != null ? createdAt.toString().substring(0, 19) : "");
                cell9.setCellStyle(needsAttention ? yellowStyle : normalStyle);

                // Actualizado
                Cell cell10 = row.createCell(10);
                Timestamp updatedAt = rs.getTimestamp("updated_at");
                cell10.setCellValue(updatedAt != null ? updatedAt.toString().substring(0, 19) : "");
                cell10.setCellStyle(needsAttention ? yellowStyle : normalStyle);
            }
        }

        // Autosize columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Crea una hoja por cada catálogo con sus detalles en 3 idiomas
     */
    private static void createCatalogSheetsFromDB(Workbook workbook, Connection conn,
            CellStyle headerStyle, CellStyle satHeaderStyle, CellStyle normalStyle) throws SQLException {

        // Obtener todos los catálogos activos
        String catalogSql = "SELECT id, code, prefix, catalog_type FROM catalog_header WHERE status = 1 ORDER BY id";

        try (PreparedStatement catalogStmt = conn.prepareStatement(catalogSql);
             ResultSet catalogRs = catalogStmt.executeQuery()) {

            while (catalogRs.next()) {
                int headerId = catalogRs.getInt("id");
                String code = catalogRs.getString("code");
                String catalogType = catalogRs.getString("catalog_type");

                // Determinar si es catálogo SAT
                boolean isSat = "SAT_FISCAL".equals(catalogType) || code.startsWith("c_");

                // Nombre de hoja (máx 31 caracteres para Excel)
                String sheetName = code.length() > 31 ? code.substring(0, 31) : code;

                Sheet sheet = workbook.createSheet(sheetName);

                // Headers según tipo de catálogo
                Row headerRow = sheet.createRow(0);
                String[] headers;
                if (isSat) {
                    headers = new String[]{"ID", "Clave", "External Key", "Value", "Español", "English", "Português",
                            "Internal Status", "Color", "Sort Order", "Valid From", "Valid To", "Vigencia", "Attributes", "Status"};
                } else {
                    headers = new String[]{"ID", "Clave", "External Key", "Value", "Español", "English", "Português",
                            "Internal Status", "Color", "Sort Order", "Status"};
                }

                CellStyle currentHeaderStyle = isSat ? satHeaderStyle : headerStyle;
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(currentHeaderStyle);
                }

                // Query para detalles con traducciones en 3 idiomas
                String detailSql = "SELECT d.id, d.key, d.external_key, d.value, d.internal_status, d.color, d.sort_order, " +
                        "d.valid_from, d.valid_to, d.attributes, d.status, " +
                        "(SELECT dl.description FROM dictionary_lang dl WHERE dl.dict_id = d.dict_id AND dl.lang_id = 1) as desc_es, " +
                        "(SELECT dl.description FROM dictionary_lang dl WHERE dl.dict_id = d.dict_id AND dl.lang_id = 2) as desc_en, " +
                        "(SELECT dl.description FROM dictionary_lang dl WHERE dl.dict_id = d.dict_id AND dl.lang_id = 3) as desc_pt " +
                        "FROM catalog_detail d WHERE d.header_id = ? ORDER BY d.sort_order, d.id";

                int rowNum = 1;
                try (PreparedStatement detailStmt = conn.prepareStatement(detailSql)) {
                    detailStmt.setInt(1, headerId);
                    try (ResultSet detailRs = detailStmt.executeQuery()) {
                        while (detailRs.next()) {
                            Row row = sheet.createRow(rowNum++);
                            int colNum = 0;

                            // ID
                            Cell cellId = row.createCell(colNum++);
                            cellId.setCellValue(detailRs.getInt("id"));
                            cellId.setCellStyle(normalStyle);

                            // Clave
                            Cell cellKey = row.createCell(colNum++);
                            cellKey.setCellValue(detailRs.getString("key"));
                            cellKey.setCellStyle(normalStyle);

                            // External Key
                            Cell cellExtKey = row.createCell(colNum++);
                            String extKey = detailRs.getString("external_key");
                            cellExtKey.setCellValue(extKey != null ? extKey : "");
                            cellExtKey.setCellStyle(normalStyle);

                            // Value
                            Cell cellValue = row.createCell(colNum++);
                            String value = detailRs.getString("value");
                            cellValue.setCellValue(value != null ? value : "");
                            cellValue.setCellStyle(normalStyle);

                            // Español
                            Cell cellEs = row.createCell(colNum++);
                            String descEs = detailRs.getString("desc_es");
                            cellEs.setCellValue(descEs != null ? descEs : "");
                            cellEs.setCellStyle(normalStyle);

                            // English
                            Cell cellEn = row.createCell(colNum++);
                            String descEn = detailRs.getString("desc_en");
                            cellEn.setCellValue(descEn != null ? descEn : "");
                            cellEn.setCellStyle(normalStyle);

                            // Português
                            Cell cellPt = row.createCell(colNum++);
                            String descPt = detailRs.getString("desc_pt");
                            cellPt.setCellValue(descPt != null ? descPt : "");
                            cellPt.setCellStyle(normalStyle);

                            // Internal Status
                            Cell cellIntStatus = row.createCell(colNum++);
                            int intStatus = detailRs.getInt("internal_status");
                            if (!detailRs.wasNull()) {
                                cellIntStatus.setCellValue(intStatus);
                            }
                            cellIntStatus.setCellStyle(normalStyle);

                            // Color
                            Cell cellColor = row.createCell(colNum++);
                            String color = detailRs.getString("color");
                            cellColor.setCellValue(color != null ? color : "");
                            cellColor.setCellStyle(normalStyle);

                            // Sort Order
                            Cell cellSort = row.createCell(colNum++);
                            cellSort.setCellValue(detailRs.getInt("sort_order"));
                            cellSort.setCellStyle(normalStyle);

                            // Campos adicionales para SAT
                            if (isSat) {
                                // Valid From
                                Cell cellValidFrom = row.createCell(colNum++);
                                Date validFrom = detailRs.getDate("valid_from");
                                cellValidFrom.setCellValue(validFrom != null ? validFrom.toString() : "");
                                cellValidFrom.setCellStyle(normalStyle);

                                // Valid To
                                Cell cellValidTo = row.createCell(colNum++);
                                Date validTo = detailRs.getDate("valid_to");
                                cellValidTo.setCellValue(validTo != null ? validTo.toString() : "");
                                cellValidTo.setCellStyle(normalStyle);

                                // Vigencia (calculada)
                                Cell cellVigencia = row.createCell(colNum++);
                                String vigencia = calcularVigencia(validFrom, validTo);
                                cellVigencia.setCellValue(vigencia);
                                cellVigencia.setCellStyle(normalStyle);

                                // Attributes
                                Cell cellAttrs = row.createCell(colNum++);
                                String attrs = detailRs.getString("attributes");
                                cellAttrs.setCellValue(attrs != null ? attrs : "");
                                cellAttrs.setCellStyle(normalStyle);
                            }

                            // Status
                            Cell cellStatus = row.createCell(colNum);
                            int status = detailRs.getInt("status");
                            cellStatus.setCellValue(status == 1 ? "ACTIVO" : "INACTIVO");
                            cellStatus.setCellStyle(normalStyle);
                        }
                    }
                }

                // Autosize columns
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }
            }
        }
    }

    /**
     * Crea la hoja RELACIONES desde la tabla catalog_detail_relation
     */
    private static void createRelacionesSheetFromDB(Workbook workbook, Connection conn,
            CellStyle headerStyle, CellStyle normalStyle) throws SQLException {

        Sheet sheet = workbook.createSheet("RELACIONES");

        // Headers
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Catálogo Origen", "Detalle Origen (Key)", "External Key Origen",
                "Catálogo Destino", "Detalle Destino (Key)", "External Key Destino",
                "Tipo Relación", "Status", "Creado Por", "Fecha Creación"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Query para relaciones
        String sql = "SELECT r.id, r.relation_type, r.status, r.created_by, r.created_at, " +
                "sh.code as source_catalog, sd.key as source_key, sd.external_key as source_ext_key, " +
                "th.code as target_catalog, td.key as target_key, td.external_key as target_ext_key " +
                "FROM catalog_detail_relation r " +
                "JOIN catalog_detail sd ON r.source_detail_id = sd.id " +
                "JOIN catalog_header sh ON sd.header_id = sh.id " +
                "JOIN catalog_detail td ON r.target_detail_id = td.id " +
                "JOIN catalog_header th ON td.header_id = th.id " +
                "ORDER BY sh.code, sd.key, th.code, td.key";

        int rowNum = 1;
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Row row = sheet.createRow(rowNum++);

                // ID
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(rs.getInt("id"));
                cell0.setCellStyle(normalStyle);

                // Catálogo Origen
                Cell cell1 = row.createCell(1);
                cell1.setCellValue(rs.getString("source_catalog"));
                cell1.setCellStyle(normalStyle);

                // Detalle Origen Key
                Cell cell2 = row.createCell(2);
                cell2.setCellValue(rs.getString("source_key"));
                cell2.setCellStyle(normalStyle);

                // External Key Origen
                Cell cell3 = row.createCell(3);
                String srcExtKey = rs.getString("source_ext_key");
                cell3.setCellValue(srcExtKey != null ? srcExtKey : "");
                cell3.setCellStyle(normalStyle);

                // Catálogo Destino
                Cell cell4 = row.createCell(4);
                cell4.setCellValue(rs.getString("target_catalog"));
                cell4.setCellStyle(normalStyle);

                // Detalle Destino Key
                Cell cell5 = row.createCell(5);
                cell5.setCellValue(rs.getString("target_key"));
                cell5.setCellStyle(normalStyle);

                // External Key Destino
                Cell cell6 = row.createCell(6);
                String tgtExtKey = rs.getString("target_ext_key");
                cell6.setCellValue(tgtExtKey != null ? tgtExtKey : "");
                cell6.setCellStyle(normalStyle);

                // Tipo Relación
                Cell cell7 = row.createCell(7);
                cell7.setCellValue(rs.getString("relation_type"));
                cell7.setCellStyle(normalStyle);

                // Status
                Cell cell8 = row.createCell(8);
                int status = rs.getInt("status");
                cell8.setCellValue(status == 1 ? "ACTIVO" : "INACTIVO");
                cell8.setCellStyle(normalStyle);

                // Creado Por
                Cell cell9 = row.createCell(9);
                String createdBy = rs.getString("created_by");
                cell9.setCellValue(createdBy != null ? createdBy : "");
                cell9.setCellStyle(normalStyle);

                // Fecha Creación
                Cell cell10 = row.createCell(10);
                Timestamp createdAt = rs.getTimestamp("created_at");
                cell10.setCellValue(createdAt != null ? createdAt.toString().substring(0, 19) : "");
                cell10.setCellStyle(normalStyle);
            }
        }

        // Si no hay relaciones, agregar nota
        if (rowNum == 1) {
            Row noteRow = sheet.createRow(1);
            Cell noteCell = noteRow.createCell(0);
            noteCell.setCellValue("No hay relaciones configuradas en la tabla catalog_detail_relation");
            noteCell.setCellStyle(normalStyle);
        }

        // Nota explicativa
        Row noteRow = sheet.createRow(rowNum + 2);
        Cell noteCell = noteRow.createCell(0);
        noteCell.setCellValue("NOTA: Las relaciones definen dependencias entre catálogos. Ej: UsoCFDI G01 depende de RegimenFiscal 601");

        Row noteRow2 = sheet.createRow(rowNum + 3);
        Cell noteCell2 = noteRow2.createCell(0);
        noteCell2.setCellValue("Endpoint: GET /{code}/details?dependsOn=c_RegimenFiscal:{external_key}");

        // Autosize columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Calcula el estado de vigencia de un registro basado en valid_from y valid_to.
     *
     * @param validFrom fecha de inicio de vigencia (puede ser null)
     * @param validTo fecha de fin de vigencia (puede ser null)
     * @return "SIN RESTRICCIÓN", "VIGENTE", "EXPIRADO" o "PENDIENTE"
     */
    private static String calcularVigencia(Date validFrom, Date validTo) {
        // Si ambas fechas son null, no hay restricción de vigencia
        if (validFrom == null && validTo == null) {
            return "SIN RESTRICCIÓN";
        }

        LocalDate today = LocalDate.now();
        LocalDate from = validFrom != null ? validFrom.toLocalDate() : null;
        LocalDate to = validTo != null ? validTo.toLocalDate() : null;

        // Si valid_to < hoy -> EXPIRADO
        if (to != null && to.isBefore(today)) {
            return "EXPIRADO";
        }

        // Si valid_from > hoy -> PENDIENTE (aún no entra en vigencia)
        if (from != null && from.isAfter(today)) {
            return "PENDIENTE";
        }

        // Si valid_from <= hoy AND (valid_to es null OR valid_to >= hoy) -> VIGENTE
        return "VIGENTE";
    }
}
