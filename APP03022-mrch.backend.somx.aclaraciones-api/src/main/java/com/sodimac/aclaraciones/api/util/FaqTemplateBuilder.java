/*-----------------------------------------------------------
 * util – FaqTemplateBuilder.java
 *-----------------------------------------------------------*/
package com.sodimac.aclaraciones.api.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;

/**
 * Genera un archivo XLSX en memoria con:
 *
 * • Hoja «Plantilla» → encabezados (question, answer, publicado) + lista
 * desplegable ✔ / ✖ en la columna “publicado”.
 * • Hoja «Ejemplo» → cuatro filas de demo ya rellenadas.
 */
public final class FaqTemplateBuilder {

    private FaqTemplateBuilder() {
    }

    public static byte[] build() {

        try (Workbook wb = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            /* ── estilos de encabezado ── */
            Font bold = wb.createFont();
            bold.setBold(true);
            CellStyle th = wb.createCellStyle();
            th.setFont(bold);

            /* ────────── Hoja 1: Plantilla ────────── */
            Sheet tpl = wb.createSheet("Plantilla");

            Row h = tpl.createRow(0);
            h.createCell(0).setCellValue("question");
            h.getCell(0).setCellStyle(th);
            h.createCell(1).setCellValue("answer");
            h.getCell(1).setCellStyle(th);
            h.createCell(2).setCellValue("publicado");
            h.getCell(2).setCellStyle(th);

            // Validación ✔ / ✖ en columna C
            DataValidationHelper dvh = tpl.getDataValidationHelper();
            DataValidationConstraint cst = dvh.createExplicitListConstraint(
                    new String[] { "✔", "✖" });
            CellRangeAddressList range = new CellRangeAddressList(1, 10_000, 2, 2); // C2:C10001
            DataValidation dv = dvh.createValidation(cst, range);
            dv.setShowErrorBox(true);
            tpl.addValidationData(dv);

            tpl.autoSizeColumn(0);
            tpl.autoSizeColumn(1);
            tpl.autoSizeColumn(2);

            /* ────────── Hoja 2: Ejemplo ─────────── */
            Sheet ex = wb.createSheet("Ejemplo");
            Row eh = ex.createRow(0);
            eh.createCell(0).setCellValue("question");
            eh.getCell(0).setCellStyle(th);
            eh.createCell(1).setCellValue("answer");
            eh.getCell(1).setCellStyle(th);
            eh.createCell(2).setCellValue("publicado");
            eh.getCell(2).setCellStyle(th);

            Object[][] demo = {
                    { "¿Cuál es el horario de atención?", "L-V 9-18 h", "✔" },
                    { "¿Cómo solicitar factura?", "Ingresa a tu perfil y…", "✔" },
                    { "¿Puedo cambiar mi pedido?", "Sí, dentro de las 24 h", "✖" },
                    { "¿Cómo rastreo mi envío?", "Usa el ID en la sección…", "✔" }
            };
            for (int i = 0; i < demo.length; i++) {
                Row r = ex.createRow(i + 1);
                r.createCell(0).setCellValue((String) demo[i][0]);
                r.createCell(1).setCellValue((String) demo[i][1]);
                r.createCell(2).setCellValue((String) demo[i][2]);
            }
            ex.autoSizeColumn(0);
            ex.autoSizeColumn(1);
            ex.autoSizeColumn(2);

            /* ── devolver como byte[] ── */
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error creating FAQ template", e);
        }
    }
}
