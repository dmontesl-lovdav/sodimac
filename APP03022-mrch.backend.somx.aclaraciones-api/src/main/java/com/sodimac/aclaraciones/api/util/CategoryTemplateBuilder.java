/*-----------------------------------------------------------
 * NEW util  – CategoryTemplateBuilder.java
 *-----------------------------------------------------------*/
package com.sodimac.aclaraciones.api.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;

public final class CategoryTemplateBuilder {

    private CategoryTemplateBuilder() {
    }

    /**
     * Generates an .xlsx file in memory with:
     * • Sheet **Plantilla** → only headers + data-validation (“✔ / ✖”) in
     * **Publicado** column
     * • Sheet **Ejemplo** → 4 demo rows already filled-in
     */
    public static byte[] build() {

        try (Workbook wb = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            /* fonts & styles */
            Font bold = wb.createFont();
            bold.setBold(true);
            CellStyle th = wb.createCellStyle();
            th.setFont(bold);

            /* ────────────────── Sheet 1: Plantilla ────────────────── */
            Sheet tpl = wb.createSheet("Plantilla");

            // header
            Row h = tpl.createRow(0);
            h.createCell(0).setCellValue("name");
            h.getCell(0).setCellStyle(th);
            h.createCell(1).setCellValue("description");
            h.getCell(1).setCellStyle(th);
            h.createCell(2).setCellValue("publicado");
            h.getCell(2).setCellStyle(th);

            // data-validation (column C)
            DataValidationHelper dvh = tpl.getDataValidationHelper();
            DataValidationConstraint cst = dvh.createExplicitListConstraint(new String[] { "✔", "✖" });
            CellRangeAddressList range = new CellRangeAddressList(1, 10_000, 2, 2); // C2:C10001
            DataValidation dv = dvh.createValidation(cst, range);
            dv.setShowErrorBox(true);
            tpl.addValidationData(dv);

            tpl.autoSizeColumn(0);
            tpl.autoSizeColumn(1);
            tpl.autoSizeColumn(2);

            /* ────────────────── Sheet 2: Ejemplo ──────────────────── */
            Sheet ex = wb.createSheet("Ejemplo");
            Row eh = ex.createRow(0); // same headers
            eh.createCell(0).setCellValue("name");
            eh.getCell(0).setCellStyle(th);
            eh.createCell(1).setCellValue("description");
            eh.getCell(1).setCellStyle(th);
            eh.createCell(2).setCellValue("publicado");
            eh.getCell(2).setCellStyle(th);

            Object[][] demo = {
                    { "General", "General FAQs", "✔" },
                    { "Payments", "Billing methods, refunds", "✔" },
                    { "Returns", "Returns policy & workflow", "✖" },
                    { "Shipping", "Delivery times & tracking", "✔" }
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

            /* write to byte[] */
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error creating template", e);
        }
    }
}
