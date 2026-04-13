package com.sodimac.rebates.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sodimac.rebates.model.RebateAcuerdos;

public class ExportAcuerdosExcel {

	private static Logger logger = LoggerFactory.getLogger(ExportAcuerdosExcel.class);

	public static ByteArrayInputStream acuerdosListToExcelFile(List<RebateAcuerdos> listPolizaContable) {

//		try (Workbook workbook = new XSSFWorkbook()) {
		try (SXSSFWorkbook workbook = new SXSSFWorkbook(10000)) {
			Sheet sheet = workbook.createSheet("Acuerdos Comerciales");

			// Tamaño de columnas: cm aprox.
			sheet.setColumnWidth(0, 10 * 256);
			sheet.setColumnWidth(1, 20 * 256);
			sheet.setColumnWidth(2, 20 * 256);
			sheet.setColumnWidth(3, 30 * 256);
			sheet.setColumnWidth(4, 40 * 256);
			sheet.setColumnWidth(5, 20 * 256);
			sheet.setColumnWidth(6, 20 * 256);
			sheet.setColumnWidth(7, 30 * 256);
			sheet.setColumnWidth(8, 20 * 256);
			sheet.setColumnWidth(9, 20 * 256);
			sheet.setColumnWidth(10, 20 * 256);
			sheet.setColumnWidth(11, 20 * 256);
			sheet.setColumnWidth(12, 20 * 256);
			sheet.setColumnWidth(13, 20 * 256);
			sheet.setColumnWidth(14, 20 * 256);
			sheet.setColumnWidth(15, 20 * 256);
			

			// keep 100 rows in memory, exceeding rows will be
			// flushed to diskSheet sh = workbook.createSheet();

			Row row = sheet.createRow(0);
			CellStyle headerCellStyle = workbook.createCellStyle();
			// headerCellStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
			// headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

			Font font = ((SXSSFWorkbook) workbook).createFont();
//			XSSFFont font = ((XSSFWorkbook) workbook).createFont();
			// font.setFontName("Arial");
			// font.setFontHeightInPoints((short) 12);
			font.setBold(true);
			// font.setColor(IndexedColors.WHITE.getIndex());
			headerCellStyle.setFont(font);

			String[] columns = { "Id", "Número Proveedor", "RFC", "Correo", "Razón Social",
					"Estado", "Familia", "Clasificación", "Número Acuerdo", "Tipo Acuerdo", "Moneda",
					"Valor", "Tipo Valor", "Fill Rate", "Programa Pago", "Marca" };

			// Creating header
			for (int i = 0; i < columns.length; i++) {

				Cell cell = row.createCell(i);
				cell.setCellValue(columns[i]);
				cell.setCellStyle(headerCellStyle);
			}

			if (listPolizaContable != null && listPolizaContable.size() >= 1) {

				// Creating style rows
				CellStyle rowStyle = workbook.createCellStyle();
				rowStyle.setWrapText(true);

				// Creating data rows for each discount
				for (int i = 0; i < listPolizaContable.size(); i++) {

					Row dataRow = sheet.createRow(i + 1);
					// Cell 0
					Cell cell = dataRow.createCell(0);
					cell.setCellValue(listPolizaContable.get(i).getId());

					// Cell 1
					cell = dataRow.createCell(1);
					cell.setCellValue(listPolizaContable.get(i).getNumeroProveedor());

					// Cell 2
					cell = dataRow.createCell(2);
					cell.setCellValue(listPolizaContable.get(i).getRfc());

					// Cell 3
					cell = dataRow.createCell(3);
					cell.setCellValue(listPolizaContable.get(i).getCorreo());

					// Cell 4
					cell = dataRow.createCell(4);
					cell.setCellValue(listPolizaContable.get(i).getRazonSocial());

					// Cell 5
					cell = dataRow.createCell(5);
					cell.setCellValue(listPolizaContable.get(i).getEstado());

					// Cell 6
					cell = dataRow.createCell(6);
					cell.setCellValue(listPolizaContable.get(i).getFamilia());

					// Cell 7
					cell = dataRow.createCell(7);
					cell.setCellValue(listPolizaContable.get(i).getClasificacionComercial());

					// Cell 8
					cell = dataRow.createCell(8);
					cell.setCellValue(listPolizaContable.get(i).getNumeroAcuerdo());

					// Cell 9
					cell = dataRow.createCell(9);
					cell.setCellValue(listPolizaContable.get(i).getTipoAcuerdo());

					// Cell 10
					cell = dataRow.createCell(10);
					cell.setCellValue(listPolizaContable.get(i).getMoneda());
					
					// Cell 11
					cell = dataRow.createCell(11);
					cell.setCellValue(listPolizaContable.get(i).getValor());
					
					// Cell 12
					cell = dataRow.createCell(12);
					cell.setCellValue(listPolizaContable.get(i).getTipoValor());
					
					// Cell 13
					cell = dataRow.createCell(13);
					cell.setCellValue(listPolizaContable.get(i).getFillRate());

					// Cell 14
					cell = dataRow.createCell(14);
					cell.setCellValue(listPolizaContable.get(i).getProgramaPago());
					
					// Cell 15
					cell = dataRow.createCell(15);
					cell.setCellValue(listPolizaContable.get(i).getMarca());
				}

			}

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			workbook.write(outputStream);
			return new ByteArrayInputStream(outputStream.toByteArray());

		} catch (Exception ex) {

			ex.printStackTrace();
			logger.error("exportExcell-AcuerdosComerciales ", ex);
			return null;
		}
	}

}