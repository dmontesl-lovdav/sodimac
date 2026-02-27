package com.sodimac.catman.api.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

/**
 * Test para leer el archivo catalogos.xlsx y mostrar su estructura.
 */
public class ExcelReaderTest {

    @Disabled("Utilidad de lectura - ejecutar manualmente cuando sea necesario")
    @Test
    public void readCatalogosExcel() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("catalogos.xlsx")) {
            if (is == null) {
                System.out.println("ERROR: No se encontró el archivo catalogos.xlsx");
                return;
            }

            Workbook workbook = new XSSFWorkbook(is);

            System.out.println("=== HOJAS DISPONIBLES ===");
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                System.out.println("  - " + workbook.getSheetName(i));
            }
            System.out.println();

            for (int sheetIdx = 0; sheetIdx < workbook.getNumberOfSheets(); sheetIdx++) {
                Sheet sheet = workbook.getSheetAt(sheetIdx);
                System.out.println("=== HOJA: " + sheet.getSheetName() + " ===");
                System.out.println("Filas: " + (sheet.getLastRowNum() + 1));

                // Headers (primera fila)
                Row headerRow = sheet.getRow(0);
                if (headerRow != null) {
                    System.out.print("Headers: ");
                    for (int col = 0; col < headerRow.getLastCellNum(); col++) {
                        Cell cell = headerRow.getCell(col);
                        System.out.print(getCellValue(cell));
                        if (col < headerRow.getLastCellNum() - 1) System.out.print(" | ");
                    }
                    System.out.println();
                }

                // Primeras 15 filas de datos
                System.out.println("Primeras 15 filas de datos:");
                int maxRows = Math.min(16, sheet.getLastRowNum() + 1);
                for (int rowIdx = 1; rowIdx < maxRows; rowIdx++) {
                    Row row = sheet.getRow(rowIdx);
                    if (row != null) {
                        System.out.print("  " + (rowIdx + 1) + ": ");
                        for (int col = 0; col < row.getLastCellNum(); col++) {
                            Cell cell = row.getCell(col);
                            System.out.print(getCellValue(cell));
                            if (col < row.getLastCellNum() - 1) System.out.print(" | ");
                        }
                        System.out.println();
                    }
                }
                System.out.println();
            }

            workbook.close();
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                double num = cell.getNumericCellValue();
                if (num == Math.floor(num)) {
                    return String.valueOf((long) num);
                }
                return String.valueOf(num);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "";
        }
    }
}
