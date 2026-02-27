package com.sodimac.catman.api.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.util.*;

/**
 * Genera script SQL desde el archivo catalogos.xlsx
 */
public class GenerateSqlFromExcel {

    // Hojas a ignorar (no son catálogos)
    private static final Set<String> IGNORE_SHEETS = Set.of("Hoja2");

    // Contador global para dict_id
    private int dictIdCounter = 1000;

    @Disabled("Utilidad de generacion SQL - ejecutar manualmente cuando sea necesario")
    @Test
    public void generateSqlScript() throws Exception {
        StringBuilder ddl = new StringBuilder();
        StringBuilder insertDictionary = new StringBuilder();
        StringBuilder insertHeader = new StringBuilder();
        StringBuilder insertDetail = new StringBuilder();

        // DDL
        ddl.append("-- ============================================================================\n");
        ddl.append("-- CATALOGOS API - Script de Base de Datos\n");
        ddl.append("-- ============================================================================\n\n");

        ddl.append("-- Eliminar tablas si existen (en orden por dependencias)\n");
        ddl.append("DROP TABLE IF EXISTS catalog_detail CASCADE;\n");
        ddl.append("DROP TABLE IF EXISTS catalog_header CASCADE;\n");
        ddl.append("DROP TABLE IF EXISTS dictionary_lang CASCADE;\n\n");

        ddl.append("-- ============================================================================\n");
        ddl.append("-- Tabla: dictionary_lang (Diccionario de traducciones)\n");
        ddl.append("-- ============================================================================\n");
        ddl.append("CREATE TABLE dictionary_lang (\n");
        ddl.append("    id SERIAL PRIMARY KEY,\n");
        ddl.append("    dict_id INTEGER NOT NULL,\n");
        ddl.append("    lang_id INTEGER NOT NULL,\n");
        ddl.append("    description VARCHAR(512) NOT NULL,\n");
        ddl.append("    CONSTRAINT uk_dictionary_lang UNIQUE (dict_id, lang_id)\n");
        ddl.append(");\n\n");
        ddl.append("CREATE INDEX idx_dictionary_dict_id ON dictionary_lang(dict_id);\n");
        ddl.append("CREATE INDEX idx_dictionary_lang_id ON dictionary_lang(lang_id);\n\n");

        ddl.append("-- ============================================================================\n");
        ddl.append("-- Tabla: catalog_header (Encabezado de catálogos)\n");
        ddl.append("-- ============================================================================\n");
        ddl.append("CREATE TABLE catalog_header (\n");
        ddl.append("    id SERIAL PRIMARY KEY,\n");
        ddl.append("    code VARCHAR(64) NOT NULL UNIQUE,\n");
        ddl.append("    name VARCHAR(128) NOT NULL,\n");
        ddl.append("    description VARCHAR(512),\n");
        ddl.append("    module VARCHAR(32),\n");
        ddl.append("    status INTEGER NOT NULL DEFAULT 1,\n");
        ddl.append("    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\n");
        ddl.append("    updated_at TIMESTAMP\n");
        ddl.append(");\n\n");
        ddl.append("CREATE INDEX idx_catalog_header_code ON catalog_header(code);\n");
        ddl.append("CREATE INDEX idx_catalog_header_status ON catalog_header(status);\n\n");

        ddl.append("-- ============================================================================\n");
        ddl.append("-- Tabla: catalog_detail (Detalle de catálogos)\n");
        ddl.append("-- ============================================================================\n");
        ddl.append("CREATE TABLE catalog_detail (\n");
        ddl.append("    id SERIAL PRIMARY KEY,\n");
        ddl.append("    header_id INTEGER NOT NULL REFERENCES catalog_header(id),\n");
        ddl.append("    key VARCHAR(64) NOT NULL,\n");
        ddl.append("    dict_id INTEGER NOT NULL,\n");
        ddl.append("    color VARCHAR(16),\n");
        ddl.append("    sort_order INTEGER NOT NULL DEFAULT 0,\n");
        ddl.append("    status INTEGER NOT NULL DEFAULT 1,\n");
        ddl.append("    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\n");
        ddl.append("    updated_at TIMESTAMP,\n");
        ddl.append("    CONSTRAINT uk_catalog_detail UNIQUE (header_id, key),\n");
        ddl.append("    CONSTRAINT fk_catalog_detail_dict FOREIGN KEY (dict_id) REFERENCES dictionary_lang(dict_id)\n");
        ddl.append(");\n\n");
        ddl.append("CREATE INDEX idx_catalog_detail_header ON catalog_detail(header_id);\n");
        ddl.append("CREATE INDEX idx_catalog_detail_key ON catalog_detail(key);\n");
        ddl.append("CREATE INDEX idx_catalog_detail_status ON catalog_detail(status);\n\n");

        // Leer Excel y generar INSERTs
        insertDictionary.append("-- ============================================================================\n");
        insertDictionary.append("-- INSERTs: dictionary_lang\n");
        insertDictionary.append("-- ============================================================================\n\n");

        insertHeader.append("-- ============================================================================\n");
        insertHeader.append("-- INSERTs: catalog_header\n");
        insertHeader.append("-- ============================================================================\n\n");

        insertDetail.append("-- ============================================================================\n");
        insertDetail.append("-- INSERTs: catalog_detail\n");
        insertDetail.append("-- ============================================================================\n\n");

        try (InputStream is = getClass().getClassLoader().getResourceAsStream("catalogos.xlsx")) {
            if (is == null) {
                System.out.println("ERROR: No se encontró el archivo catalogos.xlsx");
                return;
            }

            Workbook workbook = new XSSFWorkbook(is);
            int headerId = 1;

            for (int sheetIdx = 0; sheetIdx < workbook.getNumberOfSheets(); sheetIdx++) {
                Sheet sheet = workbook.getSheetAt(sheetIdx);
                String sheetName = sheet.getSheetName();

                if (IGNORE_SHEETS.contains(sheetName)) {
                    continue;
                }

                // Obtener descripción del catálogo (fila 1, después del nombre)
                String catalogDescription = "";
                Row descRow = sheet.getRow(1);
                if (descRow != null && descRow.getCell(0) != null) {
                    catalogDescription = getCellValue(descRow.getCell(0));
                }

                // Determinar módulo basado en el nombre
                String module = determineModule(sheetName);

                // INSERT para catalog_header
                insertHeader.append(String.format(
                    "INSERT INTO catalog_header (id, code, name, description, module, status) VALUES (%d, '%s', '%s', '%s', '%s', 1);\n",
                    headerId,
                    escapeSQL(sheetName),
                    escapeSQL(formatName(sheetName)),
                    escapeSQL(catalogDescription),
                    module
                ));

                // Procesar filas de detalle
                // Encontrar la fila de headers (donde está idEstatus, descripción, etc.)
                int headerRowIdx = findHeaderRow(sheet);
                if (headerRowIdx < 0) {
                    headerId++;
                    continue;
                }

                Row headerRow = sheet.getRow(headerRowIdx);
                int keyColIdx = findColumnIndex(headerRow, "idEstatus", "key", "Codigo error");
                int descColIdx = findColumnIndex(headerRow, "descripción", "description", "Descripcion");
                int colorColIdx = findColumnIndex(headerRow, "Color", "Colores", "color");

                if (keyColIdx < 0 || descColIdx < 0) {
                    // Intentar con índices por defecto
                    keyColIdx = 0;
                    descColIdx = 1;
                }

                int sortOrder = 1;
                for (int rowIdx = headerRowIdx + 1; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                    Row row = sheet.getRow(rowIdx);
                    if (row == null) continue;

                    String key = getCellValue(row.getCell(keyColIdx));
                    String description = getCellValue(row.getCell(descColIdx));
                    String color = colorColIdx >= 0 ? getCellValue(row.getCell(colorColIdx)) : "";

                    // Saltar filas vacías
                    if (key.isEmpty() && description.isEmpty()) continue;
                    if (key.isEmpty()) continue;

                    int currentDictId = dictIdCounter++;

                    // INSERT para dictionary_lang (solo español por ahora)
                    insertDictionary.append(String.format(
                        "INSERT INTO dictionary_lang (dict_id, lang_id, description) VALUES (%d, 1, '%s');\n",
                        currentDictId,
                        escapeSQL(description)
                    ));

                    // INSERT para catalog_detail
                    insertDetail.append(String.format(
                        "INSERT INTO catalog_detail (header_id, key, dict_id, color, sort_order, status) VALUES (%d, '%s', %d, %s, %d, 1);\n",
                        headerId,
                        escapeSQL(key),
                        currentDictId,
                        color.isEmpty() ? "NULL" : "'" + escapeSQL(color) + "'",
                        sortOrder++
                    ));
                }

                headerId++;
            }

            workbook.close();
        }

        // Escribir archivo SQL
        String sqlPath = "src/main/resources/schema.sql";
        try (PrintWriter writer = new PrintWriter(new FileWriter(sqlPath))) {
            writer.print(ddl);
            writer.print(insertDictionary);
            writer.print("\n");
            writer.print(insertHeader);
            writer.print("\n");
            writer.print(insertDetail);
        }

        System.out.println("Script SQL generado en: " + sqlPath);
        System.out.println("Total dict_id generados: " + (dictIdCounter - 1000));
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                double num = cell.getNumericCellValue();
                if (num == Math.floor(num)) {
                    return String.valueOf((long) num);
                }
                return String.valueOf(num);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    private String escapeSQL(String value) {
        if (value == null) return "";
        return value.replace("'", "''").replace("\n", " ").replace("\r", "");
    }

    private String formatName(String sheetName) {
        // CatEstatusFactura -> Estatus Factura
        return sheetName
            .replace("Cat", "")
            .replaceAll("([a-z])([A-Z])", "$1 $2");
    }

    private String determineModule(String sheetName) {
        if (sheetName.contains("Factura") || sheetName.contains("Pago") ||
            sheetName.contains("NotaCredito") || sheetName.contains("Complemento")) {
            return "fiscal";
        }
        if (sheetName.contains("CartaPorte") || sheetName.contains("Guia") ||
            sheetName.contains("Entrega")) {
            return "transporte";
        }
        if (sheetName.contains("Msg") || sheetName.contains("Pac")) {
            return "sistema";
        }
        return "general";
    }

    private int findHeaderRow(Sheet sheet) {
        for (int i = 0; i <= Math.min(5, sheet.getLastRowNum()); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            for (int j = 0; j < row.getLastCellNum(); j++) {
                String val = getCellValue(row.getCell(j)).toLowerCase();
                if (val.contains("idestatus") || val.contains("descripción") ||
                    val.contains("descripcion") || val.equals("key")) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int findColumnIndex(Row row, String... names) {
        if (row == null) return -1;
        for (int i = 0; i < row.getLastCellNum(); i++) {
            String val = getCellValue(row.getCell(i)).toLowerCase();
            for (String name : names) {
                if (val.contains(name.toLowerCase())) {
                    return i;
                }
            }
        }
        return -1;
    }
}
